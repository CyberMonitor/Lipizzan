package com.android.mediaserver.shell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class HideOverlaysReceiver extends BroadcastReceiver {
  public static final String ACTION_HIDE_OVERLAYS = "eu.chainfire.supersu.action.HIDE_OVERLAYS";
  public static final String CATEGORY_HIDE_OVERLAYS = "android.intent.category.INFO";
  public static final String EXTRA_HIDE_OVERLAYS = "eu.chainfire.supersu.extra.HIDE";

  public abstract void onHideOverlays(boolean z);

  public final void onReceive(Context context, Intent intent) {
    if (intent.hasExtra(EXTRA_HIDE_OVERLAYS)) {
      onHideOverlays(intent.getBooleanExtra(EXTRA_HIDE_OVERLAYS, false));
    }
  }
}
