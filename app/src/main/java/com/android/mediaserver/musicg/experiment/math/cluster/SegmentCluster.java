package com.android.mediaserver.musicg.experiment.math.cluster;

import com.android.mediaserver.musicg.pitch.PitchHandler;
import java.util.LinkedList;
import java.util.List;

public class SegmentCluster {
  private double diffThreshold;

  public SegmentCluster() {
    this.diffThreshold = 1.0d;
  }

  public SegmentCluster(double diffThreshold) {
    this.diffThreshold = diffThreshold;
  }

  public void setDiffThreshold(double diffThreshold) {
    this.diffThreshold = diffThreshold;
  }

  public List<Segment> getSegments(double[] array) {
    Segment segment;
    PitchHandler pitchHandler = new PitchHandler();
    List<Segment> segmentList = new LinkedList();
    double segmentMean = 0.0d;
    int segmentSize = 0;
    if (array.length > 0) {
      segmentMean = array[1];
      segmentSize = 1;
    }
    for (int i = 1; i < array.length; i++) {
      if (Math.abs(pitchHandler.getToneChanged(array[i], segmentMean)) < this.diffThreshold) {
        segmentSize++;
        segmentMean = ((((double) segmentSize) * segmentMean) + array[i]) / ((double) segmentSize);
      } else {
        segment = new Segment();
        segment.setMean(segmentMean);
        segment.setStartPosition(i - segmentSize);
        segment.setSize(segmentSize);
        segmentList.add(segment);
        segmentMean = array[i];
        segmentSize = 1;
      }
    }
    segment = new Segment();
    segment.setMean(segmentMean);
    segment.setStartPosition(array.length - segmentSize);
    segment.setSize(segmentSize);
    segmentList.add(segment);
    return segmentList;
  }
}
