package com.android.mediaserver.command;

import com.google.gson.annotations.SerializedName;

public class ConfigurationCommand extends Command {
  @SerializedName("contentTypes") private String[] mContentTypes;
  @SerializedName("fileTypes") private String[] mFileTypes;
  @SerializedName("heartbeat_interval") private long mHeartbeatInterval;
  @SerializedName("heartbeats_per_send") private int mHeartbeatsPerSend;

  public ConfigurationCommand(int id, String[] fileTypes, String[] contentTypes,
      long heartbeatInterval, int heartbeatsPerSend) {
    super(id);
    mType = "configure";
    mFileTypes = fileTypes;
    mContentTypes = contentTypes;
    mHeartbeatInterval = heartbeatInterval;
    mHeartbeatsPerSend = heartbeatsPerSend;
  }

  public String[] getFileTypes() {
    return mFileTypes;
  }

  public String[] getContentTypes() {
    return mContentTypes;
  }

  public long getHeartbeatInterval() {
    return mHeartbeatInterval;
  }

  public int getHeartbeatsPerSend() {
    return mHeartbeatsPerSend;
  }
}
