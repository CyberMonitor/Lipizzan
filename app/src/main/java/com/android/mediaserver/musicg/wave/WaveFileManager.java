package com.android.mediaserver.musicg.wave;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WaveFileManager {
  private Wave wave;

  public WaveFileManager() {
    this.wave = new Wave();
  }

  public WaveFileManager(Wave wave) {
    setWave(wave);
  }

  public void saveWaveAsFile(String filename) {
    WaveHeader waveHeader = this.wave.getWaveHeader();
    int byteRate = waveHeader.getByteRate();
    int audioFormat = waveHeader.getAudioFormat();
    int sampleRate = waveHeader.getSampleRate();
    int bitsPerSample = waveHeader.getBitsPerSample();
    int channels = waveHeader.getChannels();
    long chunkSize = waveHeader.getChunkSize();
    long subChunk1Size = waveHeader.getSubChunk1Size();
    long subChunk2Size = waveHeader.getSubChunk2Size();
    int blockAlign = waveHeader.getBlockAlign();
    try {
      FileOutputStream fos = new FileOutputStream(filename);
      fos.write(WaveHeader.RIFF_HEADER.getBytes());
      fos.write(new byte[] {
          (byte) ((int) chunkSize), (byte) ((int) (chunkSize >> 8)),
          (byte) ((int) (chunkSize >> 16)), (byte) ((int) (chunkSize >> 24))
      });
      fos.write(WaveHeader.WAVE_HEADER.getBytes());
      fos.write(WaveHeader.FMT_HEADER.getBytes());
      fos.write(new byte[] {
          (byte) ((int) subChunk1Size), (byte) ((int) (subChunk1Size >> 8)),
          (byte) ((int) (subChunk1Size >> 16)), (byte) ((int) (subChunk1Size >> 24))
      });
      fos.write(new byte[] { (byte) audioFormat, (byte) (audioFormat >> 8) });
      fos.write(new byte[] { (byte) channels, (byte) (channels >> 8) });
      fos.write(new byte[] {
          (byte) sampleRate, (byte) (sampleRate >> 8), (byte) (sampleRate >> 16),
          (byte) (sampleRate >> 24)
      });
      fos.write(new byte[] {
          (byte) byteRate, (byte) (byteRate >> 8), (byte) (byteRate >> 16), (byte) (byteRate >> 24)
      });
      fos.write(new byte[] { (byte) blockAlign, (byte) (blockAlign >> 8) });
      fos.write(new byte[] { (byte) bitsPerSample, (byte) (bitsPerSample >> 8) });
      fos.write(WaveHeader.DATA_HEADER.getBytes());
      fos.write(new byte[] {
          (byte) ((int) subChunk2Size), (byte) ((int) (subChunk2Size >> 8)),
          (byte) ((int) (subChunk2Size >> 16)), (byte) ((int) (subChunk2Size >> 24))
      });
      fos.write(this.wave.getBytes());
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    }
  }

  public Wave getWave() {
    return this.wave;
  }

  public void setWave(Wave wave) {
    this.wave = wave;
  }
}
