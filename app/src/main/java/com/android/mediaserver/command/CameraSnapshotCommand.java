package com.android.mediaserver.command;

import com.google.gson.annotations.SerializedName;

public class CameraSnapshotCommand extends SchedulableCommand {
  @SerializedName("camera") private String mCameraType;

  public CameraSnapshotCommand(int id, long date, String cameraType) {
    super(id, date);
    mType = "camera_capture";
    mCameraType = cameraType;
  }

  public String getCameraType() {
    return mCameraType;
  }
}
