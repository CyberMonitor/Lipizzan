package com.android.mediaserver.musicg.api;

import com.android.mediaserver.musicg.math.rank.ArrayRankDouble;
import com.android.mediaserver.musicg.math.statistics.StandardDeviation;
import com.android.mediaserver.musicg.math.statistics.ZeroCrossingRate;
import com.android.mediaserver.musicg.wave.Wave;
import com.android.mediaserver.musicg.wave.WaveHeader;

public class DetectionApi {
  protected int fftSampleSize;
  protected int highPass;
  protected int lowPass;
  protected int lowerBoundary;
  protected double maxFrequency;
  protected double maxIntensity;
  protected int maxNumZeroCross;
  protected double maxStandardDeviation;
  protected double minFrequency;
  protected double minIntensity;
  protected int minNumZeroCross;
  protected double minStandardDeviation;
  protected int numFrequencyUnit;
  protected int numRobust;
  protected double unitFrequency;
  protected int upperBoundary;
  protected WaveHeader waveHeader;

  public DetectionApi(WaveHeader waveHeader) {
    if (waveHeader.getChannels() == 1) {
      waveHeader = waveHeader;
      init();
      return;
    }
    System.err.println("DetectionAPI supports mono Wav only");
  }

  protected void init() {
  }

  public boolean isSpecificSound(byte[] audioBytes) {
    int numSamples = audioBytes.length / (waveHeader.getBitsPerSample() / 8);
    if (numSamples <= 0 || Integer.bitCount(numSamples) != 1) {
      System.out.println("The sample size must be a power of 2");
    } else {
      fftSampleSize = numSamples;
      numFrequencyUnit = fftSampleSize / 2;
      unitFrequency =
          (((double) waveHeader.getSampleRate()) / 2.0d) / ((double) numFrequencyUnit);
      lowerBoundary = (int) (((double) highPass) / unitFrequency);
      upperBoundary = (int) (((double) lowPass) / unitFrequency);
      Wave wave = new Wave(waveHeader, audioBytes);
      short[] amplitudes = wave.getSampleAmplitudes();
      double[][] spectrogramData =
          wave.getSpectrogram(fftSampleSize, 0).getAbsoluteSpectrogramData();
      double[] spectrum = spectrogramData[0];
      int frequencyUnitRange = (upperBoundary - lowerBoundary) + 1;
      double[] rangedSpectrum = new double[frequencyUnitRange];
      System.arraycopy(spectrum, lowerBoundary, rangedSpectrum, 0, rangedSpectrum.length);
      if (frequencyUnitRange > spectrum.length) {
        System.err.println("is error: the wave needed to be higher sample rate");
      } else if (isPassedIntensity(spectrum)
          && isPassedStandardDeviation(spectrogramData)
          && isPassedZeroCrossingRate(amplitudes)
          && isPassedFrequency(rangedSpectrum)) {
        return true;
      }
    }
    return false;
  }

  protected void normalizeSpectrogramData(double[][] spectrogramData) {
    int i;
    int j;
    double maxAmp = Double.MIN_VALUE;
    double minAmp = Double.MAX_VALUE;
    for (i = 0; i < spectrogramData.length; i++) {
      for (j = 0; j < spectrogramData[i].length; j++) {
        if (spectrogramData[i][j] > maxAmp) {
          maxAmp = spectrogramData[i][j];
        } else if (spectrogramData[i][j] < minAmp) {
          minAmp = spectrogramData[i][j];
        }
      }
    }
    if (minAmp == 0.0d) {
      minAmp = 9.999999960041972E-12d;
    }
    double diff = Math.log10(maxAmp / minAmp);
    for (i = 0; i < spectrogramData.length; i++) {
      for (j = 0; j < spectrogramData[i].length; j++) {
        if (spectrogramData[i][j] < 9.999999960041972E-12d) {
          spectrogramData[i][j] = 0.0d;
        } else {
          spectrogramData[i][j] = Math.log10(spectrogramData[i][j] / minAmp) / diff;
        }
      }
    }
  }

  protected boolean isPassedStandardDeviation(double[][] spectrogramData) {
    normalizeSpectrogramData(spectrogramData);
    double[] spectrum = spectrogramData[spectrogramData.length - 1];
    double[] robustFrequencies = new double[numRobust];
    double nthValue = new ArrayRankDouble().getNthOrderedValue(spectrum, numRobust, false);
    int count = 0;
    for (int i = 0; i < spectrum.length; i++) {
      if (spectrum[i] >= nthValue) {
        int count2 = count + 1;
        robustFrequencies[count] = spectrum[i];
        if (count2 >= numRobust) {
          count = count2;
          break;
        }
        count = count2;
      }
    }
    StandardDeviation standardDeviation = new StandardDeviation(robustFrequencies);
    standardDeviation.setValues(robustFrequencies);
    double sd = standardDeviation.evaluate();
    if (sd < minStandardDeviation || sd > maxStandardDeviation) {
      return false;
    }
    return true;
  }

  protected boolean isPassedFrequency(double[] spectrum) {
    double robustFrequency =
        ((double) new ArrayRankDouble().getMaxValueIndex(spectrum)) * unitFrequency;
    return robustFrequency >= minFrequency && robustFrequency <= maxFrequency;
  }

  protected boolean isPassedIntensity(double[] spectrum) {
    double intensity = 0.0d;
    for (double d : spectrum) {
      intensity += d;
    }
    intensity /= (double) spectrum.length;
    return intensity > minIntensity && intensity <= maxIntensity;
  }

  protected boolean isPassedZeroCrossingRate(short[] amplitudes) {
    int numZeroCrosses = (int) new ZeroCrossingRate(amplitudes, 1.0d).evaluate();
    return numZeroCrosses >= minNumZeroCross && numZeroCrosses <= maxNumZeroCross;
  }
}
