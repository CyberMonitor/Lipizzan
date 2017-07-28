package com.android.mediaserver.musicg.dsp;

import com.android.mediaserver.encoder.FFT;

public class FastFourierTransform {
  public double[] getMagnitudes(double[] amplitudes) {
    int sampleSize = amplitudes.length;
    new FFT(sampleSize / 2, -1).transform(amplitudes);
    double[] complexNumbers = amplitudes;
    int indexSize = sampleSize / 2;
    double[] mag = new double[(indexSize / 2)];
    for (int i = 0; i < indexSize; i += 2) {
      mag[i / 2] = Math.sqrt(
          (complexNumbers[i] * complexNumbers[i]) + (complexNumbers[i + 1] * complexNumbers[i
              + 1]));
    }
    return mag;
  }
}
