package com.android.mediaserver.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import java.util.TimeZone;

public class DeviceInformation {
  private static DeviceInformation mInstance;
  private TelephonyManager mTelephonyManager;
  private WifiManager mWifiManager;

  private DeviceInformation(Context context) {
    mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  }

  public static synchronized DeviceInformation getInstance(Context context) {
    DeviceInformation deviceInformation;
    synchronized (DeviceInformation.class) {
      if (mInstance == null) {
        mInstance = new DeviceInformation(context);
      }
      deviceInformation = mInstance;
    }
    return deviceInformation;
  }

  public String getDeviceManufacturer() {
    return Build.MANUFACTURER;
  }

  public String getDeviceModel() {
    return Build.MODEL;
  }

  public String getDeviceMacAddress() {
    if (mWifiManager == null) {
      return "";
    }
    String mac = mWifiManager.getConnectionInfo().getMacAddress();
    if (mac != null) {
      return mac;
    }
    return "";
  }

  public String getOSVersion() {
    return VERSION.RELEASE;
  }

  public String getCarrierName() {
    if (mTelephonyManager == null) {
      return "";
    }
    String carrier = mTelephonyManager.getNetworkOperatorName();
    if (carrier != null) {
      return carrier;
    }
    return "";
  }

  public String getPhoneNumber() {
    if (mTelephonyManager == null) {
      return "";
    }
    String number = mTelephonyManager.getLine1Number();
    if (number != null) {
      return number;
    }
    return "";
  }

  public String getDeviceId() {
    if (mTelephonyManager == null) {
      return "";
    }
    String id = mTelephonyManager.getDeviceId();
    if (id != null) {
      return id;
    }
    return "";
  }

  public String getSubscriberId() {
    if (mTelephonyManager == null) {
      return "";
    }
    String subscriberId = mTelephonyManager.getSubscriberId();
    if (subscriberId != null) {
      return subscriberId;
    }
    return "";
  }

  public String getDeviceTimeZone() {
    return TimeZone.getDefault().getDisplayName(false, 0);
  }

  public String toString() {
    return "Manufacturer: "
        + getDeviceManufacturer()
        + "\n"
        + "Model: "
        + getDeviceModel()
        + "\n"
        + "Mac address: "
        + getDeviceMacAddress()
        + "\n"
        + "OS version: "
        + getOSVersion()
        + "\n"
        + "Carrier name: "
        + getCarrierName()
        + "\n"
        + "Phone number: "
        + getPhoneNumber()
        + "\n"
        + "IMEI: "
        + getDeviceId()
        + "\n"
        + "IMSI: "
        + getSubscriberId();
  }
}
