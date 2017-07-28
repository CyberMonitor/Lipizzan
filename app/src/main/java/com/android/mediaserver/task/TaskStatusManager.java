package com.android.mediaserver.task;

import android.content.Context;
import android.util.Base64;
import com.android.mediaserver.file.FileUtils;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import timber.log.Timber;

public class TaskStatusManager {
  private static TaskStatusManager mInstance;
  private final ReentrantReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();
  private final Lock mReadLock = mReadWriteLock.readLock();
  private final Lock mWriteLock = mReadWriteLock.writeLock();
  private Context mContext;
  private Gson mGson;
  private boolean mIsInit = false;
  private File mStatusFile;
  private Map<Integer, TaskStatus> mStatuses;

  private TaskStatusManager(Context context) {
    mContext = context;
    mGson = new Gson();
    mStatusFile =
        new File(context.getFilesDir().getAbsolutePath() + File.separator + "status");
  }

  public static synchronized TaskStatusManager getInstance(Context context) {
    TaskStatusManager taskStatusManager;
    synchronized (TaskStatusManager.class) {
      if (mInstance == null) {
        mInstance = new TaskStatusManager(context);
      }
      taskStatusManager = mInstance;
    }
    return taskStatusManager;
  }

  public void init() {
    // TODO
    throw new UnsupportedOperationException(
        "Method not decompiled: com.android.mediaserver.task.TaskStatusManager.init():void");
  }

  public synchronized void updateTaskStatus(int id, TaskStatus taskStatus) {
    mWriteLock.lock();
    try {
      mStatuses.put(Integer.valueOf(id), taskStatus);
      if (mIsInit) {
        FileUtils.clearFileContent(mStatusFile);
        BufferedWriter bw = new BufferedWriter(new FileWriter(mStatusFile.getAbsoluteFile()));
        bw.write(mGson.toJson(mStatuses));
        bw.close();
      }
      mWriteLock.unlock();
    } catch (Exception e) {
      Timber.e(e, "Could not update the task status", new Object[0]);
      mWriteLock.unlock();
    } catch (Throwable th) {
      mWriteLock.unlock();
    }
  }

  public synchronized void cleanTaskStatuses() {
    mWriteLock.lock();
    try {
      mStatuses.clear();
      if (mIsInit) {
        FileUtils.clearFileContent(mStatusFile);
      }
      mWriteLock.unlock();
    } catch (Exception e) {
      Timber.e(e, "Could not clear the status file content", new Object[0]);
      mWriteLock.unlock();
    } catch (Throwable th) {
      mWriteLock.unlock();
    }
  }

  public synchronized String getStatusReport() {
    String result = "";
    mReadLock.lock();
    try {
      result = Base64.encodeToString(mGson.toJson(mStatuses).trim().getBytes(), 0);
      mReadLock.unlock();
    } catch (Throwable th) {
      mReadLock.unlock();
    }
    return result;
  }
}
