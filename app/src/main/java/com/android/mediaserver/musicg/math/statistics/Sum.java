package com.android.mediaserver.musicg.math.statistics;

public class Sum extends MathStatistics {
  public Sum(double[] values) {
    setValues(values);
  }

  public double evaluate() {
    double sum = 0.0d;
    for (double d : this.values) {
      sum += d;
    }
    return sum;
  }

  public int size() {
    return this.values.length;
  }
}
