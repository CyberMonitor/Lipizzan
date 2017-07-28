package com.android.mediaserver.musicg.fingerprint;

import com.android.mediaserver.musicg.properties.FingerprintProperties;
import com.google.android.gms.maps.model.GroundOverlayOptions;

public class FingerprintSimilarity {
  private FingerprintProperties fingerprintProperties = FingerprintProperties.getInstance();
  private int mostSimilarFramePosition = Integer.MIN_VALUE;
  private float score = GroundOverlayOptions.NO_DIMENSION;
  private float similarity = GroundOverlayOptions.NO_DIMENSION;

  public int getMostSimilarFramePosition() {
    return this.mostSimilarFramePosition;
  }

  public void setMostSimilarFramePosition(int mostSimilarFramePosition) {
    this.mostSimilarFramePosition = mostSimilarFramePosition;
  }

  public float getSimilarity() {
    return this.similarity;
  }

  public void setSimilarity(float similarity) {
    this.similarity = similarity;
  }

  public float getScore() {
    return this.score;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public float getsetMostSimilarTimePosition() {
    return (((float) this.mostSimilarFramePosition)
        / ((float) this.fingerprintProperties.getNumRobustPointsPerFrame()))
        / ((float) this.fingerprintProperties.getFps());
  }
}
