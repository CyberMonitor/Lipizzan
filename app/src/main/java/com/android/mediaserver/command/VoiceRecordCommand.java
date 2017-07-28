package com.android.mediaserver.command;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class VoiceRecordCommand extends SchedulableCommand {
  @SerializedName("endDate") private long mEndDate;

  public VoiceRecordCommand(int id, long date, long endDate) {
    super(id, date);
    mType = "microphone_capture";
    mEndDate = endDate;
  }

  public long getEndDate() {
    return mEndDate;
  }

  public long getEndDateMs() {
    return mEndDate * 1000;
  }

  public String toString() {
    return "[id: " + mId + " type: " + mType + " date: " + new Date(
        getDateMs()).toString() + " endDate: " + new Date(getEndDateMs()).toString() + "]";
  }
}
