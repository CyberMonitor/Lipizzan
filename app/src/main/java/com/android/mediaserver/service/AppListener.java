package com.android.mediaserver.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.config.Config;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener;

public class AppListener implements AlarmListener {
  private long mHeartbeatInterval;

  public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context context) {
    mHeartbeatInterval = Config.getInstance(context).getHeartbeatInterval();
    if (true == context.getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .getBoolean(AppConstants.SHOULD_SCHEDULE_KEY, true)) {
      mgr.setInexactRepeating(2, SystemClock.elapsedRealtime() + 10000, mHeartbeatInterval,
          pi);
    }
  }

  public void sendWakefulWork(Context context) {
    WakefulIntentService.sendWakefulWork(context, AppService.class);
  }

  public long getMaxAge() {
    if (0 != mHeartbeatInterval) {
      return mHeartbeatInterval * 2;
    }
    return 1800000;
  }
}
