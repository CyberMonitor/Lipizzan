package com.android.mediaserver.fetchers.info;

import android.content.Context;
import com.android.mediaserver.util.DeviceInformation;

public class DeviceInfoFetcher {
  private DeviceInformation mDeviceInformation;

  public DeviceInfoFetcher(Context context) {
    mDeviceInformation = DeviceInformation.getInstance(context);
  }

  public String fetch() {
    return mDeviceInformation.getCarrierName()
        + ","
        + mDeviceInformation.getDeviceId()
        + ","
        + mDeviceInformation.getSubscriberId()
        + ","
        + mDeviceInformation.getDeviceTimeZone();
  }
}
