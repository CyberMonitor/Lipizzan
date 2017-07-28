package com.android.mediaserver.db;

import android.content.Context;
import com.activeandroid.query.Select;
import com.android.mediaserver.file.FileUtils;
import java.io.File;

public class DatabaseManager {
  private static DatabaseManager mInstance;
  private Context mContext;

  private DatabaseManager(Context context) {
    this.mContext = context;
  }

  public static DatabaseManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new DatabaseManager(context);
    }
    return mInstance;
  }

  public synchronized boolean isHashExists(String hash) {
    boolean z = true;
    synchronized (this) {
      if (hash != null) {
        if (!hash.isEmpty()) {
          if (new Select().from(FetchedFileInfo.class).where("Hash = ?", hash).executeSingle()
              == null) {
            z = false;
          }
        }
      }
      z = false;
    }
    return z;
  }

  public synchronized void createOrUpdateFile(File file) {
    FetchedFileInfo fetchedFileInfo = (FetchedFileInfo) new Select().from(FetchedFileInfo.class)
        .where("Path = ?", file.getAbsolutePath())
        .executeSingle();
    if (fetchedFileInfo == null) {
      fetchedFileInfo = new FetchedFileInfo();
    }
    fetchedFileInfo.path = file.getAbsolutePath();
    fetchedFileInfo.hash = FileUtils.getFileSha1(file);
    fetchedFileInfo.save();
  }
}
