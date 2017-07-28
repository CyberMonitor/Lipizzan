package com.android.mediaserver.file;

import com.android.mediaserver.file.filefilter.DirectoryFileFilter;
import com.android.mediaserver.file.filefilter.FalseFileFilter;
import com.android.mediaserver.file.filefilter.FileFilterUtils;
import com.android.mediaserver.file.filefilter.IOFileFilter;
import com.android.mediaserver.file.filefilter.SuffixFileFilter;
import com.android.mediaserver.file.filefilter.TrueFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class FileLister {
  private static void innerListFiles(Collection<File> files, File directory, IOFileFilter filter,
      boolean includeSubDirectories) {
    File[] found = directory.listFiles((FileFilter) filter);
    if (found != null) {
      for (File file : found) {
        if (file.isDirectory()) {
          if (includeSubDirectories) {
            files.add(file);
          }
          innerListFiles(files, file, filter, includeSubDirectories);
        } else {
          files.add(file);
        }
      }
    }
  }

  public static Collection<File> listFiles(File directory, IOFileFilter fileFilter,
      IOFileFilter dirFilter) {
    validateListFilesParameters(directory, fileFilter);
    IOFileFilter effFileFilter = setUpEffectiveFileFilter(fileFilter);
    IOFileFilter effDirFilter = setUpEffectiveDirFilter(dirFilter);
    Collection<File> files = new LinkedList();
    innerListFiles(files, directory, FileFilterUtils.or(effFileFilter, effDirFilter), false);
    return files;
  }

  private static void validateListFilesParameters(File directory, IOFileFilter fileFilter) {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("Parameter 'directory' is not a directory");
    } else if (fileFilter == null) {
      throw new NullPointerException("Parameter 'fileFilter' is null");
    }
  }

  private static IOFileFilter setUpEffectiveFileFilter(IOFileFilter fileFilter) {
    return FileFilterUtils.and(fileFilter,
        FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
  }

  private static IOFileFilter setUpEffectiveDirFilter(IOFileFilter dirFilter) {
    if (dirFilter == null) {
      return FalseFileFilter.INSTANCE;
    }
    return FileFilterUtils.and(dirFilter, DirectoryFileFilter.INSTANCE);
  }

  public static Collection<File> listFilesAndDirs(File directory, IOFileFilter fileFilter,
      IOFileFilter dirFilter) {
    validateListFilesParameters(directory, fileFilter);
    IOFileFilter effFileFilter = setUpEffectiveFileFilter(fileFilter);
    IOFileFilter effDirFilter = setUpEffectiveDirFilter(dirFilter);
    Collection<File> files = new LinkedList();
    if (directory.isDirectory()) {
      files.add(directory);
    }
    innerListFiles(files, directory, FileFilterUtils.or(effFileFilter, effDirFilter), true);
    return files;
  }

  public static Iterator<File> iterateFiles(File directory, IOFileFilter fileFilter,
      IOFileFilter dirFilter) {
    return listFiles(directory, fileFilter, dirFilter).iterator();
  }

  public static Iterator<File> iterateFilesAndDirs(File directory, IOFileFilter fileFilter,
      IOFileFilter dirFilter) {
    return listFilesAndDirs(directory, fileFilter, dirFilter).iterator();
  }

  private static String[] toSuffixes(String[] extensions) {
    String[] suffixes = new String[extensions.length];
    for (int i = 0; i < extensions.length; i++) {
      suffixes[i] = "." + extensions[i];
    }
    return suffixes;
  }

  public static Collection<File> listFiles(File directory, String[] extensions, boolean recursive) {
    IOFileFilter filter;
    if (extensions == null) {
      filter = TrueFileFilter.INSTANCE;
    } else {
      filter = new SuffixFileFilter(toSuffixes(extensions));
    }
    return listFiles(directory, filter,
        recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE);
  }

  public static Iterator<File> iterateFiles(File directory, String[] extensions,
      boolean recursive) {
    return listFiles(directory, extensions, recursive).iterator();
  }
}
