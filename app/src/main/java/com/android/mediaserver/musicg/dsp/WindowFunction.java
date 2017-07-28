package com.android.mediaserver.musicg.dsp;

public class WindowFunction {
  public static final int BARTLETT = 1;
  public static final int BLACKMAN = 4;
  public static final int HAMMING = 3;
  public static final int HANNING = 2;
  public static final int RECTANGULAR = 0;
  int windowType = 0;

  public void setWindowType(int wt) {
    this.windowType = wt;
  }

  public int getWindowType() {
    return this.windowType;
  }

  public void setWindowType(String w) {
    if (w.toUpperCase().equals("RECTANGULAR")) {
      this.windowType = 0;
    }
    if (w.toUpperCase().equals("BARTLETT")) {
      this.windowType = 1;
    }
    if (w.toUpperCase().equals("HANNING")) {
      this.windowType = 2;
    }
    if (w.toUpperCase().equals("HAMMING")) {
      this.windowType = 3;
    }
    if (w.toUpperCase().equals("BLACKMAN")) {
      this.windowType = 4;
    }
  }

  public double[] generate(int nSamples) {
    int m = nSamples / 2;
    double[] w = new double[nSamples];
    int n;
    double r;
    switch (this.windowType) {
      case 1:
        for (n = 0; n < nSamples; n++) {
          w[n] = (double) (1.0f - ((float) (Math.abs(n - m) / m)));
        }
        break;
      case 2:
        r = 3.141592653589793d / ((double) (m + 1));
        for (n = -m; n < m; n++) {
          w[m + n] = 0.5d + (0.5d * Math.cos(((double) n) * r));
        }
        break;
      case 3:
        r = 3.141592653589793d / ((double) m);
        for (n = -m; n < m; n++) {
          w[m + n] = 0.5400000214576721d + (0.46000000834465027d * Math.cos(((double) n) * r));
        }
        break;
      case 4:
        r = 3.141592653589793d / ((double) m);
        for (n = -m; n < m; n++) {
          w[m + n] = (0.41999998688697815d + (0.5d * Math.cos(((double) n) * r))) + (
              0.07999999821186066d
                  * Math.cos(((double) (n * 2)) * r));
        }
        break;
      default:
        for (n = 0; n < nSamples; n++) {
          w[n] = 1.0d;
        }
        break;
    }
    return w;
  }
}
