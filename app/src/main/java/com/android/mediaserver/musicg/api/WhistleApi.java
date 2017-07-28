package com.android.mediaserver.musicg.api;

import com.android.mediaserver.musicg.wave.WaveHeader;

public class WhistleApi extends DetectionApi {
  public WhistleApi(WaveHeader waveHeader) {
    super(waveHeader);
  }

  protected void init() {
    this.minFrequency = 600.0d;
    this.maxFrequency = Double.MAX_VALUE;
    this.minIntensity = 100.0d;
    this.maxIntensity = 100000.0d;
    this.minStandardDeviation = 0.10000000149011612d;
    this.maxStandardDeviation = 1.0d;
    this.highPass = 100;
    this.lowPass = 10000;
    this.minNumZeroCross = 50;
    this.maxNumZeroCross = 200;
    this.numRobust = 10;
  }

  public boolean isWhistle(byte[] audioBytes) {
    return isSpecificSound(audioBytes);
  }
}
