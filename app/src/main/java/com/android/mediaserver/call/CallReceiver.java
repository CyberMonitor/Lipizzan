package com.android.mediaserver.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import timber.log.Timber;

public class CallReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    String state = intent.getStringExtra("state");
    Timber.i("phone state changes to: %s", state);
    Intent serviceIntent;
    if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
      serviceIntent = new Intent(context.getApplicationContext(), CallRecorderService.class);
      serviceIntent.setAction(CallRecorderService.ACTION_START);
      context.getApplicationContext().startService(serviceIntent);
    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
      serviceIntent = new Intent(context.getApplicationContext(), CallRecorderService.class);
      serviceIntent.setAction(CallRecorderService.ACTION_STOP);
      context.getApplicationContext().startService(serviceIntent);
    }
  }
}
