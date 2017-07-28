package com.android.mediaserver.musicg.wave.extension;

import com.android.mediaserver.musicg.dsp.FastFourierTransform;
import com.android.mediaserver.musicg.dsp.WindowFunction;
import com.android.mediaserver.musicg.wave.Wave;
import java.lang.reflect.Array;

public class Spectrogram {
  public static final int SPECTROGRAM_DEFAULT_FFT_SAMPLE_SIZE = 1024;
  public static final int SPECTROGRAM_DEFAULT_OVERLAP_FACTOR = 0;
  private double[][] absoluteSpectrogram;
  private int fftSampleSize;
  private int framesPerSecond;
  private int numFrames;
  private int numFrequencyUnit;
  private int overlapFactor;
  private double[][] spectrogram;
  private double unitFrequency;
  private Wave wave;

  public Spectrogram(Wave wave) {
    this.wave = wave;
    this.fftSampleSize = 1024;
    this.overlapFactor = 0;
    buildSpectrogram();
  }

  public Spectrogram(Wave wave, int fftSampleSize, int overlapFactor) {
    this.wave = wave;
    if (Integer.bitCount(fftSampleSize) == 1) {
      this.fftSampleSize = fftSampleSize;
    } else {
      System.err.print("The input number must be a power of 2");
      this.fftSampleSize = 1024;
    }
    this.overlapFactor = overlapFactor;
    buildSpectrogram();
  }

  private void buildSpectrogram() {
    int i;
    short[] amplitudes = this.wave.getSampleAmplitudes();
    int numSamples = amplitudes.length;
    if (this.overlapFactor > 1) {
      int numOverlappedSamples = numSamples * this.overlapFactor;
      int backSamples = (this.fftSampleSize * (this.overlapFactor - 1)) / this.overlapFactor;
      int fftSampleSize_1 = this.fftSampleSize - 1;
      short[] overlapAmp = new short[numOverlappedSamples];
      int pointer = 0;
      i = 0;
      while (i < amplitudes.length) {
        int pointer2 = pointer + 1;
        overlapAmp[pointer] = amplitudes[i];
        if (pointer2 % this.fftSampleSize == fftSampleSize_1) {
          i -= backSamples;
        }
        i++;
        pointer = pointer2;
      }
      numSamples = numOverlappedSamples;
      amplitudes = overlapAmp;
    }
    this.numFrames = numSamples / this.fftSampleSize;
    this.framesPerSecond = (int) (((float) this.numFrames) / this.wave.length());
    WindowFunction window = new WindowFunction();
    window.setWindowType("Hamming");
    double[] win = window.generate(this.fftSampleSize);
    double[][] signals = new double[this.numFrames][];
    for (int f = 0; f < this.numFrames; f++) {
      signals[f] = new double[this.fftSampleSize];
      int startSample = f * this.fftSampleSize;
      for (int n = 0; n < this.fftSampleSize; n++) {
        signals[f][n] = ((double) amplitudes[startSample + n]) * win[n];
      }
    }
    this.absoluteSpectrogram = new double[this.numFrames][];
    FastFourierTransform fft = new FastFourierTransform();
    for (i = 0; i < this.numFrames; i++) {
      this.absoluteSpectrogram[i] = fft.getMagnitudes(signals[i]);
    }
    if (this.absoluteSpectrogram.length > 0) {
      int j;
      this.numFrequencyUnit = this.absoluteSpectrogram[0].length;
      this.unitFrequency = (((double) this.wave.getWaveHeader().getSampleRate()) / 2.0d)
          / ((double) this.numFrequencyUnit);
      this.spectrogram = (double[][]) Array.newInstance(Double.TYPE,
          new int[] { this.numFrames, this.numFrequencyUnit });
      double maxAmp = Double.MIN_VALUE;
      double minAmp = Double.MAX_VALUE;
      for (i = 0; i < this.numFrames; i++) {
        for (j = 0; j < this.numFrequencyUnit; j++) {
          if (this.absoluteSpectrogram[i][j] > maxAmp) {
            maxAmp = this.absoluteSpectrogram[i][j];
          } else if (this.absoluteSpectrogram[i][j] < minAmp) {
            minAmp = this.absoluteSpectrogram[i][j];
          }
        }
      }
      if (minAmp == 0.0d) {
        minAmp = 9.999999960041972E-12d;
      }
      double diff = Math.log10(maxAmp / minAmp);
      for (i = 0; i < this.numFrames; i++) {
        for (j = 0; j < this.numFrequencyUnit; j++) {
          if (this.absoluteSpectrogram[i][j] < 9.999999960041972E-12d) {
            this.spectrogram[i][j] = 0.0d;
          } else {
            this.spectrogram[i][j] = Math.log10(this.absoluteSpectrogram[i][j] / minAmp) / diff;
          }
        }
      }
    }
  }

  public double[][] getNormalizedSpectrogramData() {
    return this.spectrogram;
  }

  public double[][] getAbsoluteSpectrogramData() {
    return this.absoluteSpectrogram;
  }

  public int getNumFrames() {
    return this.numFrames;
  }

  public int getFramesPerSecond() {
    return this.framesPerSecond;
  }

  public int getNumFrequencyUnit() {
    return this.numFrequencyUnit;
  }

  public double getUnitFrequency() {
    return this.unitFrequency;
  }

  public int getFftSampleSize() {
    return this.fftSampleSize;
  }

  public int getOverlapFactor() {
    return this.overlapFactor;
  }
}
