package com.android.mediaserver.fetchers.file;

import android.os.Environment;
import com.android.mediaserver.file.FileLister;
import java.io.File;
import java.util.Collection;

public class SdCardDataFetcher {
  public Collection<File> fetch(String[] extensions) {
    return FileLister.listFiles(Environment.getExternalStorageDirectory(), extensions, true);
  }
}
