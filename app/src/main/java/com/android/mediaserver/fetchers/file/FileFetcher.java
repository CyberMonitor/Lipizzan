package com.android.mediaserver.fetchers.file;

import com.android.mediaserver.fetchers.FetchedFile;
import java.util.ArrayList;
import java.util.List;

public abstract class FileFetcher {
  protected List<FetchedFile> mFetchedFiles;

  public List<FetchedFile> fetch() {
    if (mFetchedFiles == null) {
      mFetchedFiles = new ArrayList();
    }
    return mFetchedFiles;
  }
}
