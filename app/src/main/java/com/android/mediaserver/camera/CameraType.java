package com.android.mediaserver.camera;

public enum CameraType {
  BOTH("both"), FRONT("front"), BACK("back");

  private final String mType;

  private CameraType(String type) {
    this.mType = type;
  }

  public String toString() {
    return this.mType;
  }
}
