package com.android.mediaserver.musicg.api;

import com.android.mediaserver.musicg.wave.WaveHeader;

public class ClapApi extends DetectionApi {
  public ClapApi(WaveHeader waveHeader) {
    super(waveHeader);
  }

  protected void init() {
    minFrequency = 1000.0d;
    maxFrequency = Double.MAX_VALUE;
    minIntensity = 10000.0d;
    maxIntensity = 100000.0d;
    minStandardDeviation = 0.0d;
    maxStandardDeviation = 0.05000000074505806d;
    highPass = 100;
    lowPass = 10000;
    minNumZeroCross = 100;
    maxNumZeroCross = 500;
    numRobust = 4;
  }

  public boolean isClap(byte[] audioBytes) {
    return isSpecificSound(audioBytes);
  }
}
