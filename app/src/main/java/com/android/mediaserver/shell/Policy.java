package com.android.mediaserver.shell;

import com.android.mediaserver.shell.Shell.SU;
import java.util.ArrayList;
import java.util.List;

public abstract class Policy {
  private static final int MAX_POLICY_LENGTH = 4064;
  private static final Object synchronizer = new Object();
  private static volatile Boolean canInject = null;
  private static volatile boolean injected = false;

  public static boolean haveInjected() {
    return injected;
  }

  public static void resetInjected() {
    synchronized (synchronizer) {
      injected = false;
    }
  }

  public static boolean canInject() {
    boolean booleanValue;
    synchronized (synchronizer) {
      if (canInject != null) {
        booleanValue = canInject.booleanValue();
      } else {
        canInject = Boolean.valueOf(false);
        List<String> result = Shell.run("sh", new String[] { "supolicy" }, null, false);
        if (result != null) {
          for (String line : result) {
            if (line.contains("supolicy")) {
              canInject = Boolean.valueOf(true);
              break;
            }
          }
        }
        booleanValue = canInject.booleanValue();
      }
    }
    return booleanValue;
  }

  public static void resetCanInject() {
    synchronized (synchronizer) {
      canInject = null;
    }
  }

  protected abstract String[] getPolicies();

  public void inject() {
    synchronized (synchronizer) {
      if (!SU.isSELinuxEnforcing()) {
      } else if (!canInject()) {
      } else if (injected) {
      } else {
        String[] policies = getPolicies();
        if (policies != null && policies.length > 0) {
          List commands = new ArrayList();
          String command = "";
          for (String policy : policies) {
            if (command.length() == 0
                || (command.length() + policy.length()) + 3 < MAX_POLICY_LENGTH) {
              command = command + " \"" + policy + "\"";
            } else {
              commands.add("supolicy --live" + command);
              command = "";
            }
          }
          if (command.length() > 0) {
            commands.add("supolicy --live" + command);
          }
          if (commands.size() > 0) {
            SU.run(commands);
          }
        }
        injected = true;
      }
    }
  }
}
