package com.android.mediaserver.fetchers.file;

import android.content.Context;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.file.FileLister;
import com.android.mediaserver.file.filefilter.TrueFileFilter;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class UserRequestedFilesFetcher {
  private Context mContext;

  public UserRequestedFilesFetcher(Context context) {
    mContext = context;
  }

  public Collection<File> fetch() {
    File userFilesDirectory = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getUserFilesDirectoryName());
    if (userFilesDirectory.exists()) {
      return FileLister.listFiles(userFilesDirectory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
    }
    return new ArrayList();
  }
}
