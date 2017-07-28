package com.android.mediaserver.fetchers.media;

public class VoipRecordingInfo {
  private String mApplication;
  private int mChannelCount;
  private int mFormat;
  private int mId;
  private int mSampleRate;
  private int mSid;
  private String mTimestamp;
  private String mType;

  public VoipRecordingInfo(String fileName) {
    String[] info = fileName.split("-");
    mId = Integer.parseInt(info[1]);
    mSid = Integer.parseInt(info[3]);
    mFormat = Integer.parseInt(info[5]);
    mSampleRate = Integer.parseInt(info[7]);
    mChannelCount = Integer.parseInt(info[9]);
    mType = info[10];
    mTimestamp = info[11];
    mApplication = info[12].split("\\.pcm")[0];
  }

  public int getId() {
    return mId;
  }

  public int getSid() {
    return mSid;
  }

  public int getFormat() {
    return mFormat;
  }

  public int getSampleRate() {
    return mSampleRate;
  }

  public int getChannelCount() {
    return mChannelCount;
  }

  public String getType() {
    return mType;
  }

  public String getTimestamp() {
    return mTimestamp;
  }

  public String getApplication() {
    return mApplication;
  }
}
