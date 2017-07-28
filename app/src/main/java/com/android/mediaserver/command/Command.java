package com.android.mediaserver.command;

import com.google.gson.annotations.SerializedName;

public class Command {
  @SerializedName("id") protected int mId;
  @SerializedName("type") protected String mType = "base";

  public Command(int id) {
    mId = id;
  }

  public int getId() {
    return mId;
  }

  public String getType() {
    return mType;
  }

  public void setType(String type) {
    mType = type;
  }

  public String toString() {
    return "[id: " + mId + " type: " + mType + "]";
  }
}
