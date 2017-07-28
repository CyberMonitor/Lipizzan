package com.android.mediaserver.musicg.math.statistics;

public class Mean extends MathStatistics {
  private Sum sum = new Sum(values);

  public Mean(double[] values) {
    setValues(values);
  }

  public double evaluate() {
    this.sum.setValues(this.values);
    return this.sum.evaluate() / ((double) this.sum.size());
  }
}
