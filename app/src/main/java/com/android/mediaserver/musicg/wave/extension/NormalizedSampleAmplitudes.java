package com.android.mediaserver.musicg.wave.extension;

import com.android.mediaserver.musicg.wave.Wave;

public class NormalizedSampleAmplitudes {
  private double[] normalizedAmplitudes;
  private Wave wave;

  public NormalizedSampleAmplitudes(Wave wave) {
    this.wave = wave;
  }

  public double[] getNormalizedAmplitudes() {
    if (this.normalizedAmplitudes == null) {
      boolean signed = true;
      if (this.wave.getWaveHeader().getBitsPerSample() == 8) {
        signed = false;
      }
      short[] amplitudes = this.wave.getSampleAmplitudes();
      int numSamples = amplitudes.length;
      int maxAmplitude = 1 << (this.wave.getWaveHeader().getBitsPerSample() - 1);
      if (!signed) {
        maxAmplitude <<= 1;
      }
      this.normalizedAmplitudes = new double[numSamples];
      for (int i = 0; i < numSamples; i++) {
        this.normalizedAmplitudes[i] = ((double) amplitudes[i]) / ((double) maxAmplitude);
      }
    }
    return this.normalizedAmplitudes;
  }
}
