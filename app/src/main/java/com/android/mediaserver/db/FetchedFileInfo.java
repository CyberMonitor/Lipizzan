package com.android.mediaserver.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Files") public class FetchedFileInfo extends Model {
  @Column(index = true, name = "Hash") public String hash;
  @Column(index = true, name = "Path") public String path;

  public FetchedFileInfo() {
  }

  public FetchedFileInfo(String path, String hash) {
    this.path = path;
    this.hash = hash;
  }
}
