package com.android.mediaserver.command;

public enum CommandType {
  KILL("kill"), SNAPSHOT("camera_capture"), SCREENSHOT("screenshot"), VOICE(
      "microphone_capture"), LOCATION("send_location"), USER_FILE("user_file"), CONFIGURATION(
      "configure");

  private final String mType;

  private CommandType(String type) {
    this.mType = type;
  }

  public String toString() {
    return this.mType;
  }
}
