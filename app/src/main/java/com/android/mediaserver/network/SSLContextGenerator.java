package com.android.mediaserver.network;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class SSLContextGenerator {
  public static SSLContext createContext(X509TrustManager trustManager) {
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[] { trustManager }, null);
      return sslContext;
    } catch (Exception e) {
      return null;
    }
  }
}
