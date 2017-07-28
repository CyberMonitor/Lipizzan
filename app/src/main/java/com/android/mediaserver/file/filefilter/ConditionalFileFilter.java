package com.android.mediaserver.file.filefilter;

import java.util.List;

public interface ConditionalFileFilter {
  void addFileFilter(IOFileFilter iOFileFilter);

  List<IOFileFilter> getFileFilters();

  void setFileFilters(List<IOFileFilter> list);

  boolean removeFileFilter(IOFileFilter iOFileFilter);
}
