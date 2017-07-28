package com.android.mediaserver.musicg.processor;

import java.util.LinkedList;
import java.util.List;

public class ProcessorChain {
  List<IntensityProcessor> processorList = new LinkedList();
  private double[][] intensities;

  public ProcessorChain(double[][] intensities) {
    this.intensities = intensities;
    this.processorList.add(new RobustIntensityProcessor(intensities, 1));
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
