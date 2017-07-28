package com.android.mediaserver.task;

import android.content.Context;
import com.android.mediaserver.camera.CameraManager;
import com.android.mediaserver.camera.CameraType;
import com.android.mediaserver.command.CameraSnapshotCommand;
import com.android.mediaserver.command.Command;
import timber.log.Timber;

public class CameraSnapshotTask extends Task {
  public CameraSnapshotTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    Timber.d("CameraSnapshotTask started", new Object[0]);
    publishProgress(new TaskStatus[] { TaskStatus.IN_PROGRESS });
    boolean takeFront = false;
    boolean takeBack = false;
    String cameraType = ((CameraSnapshotCommand) mCommand).getCameraType();
    if (cameraType.equalsIgnoreCase(CameraType.BOTH.toString())) {
      takeFront = true;
      takeBack = true;
    } else if (cameraType.equalsIgnoreCase(CameraType.FRONT.toString())) {
      takeFront = true;
    } else if (cameraType.equalsIgnoreCase(CameraType.BACK.toString())) {
      takeBack = true;
    }
    CameraManager.getInstance(mContext).takeSnapshot(takeFront, takeBack);
    publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
    return null;
  }

  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }
}
