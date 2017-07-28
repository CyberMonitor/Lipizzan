package com.android.mediaserver.musicg.fingerprint;

import com.android.mediaserver.musicg.dsp.Resampler;
import com.android.mediaserver.musicg.processor.TopManyPointsProcessorChain;
import com.android.mediaserver.musicg.properties.FingerprintProperties;
import com.android.mediaserver.musicg.wave.Wave;
import com.android.mediaserver.musicg.wave.WaveHeader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FingerprintManager {
  private FingerprintProperties fingerprintProperties = FingerprintProperties.getInstance();
  private int numFilterBanks = this.fingerprintProperties.getNumFilterBanks();
  private int numRobustPointsPerFrame = this.fingerprintProperties.getNumRobustPointsPerFrame();
  private int overlapFactor = this.fingerprintProperties.getOverlapFactor();
  private int sampleSizePerFrame = this.fingerprintProperties.getSampleSizePerFrame();

  public static int getNumFrames(byte[] fingerprint) {
    if (fingerprint.length < 8) {
      return 0;
    }
    return (((fingerprint[fingerprint.length - 8] & 255) << 8) | (fingerprint[fingerprint.length
        - 7] & 255)) + 1;
  }

  public byte[] extractFingerprint(Wave wave) {
    int x;
    byte[] fingerprint = new byte[0];
    Resampler resampler = new Resampler();
    int sourceRate = wave.getWaveHeader().getSampleRate();
    int targetRate = this.fingerprintProperties.getSampleRate();
    byte[] resampledWaveData =
        resampler.reSample(wave.getBytes(), wave.getWaveHeader().getBitsPerSample(), sourceRate,
            targetRate);
    WaveHeader resampledWaveHeader = wave.getWaveHeader();
    resampledWaveHeader.setSampleRate(targetRate);
    double[][] spectorgramData =
        new Wave(resampledWaveHeader, resampledWaveData).getSpectrogram(this.sampleSizePerFrame,
            this.overlapFactor).getNormalizedSpectrogramData();
    List<Integer>[] pointsLists = getRobustPointList(spectorgramData);
    int numFrames = pointsLists.length;
    int[][] coordinates = (int[][]) Array.newInstance(Integer.TYPE,
        new int[] { numFrames, this.numRobustPointsPerFrame });
    for (x = 0; x < numFrames; x++) {
      int y;
      if (pointsLists[x].size() == this.numRobustPointsPerFrame) {
        Iterator<Integer> pointsListsIterator = pointsLists[x].iterator();
        for (y = 0; y < this.numRobustPointsPerFrame; y++) {
          coordinates[x][y] = ((Integer) pointsListsIterator.next()).intValue();
        }
      } else {
        for (y = 0; y < this.numRobustPointsPerFrame; y++) {
          coordinates[x][y] = -1;
        }
      }
    }
    List<Byte> byteList = new LinkedList();
    for (int i = 0; i < numFrames; i++) {
      for (int j = 0; j < this.numRobustPointsPerFrame; j++) {
        if (coordinates[i][j] != -1) {
          x = i;
          byteList.add(Byte.valueOf((byte) (x >> 8)));
          byteList.add(Byte.valueOf((byte) x));
          int y = coordinates[i][j];
          byteList.add(Byte.valueOf((byte) (y >> 8)));
          byteList.add(Byte.valueOf((byte) y));
          int intensity = (int) (spectorgramData[x][y] * 2.147483647E9d);
          byteList.add(Byte.valueOf((byte) (intensity >> 24)));
          byteList.add(Byte.valueOf((byte) (intensity >> 16)));
          byteList.add(Byte.valueOf((byte) (intensity >> 8)));
          byteList.add(Byte.valueOf((byte) intensity));
        }
      }
    }
    fingerprint = new byte[byteList.size()];
    int pointer = 0;
    for (Byte byteValue : byteList) {
      int pointer2 = pointer + 1;
      fingerprint[pointer] = byteValue.byteValue();
      pointer = pointer2;
    }
    return fingerprint;
  }

  public byte[] getFingerprintFromFile(String fingerprintFile) {
    byte[] fingerprint = null;
    try {
      InputStream fis = new FileInputStream(fingerprintFile);
      fingerprint = getFingerprintFromInputStream(fis);
      fis.close();
      return fingerprint;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return fingerprint;
    } catch (IOException e2) {
      e2.printStackTrace();
      return fingerprint;
    }
  }

  public byte[] getFingerprintFromInputStream(InputStream inputStream) {
    byte[] fingerprint = null;
    try {
      fingerprint = new byte[inputStream.available()];
      inputStream.read(fingerprint);
      return fingerprint;
    } catch (IOException e) {
      e.printStackTrace();
      return fingerprint;
    }
  }

  public void saveFingerprintAsFile(byte[] fingerprint, String filename) {
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(filename);
      fileOutputStream.write(fingerprint);
      fileOutputStream.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<Integer>[] getRobustPointList(double[][] spectrogramData) {
    int i;
    int j;
    int numX = spectrogramData.length;
    int numY = spectrogramData[0].length;
    double[][] allBanksIntensities =
        (double[][]) Array.newInstance(Double.TYPE, new int[] { numX, numY });
    int bandwidthPerBank = numY / this.numFilterBanks;
    for (int b = 0; b < this.numFilterBanks; b++) {
      double[][] bankIntensities =
          (double[][]) Array.newInstance(Double.TYPE, new int[] { numX, bandwidthPerBank });
      for (i = 0; i < numX; i++) {
        for (j = 0; j < bandwidthPerBank; j++) {
          bankIntensities[i][j] = spectrogramData[i][(b * bandwidthPerBank) + j];
        }
      }
      double[][] processedIntensities =
          new TopManyPointsProcessorChain(bankIntensities, 1).getIntensities();
      for (i = 0; i < numX; i++) {
        for (j = 0; j < bandwidthPerBank; j++) {
          allBanksIntensities[i][(b * bandwidthPerBank) + j] = processedIntensities[i][j];
        }
      }
    }
    List<int[]> robustPointList = new LinkedList();
    for (i = 0; i < allBanksIntensities.length; i++) {
      for (double d : allBanksIntensities[i]) {
        if (d > 0.0d) {
          robustPointList.add(new int[] { i, (int) d });
        }
      }
    }
    List<Integer>[] robustLists = new LinkedList[spectrogramData.length];
    for (i = 0; i < robustLists.length; i++) {
      robustLists[i] = new LinkedList();
    }
    for (int[] coor : robustPointList) {
      robustLists[coor[0]].add(Integer.valueOf(coor[1]));
    }
    return robustLists;
  }
}
