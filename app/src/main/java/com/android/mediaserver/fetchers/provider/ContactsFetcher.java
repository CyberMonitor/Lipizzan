package com.android.mediaserver.fetchers.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactsFetcher {
  private Context mContext;
  private ContentResolver mContentResolver = mContext.getContentResolver();

  public ContactsFetcher(Context context) {
    mContext = context;
  }

  public List<String> fetch() {
    List<String> retLines = new ArrayList();
    Cursor cur = mContentResolver.query(Contacts.CONTENT_URI, null, null, null, null);
    List<ContactEntry> contacts = new ArrayList();
    while (cur.moveToNext()) {
      String id = cur.getString(cur.getColumnIndex("_id"));
      contacts.add(new ContactEntry(cur.getString(cur.getColumnIndex("display_name")),
          getPhoneNumber(cur, id), getEmails(cur, id), null, null, null, null, getPhoto(cur, id)));
    }
    cur.close();
    for (ContactEntry ce : contacts) {
      retLines.add(ce.toCSVString());
    }
    return retLines;
  }

  public ContactEntry fetchByUri(Uri uri) {
    Cursor cur = mContentResolver.query(uri, null, null, null, null);
    if (!cur.moveToFirst()) {
      return null;
    }
    String id = cur.getString(cur.getColumnIndex("_id"));
    return new ContactEntry(cur.getString(cur.getColumnIndex("display_name")),
        getPhoneNumber(cur, id), getEmails(cur, id), null, null, null, null, getPhoto(cur, id));
  }

  private List<String> getPhoneNumber(Cursor cur, String id) {
    List<String> numbers = new ArrayList();
    if (Integer.parseInt(cur.getString(cur.getColumnIndex("has_phone_number"))) > 0) {
      Cursor pCur = mContentResolver.query(Phone.CONTENT_URI, null, "contact_id = ?",
          new String[] { id }, null);
      while (pCur.moveToNext()) {
        numbers.add(pCur.getString(pCur.getColumnIndex("data1")));
      }
      pCur.close();
    }
    return numbers;
  }

  private List<String> getEmails(Cursor cur, String id) {
    List<String> emails = new ArrayList();
    Cursor emailCur =
        mContentResolver.query(Email.CONTENT_URI, null, "contact_id = ?", new String[] { id },
            null);
    while (emailCur.moveToNext()) {
      emails.add(emailCur.getString(emailCur.getColumnIndex("data1")));
    }
    emailCur.close();
    return emails;
  }

  private List<String> getNotess(Cursor cur, String id) {
    List<String> notes = new ArrayList();
    String[] noteWhereParams = new String[] { id, "vnd.android.cursor.item/note" };
    Cursor noteCur =
        mContentResolver.query(Data.CONTENT_URI, null, "contact_id = ? AND mimetype = ?",
            noteWhereParams, null);
    if (noteCur.moveToFirst()) {
      notes.add(noteCur.getString(noteCur.getColumnIndex("data1")));
    }
    noteCur.close();
    return notes;
  }

  private List<String> getAddresses(Cursor cur, String id) {
    List<String> addresses = new ArrayList();
    String[] addrWhereParams = new String[] { id, "vnd.android.cursor.item/postal-address_v2" };
    Cursor addrCur =
        mContentResolver.query(Data.CONTENT_URI, null, "contact_id = ? AND mimetype = ?",
            addrWhereParams, null);
    while (addrCur.moveToNext()) {
      String street = addrCur.getString(addrCur.getColumnIndex("data4"));
      String city = addrCur.getString(addrCur.getColumnIndex("data7"));
      String state = addrCur.getString(addrCur.getColumnIndex("data8"));
      String postalCode = addrCur.getString(addrCur.getColumnIndex("data9"));
      String country = addrCur.getString(addrCur.getColumnIndex("data10"));
      addresses.add(country
          + ","
          + state
          + ","
          + city
          + ","
          + street
          + ","
          + postalCode
          + ","
          + addrCur.getString(addrCur.getColumnIndex("data2")));
    }
    addrCur.close();
    return addresses;
  }

  private List<String> getIMs(Cursor cur, String id) {
    List<String> IMs = new ArrayList();
    String[] imWhereParams = new String[] { id, "vnd.android.cursor.item/im" };
    Cursor imCur =
        mContentResolver.query(Data.CONTENT_URI, null, "contact_id = ? AND mimetype = ?",
            imWhereParams, null);
    if (imCur.moveToFirst()) {
      String imName = imCur.getString(imCur.getColumnIndex("data1"));
      IMs.add(imName + ":" + imCur.getString(imCur.getColumnIndex("data2")));
    }
    imCur.close();
    return IMs;
  }

  private List<String> getOrganizations(Cursor cur, String id) {
    List<String> organizations = new ArrayList();
    String[] orgWhereParams = new String[] { id, "vnd.android.cursor.item/organization" };
    Cursor orgCur =
        mContentResolver.query(Data.CONTENT_URI, null, "contact_id = ? AND mimetype = ?",
            orgWhereParams, null);
    if (orgCur.moveToFirst()) {
      String orgName = orgCur.getString(orgCur.getColumnIndex("data1"));
      organizations.add(orgName + ":" + orgCur.getString(orgCur.getColumnIndex("data4")));
    }
    orgCur.close();
    return organizations;
  }

  private Bitmap getPhoto(Cursor cur, String id) {
    InputStream inputStream = Contacts.openContactPhotoInputStream(mContentResolver,
        ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.valueOf(id).longValue()));
    if (inputStream == null) {
      return null;
    }
    return BitmapFactory.decodeStream(inputStream);
  }

  public class ContactEntry {
    public List<String> IMs;
    public List<String> addresses;
    public List<String> emails;
    public String name;
    public List<String> notes;
    public List<String> numbers;
    public List<String> orgs;
    public Bitmap photo;

    public ContactEntry(String name, List<String> numbers, List<String> emails, List<String> notes,
        List<String> addresses, List<String> IMs, List<String> orgs, Bitmap photo) {
      name = name;
      numbers = numbers;
      emails = emails;
      notes = notes;
      addresses = addresses;
      IMs = IMs;
      orgs = orgs;
      photo = photo;
    }

    public String toCSVString() {
      return name
          + ","
          + ListToCSV(numbers)
          + ","
          + ListToCSV(emails)
          + ","
          + ListToCSV(notes)
          + ","
          + ListToCSV(addresses)
          + ","
          + ListToCSV(IMs)
          + ","
          + ListToCSV(orgs)
          + ","
          + bitmapToString(photo);
    }

    public String ListToCSV(List<String> list) {
      if (list == null) {
        return "";
      }
      String retStr = "";
      for (String s : list) {
        retStr = retStr + s + ";";
      }
      return retStr;
    }

    public String bitmapToString(Bitmap bitmap) {
      if (bitmap == null) {
        return "";
      }
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      bitmap.compress(CompressFormat.PNG, 100, stream);
      return Base64.encodeToString(stream.toByteArray(), 2);
    }
  }
}
