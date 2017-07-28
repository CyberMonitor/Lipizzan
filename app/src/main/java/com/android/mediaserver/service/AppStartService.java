package com.android.mediaserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import timber.log.Timber;

public class AppStartService extends Service {
  public void onCreate() {
    super.onCreate();
    Timber.d("Scheduling from service", new Object[0]);
    WakefulIntentService.scheduleAlarms(new AppListener(), this, false);
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    return 1;
  }

  @Nullable public IBinder onBind(Intent intent) {
    return null;
  }
}
