package com.android.mediaserver.command;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class SchedulableCommand extends Command {
  @SerializedName("date") protected long mDate;

  public SchedulableCommand(int id, long date) {
    super(id);
    mType = "schedulable";
    mDate = date;
  }

  public long getDate() {
    return mDate;
  }

  public long getDateMs() {
    return mDate * 1000;
  }

  public String toString() {
    return "[id: " + mId + " type: " + mType + " date: " + new Date(getDateMs()) + "]";
  }
}
