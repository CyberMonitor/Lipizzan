package com.android.mediaserver.musicg.properties;

import com.google.android.gms.common.ConnectionResult;

public class FingerprintProperties {
  protected static FingerprintProperties instance = null;
  private int anchorPointsIntervalLength = 4;
  private int fps = 5;
  private int lowerBoundedFrequency = 400;
  private int maxTargetZoneDistance = 4;
  private int numAnchorPointsPerInterval = 10;
  private int numFilterBanks = 4;
  private int numRobustPointsPerFrame = 4;
  private int overlapFactor = 4;
  private int numFramesInOneSecond = (this.overlapFactor * this.fps);
  private int refMaxActivePairs = 1;
  private int sampleMaxActivePairs = 10;
  private int sampleSizePerFrame = 2048;
  private int sampleRate = (this.sampleSizePerFrame * this.fps);
  private int upperBoundedFrequency = ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED;
  private int numFrequencyUnits =
      ((((this.upperBoundedFrequency - this.lowerBoundedFrequency) + 1) / this.fps) + 1);

  public static FingerprintProperties getInstance() {
    if (instance == null) {
      synchronized (FingerprintProperties.class) {
        if (instance == null) {
          instance = new FingerprintProperties();
        }
      }
    }
    return instance;
  }

  public int getNumRobustPointsPerFrame() {
    return this.numRobustPointsPerFrame;
  }

  public int getSampleSizePerFrame() {
    return this.sampleSizePerFrame;
  }

  public int getOverlapFactor() {
    return this.overlapFactor;
  }

  public int getNumFilterBanks() {
    return this.numFilterBanks;
  }

  public int getUpperBoundedFrequency() {
    return this.upperBoundedFrequency;
  }

  public int getLowerBoundedFrequency() {
    return this.lowerBoundedFrequency;
  }

  public int getFps() {
    return this.fps;
  }

  public int getRefMaxActivePairs() {
    return this.refMaxActivePairs;
  }

  public int getSampleMaxActivePairs() {
    return this.sampleMaxActivePairs;
  }

  public int getNumAnchorPointsPerInterval() {
    return this.numAnchorPointsPerInterval;
  }

  public int getAnchorPointsIntervalLength() {
    return this.anchorPointsIntervalLength;
  }

  public int getMaxTargetZoneDistance() {
    return this.maxTargetZoneDistance;
  }

  public int getNumFrequencyUnits() {
    return this.numFrequencyUnits;
  }

  public int getMaxPossiblePairHashcode() {
    return (((this.maxTargetZoneDistance * this.numFrequencyUnits) * this.numFrequencyUnits)
        + (this.numFrequencyUnits * this.numFrequencyUnits)) + this.numFrequencyUnits;
  }

  public int getSampleRate() {
    return this.sampleRate;
  }

  public int getNumFramesInOneSecond() {
    return this.numFramesInOneSecond;
  }
}
