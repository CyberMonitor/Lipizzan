package com.android.mediaserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.mediaserver.MyApplication;

public class ScreenStateReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    MyApplication application = (MyApplication) context.getApplicationContext();
    if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
      application.stopLocation();
    } else {
      application.startLocation();
    }
  }
}
