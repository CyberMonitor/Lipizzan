package com.android.mediaserver.fetchers.media;

import android.content.Context;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.file.FileLister;
import com.android.mediaserver.file.filefilter.TrueFileFilter;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class ScreenshotsFetcher {
  private Context mContext;

  public ScreenshotsFetcher(Context context) {
    mContext = context;
  }

  public Collection<File> fetch() {
    File screenshotsDirectory = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getScreenshotsDirectoryName());
    if (screenshotsDirectory.exists()) {
      return FileLister.listFiles(screenshotsDirectory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
    }
    return new ArrayList();
  }
}
