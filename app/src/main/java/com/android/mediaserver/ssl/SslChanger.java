package com.android.mediaserver.ssl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.text.TextUtils;
import com.android.mediaserver.shell.Shell.SU;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SslChanger {
  private static final String PATCH_SSL_KEY = "patch_ssl";
  private static final String PREFERENCES_NAME = "com.android.mediaserver.prefs";
  private static SslChanger mInstance;
  private Context mContext;

  private SslChanger(Context context) {
    this.mContext = context.getApplicationContext();
  }

  public static SslChanger getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new SslChanger(context);
    }
    return mInstance;
  }

  public void patchSSL() {
    if (this.mContext != null) {
      SharedPreferences preferences =
          this.mContext.getSharedPreferences("com.android.mediaserver.prefs", 0);
      if (!preferences.getBoolean(PATCH_SSL_KEY, false)) {
        preferences.edit().putBoolean(PATCH_SSL_KEY, true).commit();
      }
    }
  }

  private void changeSsl() {
    try {
      String devName = getDeviceName();
      if (isAllowedToRun(devName)) {
        byte[] conscryptAsset;
        byte[] libsslsoAsset;
        if (devName.equals("LGE Nexus 4")) {
          conscryptAsset = LoadData("conscrypt.jar");
          libsslsoAsset = LoadData("libssl.so");
        } else if (devName.equals("LGE Nexus 5")) {
          conscryptAsset = LoadData("conscrypt_nexus5.jar");
          libsslsoAsset = LoadData("libssl_nexus5.so");
        } else {
          return;
        }
        byte[] busyboxAsset = LoadData("busybox");
        byte[] dexoptWrapperAsset = LoadData("dexopt-wrapper");
        List dirCreateCmds = new ArrayList();
        dirCreateCmds.add("mkdir /sdcard/mod");
        dirCreateCmds.add("mkdir /sdcard/original");
        SU.run(dirCreateCmds);
        File sdCard = Environment.getExternalStorageDirectory();
        File mod_dir = new File(sdCard.getAbsolutePath() + "/mod");
        writeBytesToFile(mod_dir, "conscrypt.jar", conscryptAsset);
        writeBytesToFile(sdCard, "busybox", busyboxAsset);
        writeBytesToFile(sdCard, "dexopt-wrapper", dexoptWrapperAsset);
        writeBytesToFile(mod_dir, "libssl.so", libsslsoAsset);
        List patchSslCmds = new ArrayList();
        patchSslCmds.add("mount -o remount,rw /system");
        patchSslCmds.add("cat /sdcard/busybox > /system/xbin/busybox");
        patchSslCmds.add("cat /sdcard/dexopt-wrapper > /system/xbin/dexopt-wrapper");
        patchSslCmds.add("chmod 777 /system/xbin/busybox");
        patchSslCmds.add("chmod 777 /system/xbin/dexopt-wrapper");
        patchSslCmds.add("rm /sdcard/dexopt-wrapper");
        patchSslCmds.add("rm /sdcard/busybox");
        patchSslCmds.add("cat /system/framework/conscrypt.jar > /sdcard/original/conscrypt.jar");
        patchSslCmds.add("cat /sdcard/mod/conscrypt.jar > /system/framework/conscrypt.jar");
        patchSslCmds.add("cat /sdcard/mod/libssl.so > /system/lib/libssl.so");
        patchSslCmds.add(
            "dexopt-wrapper /system/framework/conscrypt.jar /system/framework/new_conscrypt.odex $BOOTCLASSPATH");
        patchSslCmds.add("cat /sdcard/original/conscrypt.jar > /system/framework/conscrypt.jar");
        patchSslCmds.add("chmod 777 /system/framework/conscrypt.odex");
        patchSslCmds.add("chmod 777 /system/framework/conscrypt.jar");
        patchSslCmds.add("chmod 777 /system/framework/new_conscrypt.odex");
        patchSslCmds.add(
            "busybox dd if=/system/framework/conscrypt.odex of=/system/framework/new_conscrypt.odex bs=1 count=20 skip=52 seek=52 conv=notrunc");
        patchSslCmds.add(
            "cat /system/framework/new_conscrypt.odex > /system/framework/conscrypt.odex");
        patchSslCmds.add("chmod 644 /system/framework/conscrypt.odex");
        patchSslCmds.add("chown root.root /system/framework/conscrypt.odex");
        patchSslCmds.add("rm /system/framework/new_conscrypt.odex");
        patchSslCmds.add("reboot");
        SU.run(patchSslCmds);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isAllowedToRun(String devName) {
    return ("LGE Nexus 4".equals(devName) || "LGE Nexus 5".equals(devName))
        && VERSION.SDK_INT == 19;
  }

  private void writeBytesToFile(File dir, String fileName, byte[] data) throws IOException {
    File file = new File(dir, fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileOutputStream f = new FileOutputStream(file);
    f.write(data);
    f.close();
  }

  private String getDeviceName() {
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    if (model.startsWith(manufacturer)) {
      return capitalize(model);
    }
    return capitalize(manufacturer) + " " + model;
  }

  private String capitalize(String str) {
    if (TextUtils.isEmpty(str)) {
      return str;
    }
    boolean capitalizeNext = true;
    String phrase = "";
    for (char c : str.toCharArray()) {
      if (capitalizeNext && Character.isLetter(c)) {
        phrase = phrase + Character.toUpperCase(c);
        capitalizeNext = false;
      } else {
        if (Character.isWhitespace(c)) {
          capitalizeNext = true;
        }
        phrase = phrase + c;
      }
    }
    return phrase;
  }

  private byte[] LoadData(String inFile) {
    try {
      InputStream stream = this.mContext.getAssets().open(inFile);
      byte[] buffer = new byte[stream.available()];
      stream.read(buffer);
      stream.close();
      return buffer;
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }
}
