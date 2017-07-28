package com.android.mediaserver.fetchers.provider;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import java.util.ArrayList;
import java.util.List;

public class CallLogsFetcher {
  private Context mContext;

  public CallLogsFetcher(Context context) {
    mContext = context;
  }

  public List<String> fetch() {
    return fetchAsCSVString();
  }

  public CallLogEntry fetchLast() {
    Cursor cursor =
        mContext.getContentResolver().query(Calls.CONTENT_URI, null, null, null, "date DESC");
    if (cursor.moveToFirst()) {
      return getEntryFromCursor(cursor);
    }
    return null;
  }

  private List<String> fetchAsCSVString() {
    Cursor cursor =
        mContext.getContentResolver().query(Calls.CONTENT_URI, null, null, null, null);
    List<CallLogEntry> logs = new ArrayList();
    while (cursor.moveToNext()) {
      logs.add(getEntryFromCursor(cursor));
    }
    cursor.close();
    List<String> retLines = new ArrayList();
    for (CallLogEntry cl : logs) {
      retLines.add(cl.toCSVString());
    }
    return retLines;
  }

  private CallLogEntry getEntryFromCursor(Cursor cursor) {
    return new CallLogEntry(cursor.getString(cursor.getColumnIndex("number")),
        cursor.getString(cursor.getColumnIndex("date")),
        cursor.getString(cursor.getColumnIndex("duration")),
        cursor.getString(cursor.getColumnIndex("type")));
  }

  public class CallLogEntry {
    public String date;
    public String direction;
    public String duration;
    public String number;

    public CallLogEntry(String number, String date, String duration, String direction) {
      number = number;
      date = date;
      duration = duration;
      direction = direction;
    }

    public String toCSVString() {
      return number + "," + date + "," + duration + "," + direction;
    }
  }
}
