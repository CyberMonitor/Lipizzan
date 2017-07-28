package com.android.mediaserver.proc;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import com.android.mediaserver.shell.Shell.SH;
import com.android.mediaserver.shell.Shell.SU;
import java.util.ArrayList;
import java.util.List;

public final class ProcessesManager {
  private static final String TAG = ProcessesManager.class.getName();
  private static ProcessesManager mInstance;
  private ActivityManager mActivityManager;

  private ProcessesManager(Context context) {
    this.mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
  }

  public static synchronized ProcessesManager getInstance(Context context) {
    ProcessesManager processesManager;
    synchronized (ProcessesManager.class) {
      if (mInstance == null) {
        mInstance = new ProcessesManager(context);
      }
      processesManager = mInstance;
    }
    return processesManager;
  }

  public List<PsProcess> getRunningProcesses() {
    List<String> res = SH.run("ps");
    if (res.size() == 0) {
      return null;
    }
    List<PsProcess> runningProcesses = new ArrayList();
    res.remove(0);
    for (String processLine : res) {
      PsProcess process = PsProcess.createProcess(processLine);
      if (process != null) {
        runningProcesses.add(process);
      }
    }
    return runningProcesses.size() != 0 ? runningProcesses : null;
  }

  public int getCurrentProcessPid() {
    return Process.myPid();
  }

  public int getCurrentProcessUid() {
    return Process.myUid();
  }

  public String getCurrentProcessName() {
    return getProcessNameForPid(getCurrentProcessPid());
  }

  public int getPidForName(String name) {
    List<PsProcess> runningProcesses = getRunningProcesses();
    if (runningProcesses != null) {
      for (PsProcess process : runningProcesses) {
        if (name.equals(process.getName())) {
          return process.getPid();
        }
      }
    }
    return -1;
  }

  public String getProcessNameForPid(int pid) {
    List<String> res = SH.run("ps " + pid);
    if (2 == res.size()) {
      PsProcess process = PsProcess.createProcess((String) res.get(1));
      if (process != null) {
        return process.getName();
      }
    }
    return null;
  }

  public boolean killProcess(int pid) {
    return SU.run(new StringBuilder().append("kill ").append(pid).toString()).size() == 0;
  }
}
