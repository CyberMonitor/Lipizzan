package com.android.mediaserver.anti;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Debug;
import android.provider.Settings.Global;

public class AntiDebug {
  public static boolean isDebug(Context context) {
    return isDebuggable(context) || isDebuggerConnected() || isAdbDebugEnabled(context);
  }

  private static boolean isDebuggable(Context context) {
    return 1 == (context.getApplicationInfo().flags & 2);
  }

  private static boolean isDebuggerConnected() {
    return Debug.isDebuggerConnected();
  }

  private static boolean isAdbDebugEnabled(Context context) {
    if (VERSION.SDK_INT < 17) {
      return false;
    }
    try {
      if (1 == Global.getInt(context.getContentResolver(), "adb_enabled")) {
        return true;
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }
}
