package com.android.mediaserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.mediaserver.AppConstants;

public class BootReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    Log.d("BootReceiver", "Received boot!");
    context.getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .edit()
        .putBoolean(AppConstants.ROOTED_ON_BOOT, true)
        .commit();
  }
}
