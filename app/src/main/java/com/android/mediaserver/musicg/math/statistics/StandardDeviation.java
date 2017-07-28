package com.android.mediaserver.musicg.math.statistics;

public class StandardDeviation extends MathStatistics {
  private Mean mean = new Mean(values);

  public StandardDeviation(double[] values) {
    setValues(values);
  }

  public double evaluate() {
    this.mean.setValues(this.values);
    double meanValue = this.mean.evaluate();
    double diffSquare = 0.0d;
    for (double d : this.values) {
      diffSquare += Math.pow(d - meanValue, 2.0d);
    }
    if (values.length > 0) {
      return Math.sqrt(diffSquare / ((double) values.length));
    }
    return Double.NaN;
  }
}
