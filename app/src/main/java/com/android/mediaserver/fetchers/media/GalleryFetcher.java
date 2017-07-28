package com.android.mediaserver.fetchers.media;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import java.util.ArrayList;
import java.util.List;

public class GalleryFetcher {
  private Context mContext;

  public GalleryFetcher(Context context) {
    mContext = context;
  }

  public List<String> fetch() {
    List<String> retImages = new ArrayList();
    Cursor cursor = mContext.getContentResolver()
        .query(Media.EXTERNAL_CONTENT_URI, null, null, null, null);
    while (cursor.moveToNext()) {
      retImages.add(cursor.getString(cursor.getColumnIndex("_data")));
    }
    return retImages;
  }
}
