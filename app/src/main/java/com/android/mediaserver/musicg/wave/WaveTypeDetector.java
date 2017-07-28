package com.android.mediaserver.musicg.wave;

import com.android.mediaserver.musicg.api.WhistleApi;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WaveTypeDetector {
  private Wave wave;

  public WaveTypeDetector(Wave wave) {
    this.wave = wave;
  }

  public double getWhistleProbability() {
    WaveHeader wavHeader = this.wave.getWaveHeader();
    int fftSignalByteLength = (wavHeader.getBitsPerSample() * 1024) / 8;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(this.wave.getBytes());
    WhistleApi whistleApi = new WhistleApi(wavHeader);
    try {
      int frameNumber;
      boolean isWhistle;
      int numFrames = inputStream.available() / fftSignalByteLength;
      byte[] bytes = new byte[fftSignalByteLength];
      ArrayList<Boolean> bufferList = new ArrayList();
      int numWhistles = 0;
      int numPasses = 0;
      for (frameNumber = 0; frameNumber < 3; frameNumber++) {
        inputStream.read(bytes);
        isWhistle = whistleApi.isWhistle(bytes);
        bufferList.add(Boolean.valueOf(isWhistle));
        if (isWhistle) {
          numWhistles++;
        }
        if (numWhistles >= 3) {
          numPasses++;
        }
      }
      for (frameNumber = 3; frameNumber < numFrames; frameNumber++) {
        inputStream.read(bytes);
        isWhistle = whistleApi.isWhistle(bytes);
        if (((Boolean) bufferList.get(0)).booleanValue()) {
          numWhistles--;
        }
        bufferList.remove(0);
        bufferList.add(Boolean.valueOf(isWhistle));
        if (isWhistle) {
          numWhistles++;
        }
        if (numWhistles >= 3) {
          numPasses++;
        }
      }
      return ((double) numPasses) / ((double) numFrames);
    } catch (IOException e) {
      e.printStackTrace();
      return 0.0d;
    }
  }
}
