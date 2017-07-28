package com.android.mediaserver.musicg.experiment.math.cluster;

public class Segment {
  private double mean;
  private int size;
  private int startPosition;

  public int getStartPosition() {
    return this.startPosition;
  }

  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
  }

  public int getSize() {
    return this.size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public double getMean() {
    return this.mean;
  }

  public void setMean(double mean) {
    this.mean = mean;
  }
}
