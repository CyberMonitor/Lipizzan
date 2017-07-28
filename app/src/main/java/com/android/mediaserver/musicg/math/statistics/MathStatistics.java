package com.android.mediaserver.musicg.math.statistics;

public abstract class MathStatistics {
  protected double[] values;

  public abstract double evaluate();

  public void setValues(double[] values) {
    this.values = values;
  }
}
