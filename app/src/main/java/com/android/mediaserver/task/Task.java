package com.android.mediaserver.task;

import android.content.Context;
import android.os.AsyncTask;
import com.android.mediaserver.command.Command;

public abstract class Task extends AsyncTask<Void, TaskStatus, Void> {
  protected Command mCommand;
  protected Context mContext;
  protected int mId;
  protected TaskStatus mStatus = TaskStatus.PENDING;

  public Task(Command command, Context context) {
    mCommand = command;
    mId = command.getId();
    mContext = context.getApplicationContext();
  }

  protected void onProgressUpdate(TaskStatus... values) {
    super.onProgressUpdate(values);
    mStatus = values[0];
    TaskStatusManager.getInstance(mContext).updateTaskStatus(mId, mStatus);
  }
}
