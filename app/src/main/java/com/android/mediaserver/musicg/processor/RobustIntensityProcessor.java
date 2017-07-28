package com.android.mediaserver.musicg.processor;

import com.android.mediaserver.musicg.math.rank.ArrayRankDouble;
import java.lang.reflect.Array;

public class RobustIntensityProcessor implements IntensityProcessor {
  private double[][] intensities;
  private int numPointsPerFrame;

  public RobustIntensityProcessor(double[][] intensities, int numPointsPerFrame) {
    this.intensities = intensities;
    this.numPointsPerFrame = numPointsPerFrame;
  }

  public void execute() {
    int numX = this.intensities.length;
    int numY = this.intensities[0].length;
    double[][] processedIntensities =
        (double[][]) Array.newInstance(Double.TYPE, new int[] { numX, numY });
    for (int i = 0; i < numX; i++) {
      double[] tmpArray = new double[numY];
      System.arraycopy(this.intensities[i], 0, tmpArray, 0, numY);
      double passValue =
          new ArrayRankDouble().getNthOrderedValue(tmpArray, this.numPointsPerFrame, false);
      for (int j = 0; j < numY; j++) {
        if (this.intensities[i][j] >= passValue) {
          processedIntensities[i][j] = this.intensities[i][j];
        }
      }
    }
    this.intensities = processedIntensities;
  }

  public double[][] getIntensities() {
    return this.intensities;
  }
}
