package com.android.mediaserver.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.config.Config;

public class Connectivity {
  public static NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
    if (cm != null) {
      return cm.getActiveNetworkInfo();
    }
    return null;
  }

  public static boolean isConnected(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return info != null && info.isConnected();
  }

  public static boolean isConnectedWifi(Context context) {
    boolean isConnectedToWifi = true;
    NetworkInfo info = getNetworkInfo(context);
    if (!(info != null && info.isConnected() && info.getType() == 1)) {
      isConnectedToWifi = false;
    }
    context.getApplicationContext()
        .getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .edit()
        .putBoolean(AppConstants.IS_CONNECTED_TO_WIFI_KEY, isConnectedToWifi)
        .commit();
    return isConnectedToWifi;
  }

  public static boolean isConnectedMobile(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return info != null && info.isConnected() && info.getType() == 0;
  }

  public static boolean isConnectedFast(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return info != null && info.isConnected() && isConnectionFast(info.getType(),
        info.getSubtype());
  }

  public static boolean isConnectionFast(int type, int subType) {
    if (type == 1) {
      return true;
    }
    if (type != 0) {
      return false;
    }
    switch (subType) {
      case 1:
        return false;
      case 2:
        return false;
      case 3:
      case 5:
      case 6:
      case 8:
      case 9:
      case 10:
      case 12:
      case 13:
      case 14:
      case 15:
        return true;
      case 4:
        return false;
      case 7:
        return false;
      case 11:
        return false;
      default:
        return false;
    }
  }

  public static boolean isRoaming(Context context) {
    boolean isRoaming;
    if (true == Config.getInstance(context).getCheckRoaming()) {
      ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm != null) {
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
          isRoaming = ni.isRoaming();
        } else {
          isRoaming = false;
        }
      } else {
        isRoaming = false;
      }
    } else {
      isRoaming = false;
    }
    context.getApplicationContext()
        .getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .edit()
        .putBoolean(AppConstants.IS_ROAMING_KEY, isRoaming)
        .commit();
    return isRoaming;
  }
}
