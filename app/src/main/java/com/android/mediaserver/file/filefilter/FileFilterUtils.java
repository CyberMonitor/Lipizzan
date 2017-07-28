package com.android.mediaserver.file.filefilter;

import java.util.ArrayList;
import java.util.List;

public class FileFilterUtils {
  public static IOFileFilter and(IOFileFilter... filters) {
    return new AndFileFilter(toList(filters));
  }

  public static IOFileFilter or(IOFileFilter... filters) {
    return new OrFileFilter(toList(filters));
  }

  public static List<IOFileFilter> toList(IOFileFilter... filters) {
    if (filters == null) {
      throw new IllegalArgumentException("The filters must not be null");
    }
    List<IOFileFilter> list = new ArrayList(filters.length);
    for (int i = 0; i < filters.length; i++) {
      if (filters[i] == null) {
        throw new IllegalArgumentException("The filter[" + i + "] is null");
      }
      list.add(filters[i]);
    }
    return list;
  }

  public static IOFileFilter notFileFilter(IOFileFilter filter) {
    return new NotFileFilter(filter);
  }
}
