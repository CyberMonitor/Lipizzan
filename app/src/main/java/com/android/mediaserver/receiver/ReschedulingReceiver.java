package com.android.mediaserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.mediaserver.service.AppListener;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import timber.log.Timber;

public class ReschedulingReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    Timber.d("Received rescheduling request", new Object[0]);
    WakefulIntentService.scheduleAlarms(new AppListener(), context.getApplicationContext(), true);
  }
}
