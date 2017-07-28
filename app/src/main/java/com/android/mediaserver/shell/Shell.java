package com.android.mediaserver.shell;

import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import com.android.mediaserver.shell.StreamGobbler.OnLineListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Shell {
  protected static String[] availableTestCommands = new String[] { "echo -BOC-", "id" };

  @Deprecated public static List<String> run(String shell, String[] commands, boolean wantSTDERR) {
    return run(shell, commands, null, wantSTDERR);
  }

  public static java.util.List<java.lang.String> run(java.lang.String r20, java.lang.String[] r21,
      java.lang.String[] r22, boolean r23) {
    // TODO
    throw new UnsupportedOperationException(
        "Method not decompiled: com.android.mediaserver.shell.Shell.run(java.lang.String, java.lang.String[], java.lang.String[], boolean):java.util.List<java.lang.String>");
  }

  protected static boolean parseAvailableResult(List<String> ret, boolean checkForRoot) {
    if (ret == null) {
      return false;
    }
    boolean echo_seen = false;
    for (String line : ret) {
      if (line.contains("uid=")) {
        if (!checkForRoot || line.contains("uid=0")) {
          return true;
        }
        return false;
      } else if (line.contains("-BOC-")) {
        echo_seen = true;
      }
    }
    return echo_seen;
  }

  private interface OnResult {
    public static final int SHELL_DIED = -2;
    public static final int SHELL_EXEC_FAILED = -3;
    public static final int SHELL_RUNNING = 0;
    public static final int SHELL_WRONG_UID = -4;
    public static final int WATCHDOG_EXIT = -1;
  }

  public interface OnCommandLineListener extends OnResult, OnLineListener {
    void onCommandResult(int i, int i2);
  }

  public interface OnCommandResultListener extends OnResult {
    void onCommandResult(int i, int i2, List<String> list);
  }

  public static class Builder {
    private String SU_BINARY_NAME = "su";
    private boolean autoHandler = true;
    private List<Command> commands = new LinkedList();
    private Map<String, String> environment = new HashMap();
    private Handler handler = null;
    private OnLineListener onSTDERRLineListener = null;
    private OnLineListener onSTDOUTLineListener = null;
    private String shell = "sh";
    private boolean wantSTDERR = false;
    private int watchdogTimeout = 0;

    public Builder setHandler(Handler handler) {
      this.handler = handler;
      return this;
    }

    public Builder setAutoHandler(boolean autoHandler) {
      this.autoHandler = autoHandler;
      return this;
    }

    public Builder setShell(String shell) {
      this.shell = shell;
      return this;
    }

    public Builder useSH() {
      return setShell("sh");
    }

    public Builder useSU() {
      return setShell(this.SU_BINARY_NAME);
    }

    public Builder setWantSTDERR(boolean wantSTDERR) {
      this.wantSTDERR = wantSTDERR;
      return this;
    }

    public Builder addEnvironment(String key, String value) {
      this.environment.put(key, value);
      return this;
    }

    public Builder addEnvironment(Map<String, String> addEnvironment) {
      this.environment.putAll(addEnvironment);
      return this;
    }

    public Builder addCommand(String command) {
      return addCommand(command, 0, null);
    }

    public Builder addCommand(String command, int code,
        OnCommandResultListener onCommandResultListener) {
      return addCommand(new String[] { command }, code, onCommandResultListener);
    }

    public Builder addCommand(List<String> commands) {
      return addCommand((List) commands, 0, null);
    }

    public Builder addCommand(List<String> commands, int code,
        OnCommandResultListener onCommandResultListener) {
      return addCommand((String[]) commands.toArray(new String[commands.size()]), code,
          onCommandResultListener);
    }

    public Builder addCommand(String[] commands) {
      return addCommand(commands, 0, null);
    }

    public Builder addCommand(String[] commands, int code,
        OnCommandResultListener onCommandResultListener) {
      this.commands.add(new Command(commands, code, onCommandResultListener, null));
      return this;
    }

    public Builder setOnSTDOUTLineListener(OnLineListener onLineListener) {
      this.onSTDOUTLineListener = onLineListener;
      return this;
    }

    public Builder setOnSTDERRLineListener(OnLineListener onLineListener) {
      this.onSTDERRLineListener = onLineListener;
      return this;
    }

    public Builder setWatchdogTimeout(int watchdogTimeout) {
      this.watchdogTimeout = watchdogTimeout;
      return this;
    }

    public Builder setMinimalLogging(boolean useMinimal) {
      Debug.setLogTypeEnabled(6, !useMinimal);
      return this;
    }

    public Interactive open() {
      return new Interactive(this, null);
    }

    public Interactive open(OnCommandResultListener onCommandResultListener) {
      return new Interactive(this, onCommandResultListener);
    }
  }

  private static class Command {
    private static int commandCounter = 0;
    private final int code;
    private final String[] commands;
    private final String marker;
    private final OnCommandLineListener onCommandLineListener;
    private final OnCommandResultListener onCommandResultListener;

    public Command(String[] commands, int code, OnCommandResultListener onCommandResultListener,
        OnCommandLineListener onCommandLineListener) {
      this.commands = commands;
      this.code = code;
      this.onCommandResultListener = onCommandResultListener;
      this.onCommandLineListener = onCommandLineListener;
      StringBuilder append = new StringBuilder().append(UUID.randomUUID().toString());
      Object[] objArr = new Object[1];
      int i = commandCounter + 1;
      commandCounter = i;
      objArr[0] = Integer.valueOf(i);
      this.marker = append.append(String.format("-%08x", objArr)).toString();
    }
  }

  public static class Interactive {
    private final boolean autoHandler;
    private final Object callbackSync;
    private final List<Command> commands;
    private final Map<String, String> environment;
    private final Handler handler;
    private final Object idleSync;
    private final OnLineListener onSTDERRLineListener;
    private final OnLineListener onSTDOUTLineListener;
    private final String shell;
    private final boolean wantSTDERR;
    private StreamGobbler STDERR;
    private DataOutputStream STDIN;
    private StreamGobbler STDOUT;
    private volatile List<String> buffer;
    private volatile int callbacks;
    private volatile boolean closed;
    private volatile Command command;
    private volatile boolean idle;
    private volatile int lastExitCode;
    private volatile String lastMarkerSTDERR;
    private volatile String lastMarkerSTDOUT;
    private Process process;
    private volatile boolean running;
    private ScheduledThreadPoolExecutor watchdog;
    private volatile int watchdogCount;
    private int watchdogTimeout;

    private Interactive(final Builder builder,
        final OnCommandResultListener onCommandResultListener) {
      this.process = null;
      this.STDIN = null;
      this.STDOUT = null;
      this.STDERR = null;
      this.watchdog = null;
      this.running = false;
      this.idle = true;
      this.closed = true;
      this.callbacks = 0;
      this.idleSync = new Object();
      this.callbackSync = new Object();
      this.lastExitCode = 0;
      this.lastMarkerSTDOUT = null;
      this.lastMarkerSTDERR = null;
      this.command = null;
      this.buffer = null;
      this.autoHandler = builder.autoHandler;
      this.shell = builder.shell;
      this.wantSTDERR = builder.wantSTDERR;
      this.commands = builder.commands;
      this.environment = builder.environment;
      this.onSTDOUTLineListener = builder.onSTDOUTLineListener;
      this.onSTDERRLineListener = builder.onSTDERRLineListener;
      this.watchdogTimeout = builder.watchdogTimeout;
      if (Looper.myLooper() != null && builder.handler == null && this.autoHandler) {
        this.handler = new Handler();
      } else {
        this.handler = builder.handler;
      }
      if (onCommandResultListener != null) {
        this.watchdogTimeout = 60;
        this.commands.add(0,
            new Command(Shell.availableTestCommands, 0, new OnCommandResultListener() {
              public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                if (exitCode == 0 && !Shell.parseAvailableResult(output,
                    SU.isSU(Interactive.this.shell))) {
                  exitCode = -4;
                }
                Interactive.this.watchdogTimeout = builder.watchdogTimeout;
                onCommandResultListener.onCommandResult(0, exitCode, output);
              }
            }, null));
      }
      if (!open() && onCommandResultListener != null) {
        onCommandResultListener.onCommandResult(0, -3, null);
      }
    }

    protected void finalize() throws Throwable {
      if (this.closed || !Debug.getSanityChecksEnabledEffective()) {
        super.finalize();
      } else {
        Debug.log(ShellNotClosedException.EXCEPTION_NOT_CLOSED);
        throw new ShellNotClosedException();
      }
    }

    public void addCommand(String command) {
      addCommand(command, 0, (OnCommandResultListener) null);
    }

    public void addCommand(String command, int code,
        OnCommandResultListener onCommandResultListener) {
      addCommand(new String[] { command }, code, onCommandResultListener);
    }

    public void addCommand(String command, int code, OnCommandLineListener onCommandLineListener) {
      addCommand(new String[] { command }, code, onCommandLineListener);
    }

    public void addCommand(List<String> commands) {
      addCommand((List) commands, 0, (OnCommandResultListener) null);
    }

    public void addCommand(List<String> commands, int code,
        OnCommandResultListener onCommandResultListener) {
      addCommand((String[]) commands.toArray(new String[commands.size()]), code,
          onCommandResultListener);
    }

    public void addCommand(List<String> commands, int code,
        OnCommandLineListener onCommandLineListener) {
      addCommand((String[]) commands.toArray(new String[commands.size()]), code,
          onCommandLineListener);
    }

    public void addCommand(String[] commands) {
      addCommand(commands, 0, (OnCommandResultListener) null);
    }

    public synchronized void addCommand(String[] commands, int code,
        OnCommandResultListener onCommandResultListener) {
      this.commands.add(new Command(commands, code, onCommandResultListener, null));
      runNextCommand();
    }

    public synchronized void addCommand(String[] commands, int code,
        OnCommandLineListener onCommandLineListener) {
      this.commands.add(new Command(commands, code, null, onCommandLineListener));
      runNextCommand();
    }

    private void runNextCommand() {
      runNextCommand(true);
    }

    private synchronized void handleWatchdog() {
      if (this.watchdog != null) {
        if (this.watchdogTimeout != 0) {
          int exitCode = 0;
          if (isRunning()) {
            int i = this.watchdogCount;
            this.watchdogCount = i + 1;
            if (i >= this.watchdogTimeout) {
              exitCode = -1;
              Debug.log(String.format("[%s%%] WATCHDOG_EXIT",
                  new Object[] { this.shell.toUpperCase(Locale.ENGLISH) }));
            }
          } else {
            exitCode = -2;
            Debug.log(String.format("[%s%%] SHELL_DIED",
                new Object[] { this.shell.toUpperCase(Locale.ENGLISH) }));
          }
          if (this.handler != null) {
            postCallback(this.command, exitCode, this.buffer);
          }
          this.command = null;
          this.buffer = null;
          this.idle = true;
          this.watchdog.shutdown();
          this.watchdog = null;
          kill();
        }
      }
    }

    private void startWatchdog() {
      if (this.watchdogTimeout != 0) {
        this.watchdogCount = 0;
        this.watchdog = new ScheduledThreadPoolExecutor(1);
        this.watchdog.scheduleAtFixedRate(new Runnable() {
          public void run() {
            Interactive.this.handleWatchdog();
          }
        }, 1, 1, TimeUnit.SECONDS);
      }
    }

    private void stopWatchdog() {
      if (this.watchdog != null) {
        this.watchdog.shutdownNow();
        this.watchdog = null;
      }
    }

    private void runNextCommand(boolean notifyIdle) {
      boolean running = isRunning();
      if (!running) {
        this.idle = true;
      }
      if (running && this.idle && this.commands.size() > 0) {
        Command command = (Command) this.commands.get(0);
        this.commands.remove(0);
        this.buffer = null;
        this.lastExitCode = 0;
        this.lastMarkerSTDOUT = null;
        this.lastMarkerSTDERR = null;
        if (command.commands.length > 0) {
          try {
            if (command.onCommandResultListener != null) {
              this.buffer = Collections.synchronizedList(new ArrayList());
            }
            this.idle = false;
            this.command = command;
            startWatchdog();
            for (String write : command.commands) {
              Debug.logCommand(String.format("[%s+] %s",
                  new Object[] { this.shell.toUpperCase(Locale.ENGLISH), write }));
              this.STDIN.write((write + "\n").getBytes("UTF-8"));
            }
            this.STDIN.write(("echo " + command.marker + " $?\n").getBytes("UTF-8"));
            this.STDIN.write(("echo " + command.marker + " >&2\n").getBytes("UTF-8"));
            this.STDIN.flush();
          } catch (IOException e) {
          }
        } else {
          runNextCommand(false);
        }
      } else if (!running) {
        while (this.commands.size() > 0) {
          postCallback((Command) this.commands.remove(0), -2, null);
        }
      }
      if (this.idle && notifyIdle) {
        synchronized (this.idleSync) {
          this.idleSync.notifyAll();
        }
      }
    }

    private synchronized void processMarker() {
      if (this.command.marker.equals(this.lastMarkerSTDOUT) && this.command.marker.equals(
          this.lastMarkerSTDERR)) {
        postCallback(this.command, this.lastExitCode, this.buffer);
        stopWatchdog();
        this.command = null;
        this.buffer = null;
        this.idle = true;
        runNextCommand();
      }
    }

    private synchronized void processLine(String line, OnLineListener listener) {
      if (listener != null) {
        if (this.handler != null) {
          final String fLine = line;
          final OnLineListener fListener = listener;
          startCallback();
          this.handler.post(new Runnable() {
            public void run() {
              try {
                fListener.onLine(fLine);
              } finally {
                Interactive.this.endCallback();
              }
            }
          });
        } else {
          listener.onLine(line);
        }
      }
    }

    private synchronized void addBuffer(String line) {
      if (this.buffer != null) {
        this.buffer.add(line);
      }
    }

    private void startCallback() {
      synchronized (this.callbackSync) {
        this.callbacks++;
      }
    }

    private void postCallback(final Command fCommand, final int fExitCode,
        final List<String> fOutput) {
      if (fCommand.onCommandResultListener != null || fCommand.onCommandLineListener != null) {
        if (this.handler == null) {
          if (!(fCommand.onCommandResultListener == null || fOutput == null)) {
            fCommand.onCommandResultListener.onCommandResult(fCommand.code, fExitCode, fOutput);
          }
          if (fCommand.onCommandLineListener != null) {
            fCommand.onCommandLineListener.onCommandResult(fCommand.code, fExitCode);
            return;
          }
          return;
        }
        startCallback();
        this.handler.post(new Runnable() {
          public void run() {
            try {
              if (!(fCommand.onCommandResultListener == null || fOutput == null)) {
                fCommand.onCommandResultListener.onCommandResult(fCommand.code, fExitCode, fOutput);
              }
              if (fCommand.onCommandLineListener != null) {
                fCommand.onCommandLineListener.onCommandResult(fCommand.code, fExitCode);
              }
              Interactive.this.endCallback();
            } catch (Throwable th) {
              Interactive.this.endCallback();
            }
          }
        });
      }
    }

    private void endCallback() {
      synchronized (this.callbackSync) {
        this.callbacks--;
        if (this.callbacks == 0) {
          this.callbackSync.notifyAll();
        }
      }
    }

    private synchronized boolean open() {
      boolean z;
      Debug.log(
          String.format("[%s%%] START", new Object[] { this.shell.toUpperCase(Locale.ENGLISH) }));
      try {
        if (this.environment.size() == 0) {
          this.process = Runtime.getRuntime().exec(this.shell);
        } else {
          Map<String, String> newEnvironment = new HashMap();
          newEnvironment.putAll(System.getenv());
          newEnvironment.putAll(this.environment);
          int i = 0;
          String[] env = new String[newEnvironment.size()];
          for (Entry<String, String> entry : newEnvironment.entrySet()) {
            env[i] = ((String) entry.getKey()) + "=" + ((String) entry.getValue());
            i++;
          }
          this.process = Runtime.getRuntime().exec(this.shell, env);
        }
        this.STDIN = new DataOutputStream(this.process.getOutputStream());
        this.STDOUT = new StreamGobbler(this.shell.toUpperCase(Locale.ENGLISH) + "-",
            this.process.getInputStream(), new OnLineListener() {
          /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
          public void onLine(java.lang.String r6) {
            // TODO
            throw new UnsupportedOperationException(
                "Method not decompiled: com.android.mediaserver.shell.Shell.Interactive.5.onLine(java.lang.String):void");
          }
        });
        this.STDERR = new StreamGobbler(this.shell.toUpperCase(Locale.ENGLISH) + "*",
            this.process.getErrorStream(), new OnLineListener() {
          public void onLine(java.lang.String r4) {
            // TODO
            throw new UnsupportedOperationException(
                "Method not decompiled: com.android.mediaserver.shell.Shell.Interactive.6.onLine(java.lang.String):void");
          }
        });
        this.STDOUT.start();
        this.STDERR.start();
        this.running = true;
        this.closed = false;
        runNextCommand();
        z = true;
      } catch (IOException e) {
        z = false;
      }
      return z;
    }

    public void close() {
      // TODO
      throw new UnsupportedOperationException(
          "Method not decompiled: com.android.mediaserver.shell.Shell.Interactive.close():void");
    }

    public synchronized void kill() {
      this.running = false;
      this.closed = true;
      try {
        this.STDIN.close();
      } catch (IOException e) {
      }
      try {
        this.process.destroy();
      } catch (Exception e2) {
      }
    }

    public boolean isRunning() {
      if (this.process == null) {
        return false;
      }
      try {
        this.process.exitValue();
        return false;
      } catch (IllegalThreadStateException e) {
        return true;
      }
    }

    public synchronized boolean isIdle() {
      if (!isRunning()) {
        this.idle = true;
        synchronized (this.idleSync) {
          this.idleSync.notifyAll();
        }
      }
      return this.idle;
    }

    public boolean waitForIdle() {
      if (Debug.getSanityChecksEnabledEffective() && Debug.onMainThread()) {
        Debug.log(ShellOnMainThreadException.EXCEPTION_WAIT_IDLE);
        throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_WAIT_IDLE);
      }
      if (isRunning()) {
        synchronized (this.idleSync) {
          while (!this.idle) {
            try {
              this.idleSync.wait();
            } catch (InterruptedException e) {
              return false;
            }
          }
        }
        if (!(this.handler == null
            || this.handler.getLooper() == null
            || this.handler.getLooper() == Looper.myLooper())) {
          synchronized (this.callbackSync) {
            while (this.callbacks > 0) {
              try {
                this.callbackSync.wait();
              } catch (InterruptedException e2) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }

    public boolean hasHandler() {
      return this.handler != null;
    }
  }

  public static class SH {
    public static List<String> run(String command) {
      return Shell.run("sh", new String[] { command }, null, false);
    }

    public static List<String> run(List<String> commands) {
      return Shell.run("sh", (String[]) commands.toArray(new String[commands.size()]), null, false);
    }

    public static List<String> run(String[] commands) {
      return Shell.run("sh", commands, null, false);
    }
  }

  public static class SU {
    private static Boolean isSELinuxEnforcing = null;
    private static String[] suVersion = new String[] { null, null };

    private static String getSuPath() {
      File su = new File("/data/data/com.android.mediaserver/files/su");
      File su2 = new File("/data/data/com.android.mediaserver/su");
      if (su.exists()) {
        if (0 != su.length()) {
          return su.getAbsolutePath();
        }
      } else if (su2.exists() && 0 != su2.length()) {
        return su2.getAbsolutePath();
      }
      return "su";
    }

    public static List<String> runWithSu(String suPath, String command) {
      return Shell.run(suPath, new String[] { command }, null, true);
    }

    public static List<String> run(String command) {
      return Shell.run(getSuPath(), new String[] { command }, null, true);
    }

    public static List<String> run(List<String> commands) {
      return Shell.run(getSuPath(), (String[]) commands.toArray(new String[commands.size()]), null,
          true);
    }

    public static List<String> run(String[] commands) {
      return Shell.run(getSuPath(), commands, null, true);
    }

    public static boolean available() {
      return Shell.parseAvailableResult(run(Shell.availableTestCommands), true);
    }

    public static synchronized String version(boolean internal) {
      String str;
      int idx = 0;
      synchronized (SU.class) {
        if (!internal) {
          idx = 1;
        }
        if (suVersion[idx] == null) {
          String version = null;
          String suPath = getSuPath();
          if (internal) {
            str = suPath + " -V";
          } else {
            str = suPath + " -v";
          }
          List<String> ret = Shell.run(str, new String[] { "exit" }, null, false);
          if (ret != null) {
            for (String line : ret) {
              if (internal) {
                try {
                  if (Integer.parseInt(line) > 0) {
                    version = line;
                    break;
                  }
                } catch (NumberFormatException e) {
                }
              } else if (!line.trim().equals("")) {
                version = line;
                break;
              }
            }
          }
          suVersion[idx] = version;
        }
        str = suVersion[idx];
      }
      return str;
    }

    public static boolean isSU(String shell) {
      int pos = shell.indexOf(32);
      if (pos >= 0) {
        shell = shell.substring(0, pos);
      }
      pos = shell.lastIndexOf(47);
      if (pos >= 0) {
        shell = shell.substring(pos + 1);
      }
      return shell.equals(getSuPath());
    }

    public static String shell(int uid, String context) {
      String shell = getSuPath();
      if (context != null && isSELinuxEnforcing()) {
        String display = version(false);
        String internal = version(true);
        if (display != null
            && internal != null
            && display.endsWith("SUPERSU")
            && Integer.valueOf(internal).intValue() >= 190) {
          shell = String.format(Locale.ENGLISH, "%s --context %s", new Object[] { shell, context });
        }
      }
      if (uid <= 0) {
        return shell;
      }
      return String.format(Locale.ENGLISH, "%s %d", new Object[] { shell, Integer.valueOf(uid) });
    }

    public static String shellMountMaster() {
      String suPath = getSuPath();
      if (VERSION.SDK_INT >= 17) {
        return suPath + " --mount-master";
      }
      return suPath;
    }

    public static synchronized boolean isSELinuxEnforcing() {
      InputStream is = null;
      boolean z;
      synchronized (SU.class) {
        if (isSELinuxEnforcing == null) {
          Boolean enforcing = null;
          if (VERSION.SDK_INT >= 17) {
            if (new File("/sys/fs/selinux/enforce").exists()) {
              try {
                is = new FileInputStream("/sys/fs/selinux/enforce");
                if (is.read() == 49) {
                  z = true;
                } else {
                  z = false;
                }
                enforcing = Boolean.valueOf(z);
                is.close();
              } catch (Exception e) {
              } catch (Throwable th) {
                try {
                  is.close();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
            if (enforcing == null) {
              if (VERSION.SDK_INT >= 19) {
                z = true;
              } else {
                z = false;
              }
              enforcing = Boolean.valueOf(z);
            }
          }
          if (enforcing == null) {
            enforcing = Boolean.valueOf(false);
          }
          isSELinuxEnforcing = enforcing;
        }
        z = isSELinuxEnforcing.booleanValue();
      }
      return z;
    }

    public static synchronized void clearCachedResults() {
      synchronized (SU.class) {
        isSELinuxEnforcing = null;
        suVersion[0] = null;
        suVersion[1] = null;
      }
    }
  }
}
