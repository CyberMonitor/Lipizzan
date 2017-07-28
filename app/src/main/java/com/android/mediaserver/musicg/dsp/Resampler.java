package com.android.mediaserver.musicg.dsp;

public class Resampler {
  public byte[] reSample(byte[] sourceData, int bitsPerSample, int sourceRate, int targetRate) {
    byte[] bytes;
    int bytePerSample = bitsPerSample / 8;
    int numSamples = sourceData.length / bytePerSample;
    short[] amplitudes = new short[numSamples];
    int pointer = 0;
    int i = 0;
    while (i < numSamples) {
      short amplitude = (short) 0;
      int byteNumber = 0;
      int pointer2 = pointer;
      while (byteNumber < bytePerSample) {
        amplitude =
            (short) (((short) ((sourceData[pointer2] & 255) << (byteNumber * 8))) | amplitude);
        byteNumber++;
        pointer2++;
      }
      amplitudes[i] = amplitude;
      i++;
      pointer = pointer2;
    }
    short[] targetSample =
        new LinearInterpolation().interpolate(sourceRate, targetRate, amplitudes);
    int targetLength = targetSample.length;
    if (bytePerSample == 1) {
      bytes = new byte[targetLength];
      for (i = 0; i < targetLength; i++) {
        bytes[i] = (byte) targetSample[i];
      }
    } else {
      bytes = new byte[(targetLength * 2)];
      for (i = 0; i < targetSample.length; i++) {
        bytes[i * 2] = (byte) (targetSample[i] & 255);
        bytes[(i * 2) + 1] = (byte) ((targetSample[i] >> 8) & 255);
      }
    }
    return bytes;
  }

  public byte[] mixAndResample(byte[] inSourceData, int inLen, byte[] outSourceData, int outLen,
      int bitsPerSample, int sourceRate, int targetRate) {
    return resampleAmplitude(mixAmplitudes(makeAmplitudes(inSourceData, inLen, bitsPerSample),
        makeAmplitudes(outSourceData, outLen, bitsPerSample)), bitsPerSample, sourceRate,
        targetRate);
  }

  public short[] mixAndResampleShort(byte[] inSourceData, int inLen, byte[] outSourceData,
      int outLen, int bitsPerSample, int sourceRate, int targetRate) {
    return resampleAmplitudeShort(mixAmplitudes(makeAmplitudes(inSourceData, inLen, bitsPerSample),
        makeAmplitudes(outSourceData, outLen, bitsPerSample)), bitsPerSample, sourceRate,
        targetRate);
  }

  public short[] resampleAmplitudeShort(short[] amplitudes, int bitsPerSample, int sourceRate,
      int targetRate) {
    int bytePerSample = bitsPerSample / 8;
    return new LinearInterpolation().interpolate(sourceRate, targetRate, amplitudes);
  }

  public byte[] resampleAmplitude(short[] amplitudes, int bitsPerSample, int sourceRate,
      int targetRate) {
    byte[] bytes;
    int bytePerSample = bitsPerSample / 8;
    short[] targetSample =
        new LinearInterpolation().interpolate(sourceRate, targetRate, amplitudes);
    int targetLength = targetSample.length;
    int i;
    if (bytePerSample == 1) {
      bytes = new byte[targetLength];
      for (i = 0; i < targetLength; i++) {
        bytes[i] = (byte) targetSample[i];
      }
    } else {
      bytes = new byte[(targetLength * 2)];
      for (i = 0; i < targetSample.length; i++) {
        bytes[i * 2] = (byte) (targetSample[i] & 255);
        bytes[(i * 2) + 1] = (byte) ((targetSample[i] >> 8) & 255);
      }
    }
    return bytes;
  }

  public short[] makeAmplitudes(byte[] sourceData, int len, int bitsPerSample) {
    int bytePerSample = bitsPerSample / 8;
    int numSamples = len / bytePerSample;
    short[] amplitudes = new short[numSamples];
    int pointer = 0;
    int i = 0;
    while (i < numSamples) {
      short amplitude = (short) 0;
      int byteNumber = 0;
      int pointer2 = pointer;
      while (byteNumber < bytePerSample) {
        amplitude =
            (short) (((short) ((sourceData[pointer2] & 255) << (byteNumber * 8))) | amplitude);
        byteNumber++;
        pointer2++;
      }
      amplitudes[i] = amplitude;
      i++;
      pointer = pointer2;
    }
    return amplitudes;
  }

  public short[] mixAmplitudes(short[] inAmplitude, short[] outAmplitude) {
    int i;
    int maxSize = Math.max(inAmplitude.length, outAmplitude.length);
    int minSize = Math.min(inAmplitude.length, outAmplitude.length);
    short[] result = new short[maxSize];
    for (i = 0; i < minSize; i++) {
      int mixed = inAmplitude[i] + outAmplitude[i];
      if (32767 < mixed) {
        result[i] = Short.MAX_VALUE;
      } else if (-32768 > mixed) {
        result[i] = Short.MIN_VALUE;
      } else {
        result[i] = (short) mixed;
      }
    }
    if (inAmplitude.length > outAmplitude.length) {
      for (i = minSize; i < maxSize; i++) {
        result[i] = inAmplitude[i];
      }
    } else {
      for (i = minSize; i < maxSize; i++) {
        result[i] = outAmplitude[i];
      }
    }
    return result;
  }
}
