package com.android.mediaserver.musicg.processor;

import java.util.LinkedList;
import java.util.List;

public class TopManyPointsProcessorChain {
  List<IntensityProcessor> processorList = new LinkedList();
  private double[][] intensities;

  public TopManyPointsProcessorChain(double[][] intensities, int numPoints) {
    this.intensities = intensities;
    this.processorList.add(new RobustIntensityProcessor(intensities, numPoints));
    process();
  }

  private void process() {
    for (IntensityProcessor processor : this.processorList) {
      processor.execute();
      this.intensities = processor.getIntensities();
    }
  }

  public double[][] getIntensities() {
    return this.intensities;
  }
}
