package com.android.mediaserver.fetchers;

import java.io.File;

public class FetchedFile {
  private File mFile;
  private String mSubtype;
  private String mType;

  public FetchedFile(File file, String type, String subtype) {
    this.mFile = file;
    this.mType = type;
    this.mSubtype = subtype;
  }

  public File getFile() {
    return this.mFile;
  }

  public void setFile(File file) {
    this.mFile = file;
  }

  public String getType() {
    return this.mType;
  }

  public String getSubtype() {
    return this.mSubtype;
  }
}
