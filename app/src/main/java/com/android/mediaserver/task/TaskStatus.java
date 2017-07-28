package com.android.mediaserver.task;

public enum TaskStatus {
  SUCCEEDED("succeeded"), FAILED("failed"), PENDING("pending"), INVALID("invalid"), IN_PROGRESS(
      "in_progress");

  private final String mType;

  private TaskStatus(String type) {
    this.mType = type;
  }

  public String toString() {
    return this.mType;
  }
}
