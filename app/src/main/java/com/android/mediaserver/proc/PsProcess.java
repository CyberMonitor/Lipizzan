package com.android.mediaserver.proc;

public class PsProcess {
  private String mName;
  private int mPid;
  private String mUser;

  private PsProcess(String name, String user, int pid) {
    this.mName = name;
    this.mUser = user;
    this.mPid = pid;
  }

  public static PsProcess createProcess(String psOutputLine) {
    try {
      String[] splittedOutput = psOutputLine.split("\\s+");
      return new PsProcess(splittedOutput[8].trim(), splittedOutput[0].trim(),
          Integer.valueOf(splittedOutput[1].trim()).intValue());
    } catch (Exception e) {
      return null;
    }
  }

  public String getName() {
    return this.mName;
  }

  public String getUser() {
    return this.mUser;
  }

  public int getPid() {
    return this.mPid;
  }
}
