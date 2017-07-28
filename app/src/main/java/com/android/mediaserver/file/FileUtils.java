package com.android.mediaserver.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;

public class FileUtils {
  public static String getFileSha1(File file) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
      FileInputStream fileInput = new FileInputStream(file);
      byte[] dataBytes = new byte[1024];
      while (true) {
        int bytesRead = fileInput.read(dataBytes);
        if (bytesRead == -1) {
          break;
        }
        messageDigest.update(dataBytes, 0, bytesRead);
      }
      byte[] digestBytes = messageDigest.digest();
      StringBuffer sb = new StringBuffer("");
      for (byte b : digestBytes) {
        sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
      }
      fileInput.close();
      return sb.toString();
    } catch (Exception e) {
      return null;
    }
  }

  public static void clearFileContent(File file) throws IOException {
    FileWriter fileOut = new FileWriter(file);
    fileOut.write("");
    fileOut.close();
  }
}
