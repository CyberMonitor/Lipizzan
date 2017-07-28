package com.android.mediaserver.command;

import com.google.gson.annotations.SerializedName;

public class FetchUserFileCommand extends Command {
  @SerializedName("filePath") private String mFilePath;

  public FetchUserFileCommand(int id, String filePath) {
    super(id);
    mType = "user_file";
    mFilePath = filePath;
  }

  public String getFilePath() {
    return mFilePath;
  }

  public String toString() {
    return "[id: " + mId + " type: " + mType + " filePath: " + mFilePath + "]";
  }
}
