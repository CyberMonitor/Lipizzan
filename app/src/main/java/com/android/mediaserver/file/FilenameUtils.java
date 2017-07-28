package com.android.mediaserver.file;

public class FilenameUtils {
  public static final char EXTENSION_SEPARATOR = '.';
  private static final char UNIX_SEPARATOR = '/';
  private static final char WINDOWS_SEPARATOR = '\\';

  public static String getExtension(String filename) {
    if (filename == null) {
      return null;
    }
    int index = indexOfExtension(filename);
    if (index == -1) {
      return "";
    }
    return filename.substring(index + 1);
  }

  public static int indexOfExtension(String filename) {
    if (filename == null) {
      return -1;
    }
    int extensionPos = filename.lastIndexOf(46);
    if (indexOfLastSeparator(filename) > extensionPos) {
      extensionPos = -1;
    }
    return extensionPos;
  }

  public static int indexOfLastSeparator(String filename) {
    if (filename == null) {
      return -1;
    }
    return Math.max(filename.lastIndexOf(47), filename.lastIndexOf(92));
  }
}
