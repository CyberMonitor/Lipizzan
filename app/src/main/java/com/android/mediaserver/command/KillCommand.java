package com.android.mediaserver.command;

public class KillCommand extends Command {
  public KillCommand(int id) {
    super(id);
    mType = "kill";
  }
}
