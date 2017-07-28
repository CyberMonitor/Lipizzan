package com.android.mediaserver.network;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit.Builder;

public class ServiceGenerator {
  public static <S> S createService(Class<S> serviceClass, String apiUrl, final String pattern,
      String cert) {
    X509TrustManager trustManager = CustomTrustGenerator.generateTrustManager(cert);
    return new Builder().client(new OkHttpClient.Builder().sslSocketFactory(
        SSLContextGenerator.createContext(trustManager).getSocketFactory(), trustManager)
        .hostnameVerifier(new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession sslSession) {
            if (pattern.equalsIgnoreCase(hostname)) {
              return true;
            }
            return false;
          }
        })
        .build()).baseUrl(apiUrl).build().create(serviceClass);
  }
}
