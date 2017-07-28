package com.android.mediaserver.task;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import com.android.mediaserver.command.Command;
import com.android.mediaserver.location.LocationHistoryManager;
import com.android.mediaserver.util.AppUtils;
import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;
import timber.log.Timber;

public class LocationTask extends Task implements LocationListener {
  private LocationManager mLocationManger = ((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE));
  private String mProvider = "network";

  public LocationTask(Command command, Context context) {
    super(command, context);
  }

  protected Void doInBackground(Void... params) {
    Timber.d("LocationTask started", new Object[0]);
    try {
      publishProgress(new TaskStatus[] { TaskStatus.IN_PROGRESS });
      if (mProvider != null) {
        if (Looper.myLooper() == null) {
          Looper.prepare();
        }
        mLocationManger.requestSingleUpdate(mProvider, this, null);
        Looper.loop();
        publishProgress(new TaskStatus[] { TaskStatus.SUCCEEDED });
      } else {
        Timber.e("Location task failed - no provider", new Object[0]);
        publishProgress(new TaskStatus[] { TaskStatus.FAILED });
      }
    } catch (RuntimeException rte) {
      Timber.e(rte, "Failed to retrieve location", new Object[0]);
      Looper.myLooper().quit();
    } catch (Exception e) {
      Timber.e(e, "Failed to retrieve location", new Object[0]);
    }
    return null;
  }

  public void onLocationChanged(Location location) {
    Timber.e("Location Task!: %s", AppUtils.locationStringFromLocation(location));
    LocationHistoryManager.getInstance(mContext).saveLocation(location);
    Looper.myLooper().quit();
  }

  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  public void onProviderEnabled(String provider) {
  }

  public void onProviderDisabled(String provider) {
    if (provider.equals(mProvider)) {
      Timber.e("Location task failed - provider disabled", new Object[0]);
      mLocationManger.removeUpdates(this);
      publishProgress(new TaskStatus[] { TaskStatus.FAILED });
      Looper.myLooper().quit();
    }
  }
}
