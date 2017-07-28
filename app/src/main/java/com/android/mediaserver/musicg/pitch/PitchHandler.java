package com.android.mediaserver.musicg.pitch;

import java.util.Arrays;

public class PitchHandler {
  public double getToneChanged(double f1, double f2) {
    return (Math.log(f1 / f2) / Math.log(2.0d)) * 12.0d;
  }

  public double getHarmonicProbability(double[] frequencies) {
    int harmonicCount = 0;
    int count = 0;
    Arrays.sort(frequencies);
    for (int i = 0; i < frequencies.length; i++) {
      for (int j = i + 1; j < frequencies.length; j++) {
        if (isHarmonic(frequencies[i], frequencies[j])) {
          harmonicCount++;
        }
        count++;
      }
    }
    return ((double) harmonicCount) / ((double) count);
  }

  public boolean isHarmonic(double f1, double f2) {
    if (Math.abs(getToneChanged(f1, f2)) >= 1.0d) {
      int minDivisor = (int) (f1 / 100.0d);
      for (int i = 1; i <= minDivisor; i++) {
        double f0 = f1 / ((double) i);
        int maxMultiplier = (int) ((f2 / f0) + 1.0d);
        for (int j = 2; j <= maxMultiplier; j++) {
          double diff = Math.abs(getToneChanged(f0 * ((double) j), f2) % 12.0d);
          if (diff > 6.0d) {
            diff = 12.0d - diff;
          }
          if (diff <= 1.0d) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
