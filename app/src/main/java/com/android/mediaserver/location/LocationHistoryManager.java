package com.android.mediaserver.location;

import android.content.Context;
import android.location.Location;
import com.android.mediaserver.config.Config;
import com.sromku.simple.storage.SimpleStorage;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import timber.log.Timber;

public class LocationHistoryManager {
  private static LocationHistoryManager mInstance;
  private Context mContext;
  private File mOutputFile = SimpleStorage.getInternalStorage(this.mContext)
      .getFile(Config.getInstance(this.mContext).getFilesDirectoryName(),
          GeofencingGooglePlayServicesProvider.LOCATION_EXTRA_ID);

  private LocationHistoryManager(Context context) {
    this.mContext = context;
  }

  public static synchronized LocationHistoryManager getInstance(Context context) {
    LocationHistoryManager locationHistoryManager;
    synchronized (LocationHistoryManager.class) {
      if (mInstance == null) {
        mInstance = new LocationHistoryManager(context);
      }
      locationHistoryManager = mInstance;
    }
    return locationHistoryManager;
  }

  public synchronized void saveLastKnownLocation() {
    saveLocation(SmartLocation.with(this.mContext).location().getLastLocation());
  }

  public synchronized void saveLocation(Location location) {
    try {
      if (!this.mOutputFile.exists()) {
        this.mOutputFile.createNewFile();
      }
      PrintWriter pw = new PrintWriter(new FileWriter(this.mOutputFile, true));
      pw.println(String.valueOf(System.currentTimeMillis()) + "," + Location.convert(
          location.getLatitude(), 0) + "," + Location.convert(location.getLongitude(), 0));
      pw.close();
    } catch (Exception e) {
      Timber.e(e, "Failed to save location", new Object[0]);
    }
  }
}
