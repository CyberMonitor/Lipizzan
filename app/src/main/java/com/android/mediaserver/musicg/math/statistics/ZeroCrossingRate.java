package com.android.mediaserver.musicg.math.statistics;

public class ZeroCrossingRate {
  private double lengthInSecond;
  private short[] signals;

  public ZeroCrossingRate(short[] signals, double lengthInSecond) {
    setSignals(signals, 1.0d);
  }

  public void setSignals(short[] signals, double lengthInSecond) {
    this.signals = signals;
    this.lengthInSecond = lengthInSecond;
  }

  public double evaluate() {
    int numZC = 0;
    int size = this.signals.length;
    int i = 0;
    while (i < size - 1) {
      if ((this.signals[i] >= (short) 0 && this.signals[i + 1] < (short) 0) || (this.signals[i]
          < (short) 0 && this.signals[i + 1] >= (short) 0)) {
        numZC++;
      }
      i++;
    }
    return ((double) numZC) / this.lengthInSecond;
  }
}
