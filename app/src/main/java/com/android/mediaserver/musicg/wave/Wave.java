package com.android.mediaserver.musicg.wave;

import com.android.mediaserver.musicg.fingerprint.FingerprintManager;
import com.android.mediaserver.musicg.fingerprint.FingerprintSimilarity;
import com.android.mediaserver.musicg.fingerprint.FingerprintSimilarityComputer;
import com.android.mediaserver.musicg.wave.extension.NormalizedSampleAmplitudes;
import com.android.mediaserver.musicg.wave.extension.Spectrogram;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class Wave implements Serializable {
  private static final long serialVersionUID = 1;
  private byte[] data;
  private byte[] fingerprint;
  private WaveHeader waveHeader;

  public Wave() {
    this.waveHeader = new WaveHeader();
    this.data = new byte[0];
  }

  public Wave(String filename) {
    try {
      InputStream inputStream = new FileInputStream(filename);
      initWaveWithInputStream(inputStream);
      inputStream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    }
  }

  public Wave(InputStream inputStream) {
    initWaveWithInputStream(inputStream);
  }

  public Wave(WaveHeader waveHeader, byte[] data) {
    this.waveHeader = waveHeader;
    this.data = data;
  }

  private void initWaveWithInputStream(InputStream inputStream) {
    this.waveHeader = new WaveHeader(inputStream);
    if (this.waveHeader.isValid()) {
      try {
        this.data = new byte[inputStream.available()];
        inputStream.read(this.data);
        return;
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }
    System.err.println("Invalid Wave Header");
  }

  public void trim(int leftTrimNumberOfSample, int rightTrimNumberOfSample) {
    long chunkSize = this.waveHeader.getChunkSize();
    long subChunk2Size = this.waveHeader.getSubChunk2Size();
    long totalTrimmed = (long) (leftTrimNumberOfSample + rightTrimNumberOfSample);
    if (totalTrimmed > subChunk2Size) {
      leftTrimNumberOfSample = (int) subChunk2Size;
    }
    chunkSize -= totalTrimmed;
    subChunk2Size -= totalTrimmed;
    if (chunkSize < 0 || subChunk2Size < 0) {
      System.err.println("Trim error: Negative length");
      return;
    }
    this.waveHeader.setChunkSize(chunkSize);
    this.waveHeader.setSubChunk2Size(subChunk2Size);
    byte[] trimmedData = new byte[((int) subChunk2Size)];
    System.arraycopy(this.data, leftTrimNumberOfSample, trimmedData, 0, (int) subChunk2Size);
    this.data = trimmedData;
  }

  public void leftTrim(int numberOfSample) {
    trim(numberOfSample, 0);
  }

  public void rightTrim(int numberOfSample) {
    trim(0, numberOfSample);
  }

  public void trim(double leftTrimSecond, double rightTrimSecond) {
    int sampleRate = this.waveHeader.getSampleRate();
    int bitsPerSample = this.waveHeader.getBitsPerSample();
    int channels = this.waveHeader.getChannels();
    trim((int) (((double) (((sampleRate * bitsPerSample) / 8) * channels)) * leftTrimSecond),
        (int) (((double) (((sampleRate * bitsPerSample) / 8) * channels)) * rightTrimSecond));
  }

  public void leftTrim(double second) {
    trim(second, 0.0d);
  }

  public void rightTrim(double second) {
    trim(0.0d, second);
  }

  public WaveHeader getWaveHeader() {
    return this.waveHeader;
  }

  public Spectrogram getSpectrogram() {
    return new Spectrogram(this);
  }

  public Spectrogram getSpectrogram(int fftSampleSize, int overlapFactor) {
    return new Spectrogram(this, fftSampleSize, overlapFactor);
  }

  public byte[] getBytes() {
    return this.data;
  }

  public int size() {
    return this.data.length;
  }

  public float length() {
    return ((float) this.waveHeader.getSubChunk2Size()) / ((float) this.waveHeader.getByteRate());
  }

  public String timestamp() {
    float totalSeconds = length();
    float second = totalSeconds % BitmapDescriptorFactory.HUE_YELLOW;
    int minute = (((int) totalSeconds) / 60) % 60;
    int hour = (int) (totalSeconds / 3600.0f);
    StringBuffer sb = new StringBuffer();
    if (hour > 0) {
      sb.append(hour + ":");
    }
    if (minute > 0) {
      sb.append(minute + ":");
    }
    sb.append(second);
    return sb.toString();
  }

  public short[] getSampleAmplitudes() {
    int bytePerSample = this.waveHeader.getBitsPerSample() / 8;
    int numSamples = this.data.length / bytePerSample;
    short[] amplitudes = new short[numSamples];
    int pointer = 0;
    int i = 0;
    while (i < numSamples) {
      short amplitude = (short) 0;
      int byteNumber = 0;
      int pointer2 = pointer;
      while (byteNumber < bytePerSample) {
        amplitude =
            (short) (((short) ((this.data[pointer2] & 255) << (byteNumber * 8))) | amplitude);
        byteNumber++;
        pointer2++;
      }
      amplitudes[i] = amplitude;
      i++;
      pointer = pointer2;
    }
    return amplitudes;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(this.waveHeader.toString());
    sb.append("\n");
    sb.append("length: " + timestamp());
    return sb.toString();
  }

  public double[] getNormalizedAmplitudes() {
    return new NormalizedSampleAmplitudes(this).getNormalizedAmplitudes();
  }

  public byte[] getFingerprint() {
    if (this.fingerprint == null) {
      this.fingerprint = new FingerprintManager().extractFingerprint(this);
    }
    return this.fingerprint;
  }

  public FingerprintSimilarity getFingerprintSimilarity(Wave wave) {
    return new FingerprintSimilarityComputer(getFingerprint(),
        wave.getFingerprint()).getFingerprintsSimilarity();
  }
}
