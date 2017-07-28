package com.android.mediaserver.musicg.wave;

import android.support.v4.view.ViewCompat;
import java.io.IOException;
import java.io.InputStream;

public class WaveHeader {
  public static final String DATA_HEADER = "data";
  public static final String FMT_HEADER = "fmt ";
  public static final int HEADER_BYTE_LENGTH = 44;
  public static final String RIFF_HEADER = "RIFF";
  public static final String WAVE_HEADER = "WAVE";
  private int audioFormat;
  private int bitsPerSample;
  private int blockAlign;
  private long byteRate;
  private int channels;
  private String chunkId;
  private long chunkSize;
  private String format;
  private long sampleRate;
  private String subChunk1Id;
  private long subChunk1Size;
  private String subChunk2Id;
  private long subChunk2Size;
  private boolean valid;

  public WaveHeader() {
    this.chunkSize = 36;
    this.subChunk1Size = 16;
    this.audioFormat = 1;
    this.channels = 1;
    this.sampleRate = 8000;
    this.byteRate = 16000;
    this.blockAlign = 2;
    this.bitsPerSample = 16;
    this.subChunk2Size = 0;
    this.valid = true;
  }

  public WaveHeader(InputStream inputStream) {
    this.valid = loadHeader(inputStream);
  }

  private boolean loadHeader(InputStream inputStream) {
    byte[] headerBuffer = new byte[44];
    try {
      inputStream.read(headerBuffer);
      byte[] r7 = new byte[4];
      int pointer = 0 + 1;
      r7[0] = headerBuffer[0];
      int pointer2 = pointer + 1;
      r7[1] = headerBuffer[pointer];
      pointer = pointer2 + 1;
      r7[2] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[3] = headerBuffer[pointer];
      this.chunkId = new String(r7);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.chunkSize =
          ((((long) (headerBuffer[pointer2] & 255)) | (((long) (headerBuffer[pointer] & 255)) << 8))
              | (((long) (headerBuffer[pointer2] & 255)) << 16)) | ((long) (headerBuffer[pointer]
              & ViewCompat.MEASURED_STATE_MASK));
      r7 = new byte[4];
      pointer = pointer2 + 1;
      r7[0] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[1] = headerBuffer[pointer];
      pointer = pointer2 + 1;
      r7[2] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[3] = headerBuffer[pointer];
      this.format = new String(r7);
      r7 = new byte[4];
      pointer = pointer2 + 1;
      r7[0] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[1] = headerBuffer[pointer];
      pointer = pointer2 + 1;
      r7[2] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[3] = headerBuffer[pointer];
      this.subChunk1Id = new String(r7);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.subChunk1Size =
          ((((long) (headerBuffer[pointer2] & 255)) | (((long) (headerBuffer[pointer] & 255)) << 8))
              | (((long) (headerBuffer[pointer2] & 255)) << 16)) | (((long) (headerBuffer[pointer]
              & 255)) << 24);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.audioFormat = (headerBuffer[pointer2] & 255) | ((headerBuffer[pointer] & 255) << 8);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.channels = (headerBuffer[pointer2] & 255) | ((headerBuffer[pointer] & 255) << 8);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.sampleRate =
          ((((long) (headerBuffer[pointer2] & 255)) | (((long) (headerBuffer[pointer] & 255)) << 8))
              | (((long) (headerBuffer[pointer2] & 255)) << 16)) | (((long) (headerBuffer[pointer]
              & 255)) << 24);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.byteRate =
          ((((long) (headerBuffer[pointer2] & 255)) | (((long) (headerBuffer[pointer] & 255)) << 8))
              | (((long) (headerBuffer[pointer2] & 255)) << 16)) | (((long) (headerBuffer[pointer]
              & 255)) << 24);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.blockAlign = (headerBuffer[pointer2] & 255) | ((headerBuffer[pointer] & 255) << 8);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.bitsPerSample = (headerBuffer[pointer2] & 255) | ((headerBuffer[pointer] & 255) << 8);
      r7 = new byte[4];
      pointer = pointer2 + 1;
      r7[0] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[1] = headerBuffer[pointer];
      pointer = pointer2 + 1;
      r7[2] = headerBuffer[pointer2];
      pointer2 = pointer + 1;
      r7[3] = headerBuffer[pointer];
      this.subChunk2Id = new String(r7);
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      pointer = pointer2 + 1;
      pointer2 = pointer + 1;
      this.subChunk2Size =
          ((((long) (headerBuffer[pointer2] & 255)) | (((long) (headerBuffer[pointer] & 255)) << 8))
              | (((long) (headerBuffer[pointer2] & 255)) << 16)) | (((long) (headerBuffer[pointer]
              & 255)) << 24);
      if (this.bitsPerSample != 8 && this.bitsPerSample != 16) {
        System.err.println("WaveHeader: only supports bitsPerSample 8 or 16");
        return false;
      } else if (this.chunkId.toUpperCase().equals(RIFF_HEADER) && this.format.toUpperCase()
          .equals(WAVE_HEADER) && this.audioFormat == 1) {
        return true;
      } else {
        System.err.println("WaveHeader: Unsupported header format");
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean isValid() {
    return this.valid;
  }

  public String getChunkId() {
    return this.chunkId;
  }

  public void setChunkId(String chunkId) {
    this.chunkId = chunkId;
  }

  public long getChunkSize() {
    return this.chunkSize;
  }

  public void setChunkSize(long chunkSize) {
    this.chunkSize = chunkSize;
  }

  public String getFormat() {
    return this.format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getSubChunk1Id() {
    return this.subChunk1Id;
  }

  public void setSubChunk1Id(String subChunk1Id) {
    this.subChunk1Id = subChunk1Id;
  }

  public long getSubChunk1Size() {
    return this.subChunk1Size;
  }

  public void setSubChunk1Size(long subChunk1Size) {
    this.subChunk1Size = subChunk1Size;
  }

  public int getAudioFormat() {
    return this.audioFormat;
  }

  public void setAudioFormat(int audioFormat) {
    this.audioFormat = audioFormat;
  }

  public int getChannels() {
    return this.channels;
  }

  public void setChannels(int channels) {
    this.channels = channels;
  }

  public int getSampleRate() {
    return (int) this.sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    int newSubChunk2Size = (int) ((this.subChunk2Size * ((long) sampleRate)) / this.sampleRate);
    if ((this.bitsPerSample / 8) % 2 == 0 && newSubChunk2Size % 2 != 0) {
      newSubChunk2Size++;
    }
    this.sampleRate = (long) sampleRate;
    this.byteRate = (long) ((this.bitsPerSample * sampleRate) / 8);
    this.chunkSize = (long) (newSubChunk2Size + 36);
    this.subChunk2Size = (long) newSubChunk2Size;
  }

  public int getByteRate() {
    return (int) this.byteRate;
  }

  public void setByteRate(long byteRate) {
    this.byteRate = byteRate;
  }

  public int getBlockAlign() {
    return this.blockAlign;
  }

  public void setBlockAlign(int blockAlign) {
    this.blockAlign = blockAlign;
  }

  public int getBitsPerSample() {
    return this.bitsPerSample;
  }

  public void setBitsPerSample(int bitsPerSample) {
    this.bitsPerSample = bitsPerSample;
  }

  public String getSubChunk2Id() {
    return this.subChunk2Id;
  }

  public void setSubChunk2Id(String subChunk2Id) {
    this.subChunk2Id = subChunk2Id;
  }

  public long getSubChunk2Size() {
    return this.subChunk2Size;
  }

  public void setSubChunk2Size(long subChunk2Size) {
    this.subChunk2Size = subChunk2Size;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("chunkId: " + this.chunkId);
    sb.append("\n");
    sb.append("chunkSize: " + this.chunkSize);
    sb.append("\n");
    sb.append("format: " + this.format);
    sb.append("\n");
    sb.append("subChunk1Id: " + this.subChunk1Id);
    sb.append("\n");
    sb.append("subChunk1Size: " + this.subChunk1Size);
    sb.append("\n");
    sb.append("audioFormat: " + this.audioFormat);
    sb.append("\n");
    sb.append("channels: " + this.channels);
    sb.append("\n");
    sb.append("sampleRate: " + this.sampleRate);
    sb.append("\n");
    sb.append("byteRate: " + this.byteRate);
    sb.append("\n");
    sb.append("blockAlign: " + this.blockAlign);
    sb.append("\n");
    sb.append("bitsPerSample: " + this.bitsPerSample);
    sb.append("\n");
    sb.append("subChunk2Id: " + this.subChunk2Id);
    sb.append("\n");
    sb.append("subChunk2Size: " + this.subChunk2Size);
    return sb.toString();
  }
}
