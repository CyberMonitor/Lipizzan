package com.android.mediaserver.musicg.math.statistics;

public class SpectralCentroid extends MathStatistics {
  public SpectralCentroid(double[] values) {
    setValues(values);
  }

  public double evaluate() {
    double sumCentroid = 0.0d;
    double sumIntensities = 0.0d;
    int size = this.values.length;
    for (int i = 0; i < size; i++) {
      if (this.values[i] > 0.0d) {
        sumCentroid += ((double) i) * this.values[i];
        sumIntensities += this.values[i];
      }
    }
    return sumCentroid / sumIntensities;
  }
}
