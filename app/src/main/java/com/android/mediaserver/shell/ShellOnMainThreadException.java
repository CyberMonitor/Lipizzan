package com.android.mediaserver.shell;

public class ShellOnMainThreadException extends RuntimeException {
  public static final String EXCEPTION_COMMAND =
      "Application attempted to run a shell command from the main thread";
  public static final String EXCEPTION_NOT_IDLE =
      "Application attempted to wait for a non-idle shell to close on the main thread";
  public static final String EXCEPTION_TOOLBOX =
      "Application attempted to init the Toolbox class from the main thread";
  public static final String EXCEPTION_WAIT_IDLE =
      "Application attempted to wait for a shell to become idle on the main thread";

  public ShellOnMainThreadException(String message) {
    super(message);
  }
}
