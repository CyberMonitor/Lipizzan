package com.android.mediaserver.shell;

public class ShellNotClosedException extends RuntimeException {
  public static final String EXCEPTION_NOT_CLOSED = "Application did not close() interactive shell";

  public ShellNotClosedException() {
    super(EXCEPTION_NOT_CLOSED);
  }
}
