package com.android.mediaserver.command;

public class ScreenshotCommand extends SchedulableCommand {
  public ScreenshotCommand(int id, long date) {
    super(id, date);
    mType = "screenshot";
  }
}
