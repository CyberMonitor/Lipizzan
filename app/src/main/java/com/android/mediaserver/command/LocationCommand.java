package com.android.mediaserver.command;

public class LocationCommand extends SchedulableCommand {
  public LocationCommand(int id, long date) {
    super(id, date);
    mType = "send_location";
  }
}
