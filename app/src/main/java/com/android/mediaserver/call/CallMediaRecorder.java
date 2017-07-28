package com.android.mediaserver.call;

import android.media.MediaRecorder;
import timber.log.Timber;

public class CallMediaRecorder {
  private int mAudioSource;
  private MediaRecorder mRecorder;

  public CallMediaRecorder(int audioSource) {
    Timber.tag(CallMediaRecorder.class.getName());
    mAudioSource = audioSource;
  }

  public void startRecording(String filename) throws Exception {
    Timber.i("start recording using media recorder", new Object[0]);
    if (mRecorder != null) {
      Timber.i("already recording", new Object[0]);
      return;
    }
    mRecorder = new MediaRecorder();
    mRecorder.setAudioSource(mAudioSource);
    mRecorder.setOutputFormat(6);
    mRecorder.setOutputFile(filename);
    mRecorder.setAudioEncoder(3);
    mRecorder.prepare();
    mRecorder.start();
  }

  public void stopRecording() throws Exception {
    if (mRecorder != null) {
      Timber.i("stop recording", new Object[0]);
      mRecorder.stop();
      mRecorder.release();
      mRecorder = null;
      return;
    }
    Timber.i("not recording", new Object[0]);
  }
}
