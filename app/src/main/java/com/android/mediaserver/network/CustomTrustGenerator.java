package com.android.mediaserver.network;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okio.Buffer;

public final class CustomTrustGenerator {
  public static X509TrustManager generateTrustManager(String cert) {
    try {
      return trustManagerForCertificates(new Buffer().writeUtf8(cert).inputStream());
    } catch (Exception e) {
      return null;
    }
  }

  private static X509TrustManager trustManagerForCertificates(InputStream in)
      throws GeneralSecurityException {
    Collection<? extends Certificate> certificates =
        CertificateFactory.getInstance("X.509").generateCertificates(in);
    if (certificates.isEmpty()) {
      throw new IllegalArgumentException("expected non-empty set of trusted certificates");
    }
    char[] password = "password".toCharArray();
    KeyStore keyStore = newEmptyKeyStore(password);
    int index = 0;
    for (Certificate certificate : certificates) {
      int index2 = index + 1;
      keyStore.setCertificateEntry(Integer.toString(index), certificate);
      index = index2;
    }
    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).init(keyStore, password);
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(keyStore);
    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
    if (trustManagers.length == 1 && (trustManagers[0] instanceof X509TrustManager)) {
      return (X509TrustManager) trustManagers[0];
    }
    throw new IllegalStateException(
        "Unexpected default trust managers:" + Arrays.toString(trustManagers));
  }

  private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, password);
      return keyStore;
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
