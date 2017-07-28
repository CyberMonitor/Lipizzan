package com.android.mediaserver.musicg.dsp;

public class LinearInterpolation {
  public short[] interpolate(int oldSampleRate, int newSampleRate, short[] samples) {
    if (oldSampleRate == newSampleRate) {
      return samples;
    }
    int newLength =
        Math.round((((float) samples.length) / ((float) oldSampleRate)) * ((float) newSampleRate));
    float lengthMultiplier = ((float) newLength) / ((float) samples.length);
    short[] interpolatedSamples = new short[newLength];
    for (int i = 0; i < newLength; i++) {
      float currentPosition = ((float) i) / lengthMultiplier;
      int nearestLeftPosition = (int) currentPosition;
      int nearestRightPosition = nearestLeftPosition + 1;
      if (nearestRightPosition >= samples.length) {
        nearestRightPosition = samples.length - 1;
      }
      interpolatedSamples[i] =
          (short) ((int) ((((float) (samples[nearestRightPosition] - samples[nearestLeftPosition]))
              * (currentPosition - ((float) nearestLeftPosition)))
              + ((float) samples[nearestLeftPosition])));
    }
    return interpolatedSamples;
  }
}
