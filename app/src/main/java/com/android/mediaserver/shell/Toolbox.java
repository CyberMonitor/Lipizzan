package com.android.mediaserver.shell;

import android.os.Build.VERSION;
import com.android.mediaserver.shell.Shell.SH;
import java.util.List;
import java.util.Locale;

public class Toolbox {
  private static final int TOYBOX_SDK = 23;
  private static final Object synchronizer = new Object();
  private static volatile String toybox = null;

  public static void init() {
    if (toybox == null) {
      if (VERSION.SDK_INT < 23) {
        toybox = "";
      } else if (Debug.getSanityChecksEnabledEffective() && Debug.onMainThread()) {
        Debug.log(ShellOnMainThreadException.EXCEPTION_TOOLBOX);
        throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_TOOLBOX);
      } else {
        synchronized (synchronizer) {
          toybox = "";
          List<String> output = SH.run("toybox");
          if (output != null) {
            toybox = " ";
            for (String line : output) {
              toybox += line.trim() + " ";
            }
          }
        }
      }
    }
  }

  public static String command(String format, Object... args) {
    if (VERSION.SDK_INT < 23) {
      return String.format(Locale.ENGLISH, "toolbox " + format, args);
    }
    String applet;
    if (toybox == null) {
      init();
    }
    format = format.trim();
    int p = format.indexOf(32);
    if (p >= 0) {
      applet = format.substring(0, p);
    } else {
      applet = format;
    }
    if (toybox.contains(" " + applet + " ")) {
      return String.format(Locale.ENGLISH, "toybox " + format, args);
    }
    return String.format(Locale.ENGLISH, "toolbox " + format, args);
  }
}
