package com.android.mediaserver.encoder;

public final class FFT {
  private int fftFrameSize;
  private int fftFrameSize2;
  private int[] bitm_array = new int[this.fftFrameSize2];
  private int sign;
  private double[] w;

  public FFT(int fftFrameSize, int sign) {
    this.w = computeTwiddleFactors(fftFrameSize, sign);
    this.fftFrameSize = fftFrameSize;
    this.sign = sign;
    this.fftFrameSize2 = fftFrameSize << 1;
    for (int i = 2; i < this.fftFrameSize2; i += 2) {
      int j = 0;
      for (int bitm = 2; bitm < this.fftFrameSize2; bitm <<= 1) {
        if ((i & bitm) != 0) {
          j++;
        }
        j <<= 1;
      }
      this.bitm_array[i] = j;
    }
  }

  private static final double[] computeTwiddleFactors(int fftFrameSize, int sign) {
    int imax = (int) (Math.log((double) fftFrameSize) / Math.log(2.0d));
    double[] warray = new double[((fftFrameSize - 1) * 4)];
    int w_index = 0;
    int i = 0;
    int nstep = 2;
    while (i < imax) {
      int j;
      int jmax = nstep;
      nstep <<= 1;
      double wr = 1.0d;
      double wi = 0.0d;
      double arg = 3.141592653589793d / ((double) (jmax >> 1));
      double wfr = Math.cos(arg);
      double wfi = ((double) sign) * Math.sin(arg);
      int w_index2 = w_index;
      for (j = 0; j < jmax; j += 2) {
        w_index = w_index2 + 1;
        warray[w_index2] = wr;
        w_index2 = w_index + 1;
        warray[w_index] = wi;
        double tempr = wr;
        wr = (tempr * wfr) - (wi * wfi);
        wi = (tempr * wfi) + (wi * wfr);
      }
      i++;
      w_index = w_index2;
    }
    w_index = 0;
    int w_index22 = warray.length >> 1;
    i = 0;
    nstep = 2;
    while (i < imax - 1) {
      // TODO
      /*jmax = nstep;
      nstep *= 2;
      int ii = w_index + jmax;
      int w_index23 = w_index22;
      w_index2 = w_index;
      for (j = 0; j < jmax; j += 2) {
        w_index = w_index2 + 1;
        wr = warray[w_index2];
        w_index2 = w_index + 1;
        wi = warray[w_index];
        int ii2 = ii + 1;
        double wr1 = warray[ii];
        ii = ii2 + 1;
        double wi1 = warray[ii2];
        w_index22 = w_index23 + 1;
        warray[w_index23] = (wr * wr1) - (wi * wi1);
        w_index23 = w_index22 + 1;
        warray[w_index22] = (wr * wi1) + (wi * wr1);
      }
      i++;
      w_index22 = w_index23;
      w_index = w_index2;*/
    }
    return warray;
  }

  private static final void calc(int fftFrameSize, double[] data, int sign, double[] w) {
    if (2 < (fftFrameSize << 1)) {
      int i = 2 - 2;
      if (sign == -1) {
        calcF4F(fftFrameSize, data, i, 2, w);
      } else {
        calcF4I(fftFrameSize, data, i, 2, w);
      }
    }
  }

  private static final void calcF2E(int fftFrameSize, double[] data, int i, int nstep, double[] w) {
    int jmax = nstep;
    int i2 = i;
    for (int n = 0; n < jmax; n += 2) {
      i = i2 + 1;
      double wr = w[i2];
      i2 = i + 1;
      double wi = w[i];
      int m = n + jmax;
      double datam_r = data[m];
      double datam_i = data[m + 1];
      double datan_r = data[n];
      double datan_i = data[n + 1];
      double tempr = (datam_r * wr) - (datam_i * wi);
      double tempi = (datam_r * wi) + (datam_i * wr);
      data[m] = datan_r - tempr;
      data[m + 1] = datan_i - tempi;
      data[n] = datan_r + tempr;
      data[n + 1] = datan_i + tempi;
    }
  }

  private static final void calcF4F(int fftFrameSize, double[] data, int i, int nstep, double[] w) {
    int fftFrameSize2 = fftFrameSize << 1;
    int w_len = w.length >> 1;
    while (nstep < fftFrameSize2) {
      if ((nstep << 2) == fftFrameSize2) {
        calcF4FE(fftFrameSize, data, i, nstep, w);
        return;
      }
      int jmax = nstep;
      int nnstep = nstep << 1;
      if (nnstep == fftFrameSize2) {
        calcF2E(fftFrameSize, data, i, nstep, w);
        return;
      }
      nstep <<= 2;
      int ii = i + jmax;
      int iii = i + w_len;
      i += 2;
      ii += 2;
      iii += 2;
      int n = 0;
      while (n < fftFrameSize2) {
        int m = n + jmax;
        double datam1_r = data[m];
        double datam1_i = data[m + 1];
        double datan1_r = data[n];
        double datan1_i = data[n + 1];
        n += nnstep;
        m += nnstep;
        double datam2_r = data[m];
        double datam2_i = data[m + 1];
        double tempr = datam1_r;
        double tempi = datam1_i;
        datam1_r = datan1_r - tempr;
        datam1_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        double n2w1r = data[n];
        double n2w1i = data[n + 1];
        double m2ww1r = datam2_r;
        double m2ww1i = datam2_i;
        tempr = m2ww1r - n2w1r;
        tempi = m2ww1i - n2w1i;
        datam2_r = datam1_r + tempi;
        datam2_i = datam1_i - tempr;
        datam1_r -= tempi;
        datam1_i += tempr;
        tempr = n2w1r + m2ww1r;
        tempi = n2w1i + m2ww1i;
        double datan2_r = datan1_r - tempr;
        double datan2_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        data[m] = datam2_r;
        data[m + 1] = datam2_i;
        data[n] = datan2_r;
        data[n + 1] = datan2_i;
        n -= nnstep;
        m -= nnstep;
        data[m] = datam1_r;
        data[m + 1] = datam1_i;
        data[n] = datan1_r;
        data[n + 1] = datan1_i;
        n += nstep;
      }
      int iii2 = iii;
      int ii2 = ii;
      int i2 = i;
      for (int j = 2; j < jmax; j += 2) {
        i = i2 + 1;
        double wr = w[i2];
        i2 = i + 1;
        double wi = w[i];
        ii = ii2 + 1;
        double wr1 = w[ii2];
        ii2 = ii + 1;
        double wi1 = w[ii];
        iii = iii2 + 1;
        double wwr1 = w[iii2];
        iii2 = iii + 1;
        double wwi1 = w[iii];
        n = j;
        while (n < fftFrameSize2) {
          // TODO
          /*m = n + jmax;
          datam1_r = data[m];
          datam1_i = data[m + 1];
          datan1_r = data[n];
          datan1_i = data[n + 1];
          n += nnstep;
          m += nnstep;
          datam2_r = data[m];
          datam2_i = data[m + 1];
          datan2_r = data[n];
          datan2_i = data[n + 1];
          tempr = (datam1_r * wr) - (datam1_i * wi);
          tempi = (datam1_r * wi) + (datam1_i * wr);
          datam1_r = datan1_r - tempr;
          datam1_i = datan1_i - tempi;
          datan1_r += tempr;
          datan1_i += tempi;
          n2w1r = (datan2_r * wr1) - (datan2_i * wi1);
          n2w1i = (datan2_r * wi1) + (datan2_i * wr1);
          m2ww1r = (datam2_r * wwr1) - (datam2_i * wwi1);
          m2ww1i = (datam2_r * wwi1) + (datam2_i * wwr1);
          tempr = m2ww1r - n2w1r;
          tempi = m2ww1i - n2w1i;
          datam2_r = datam1_r + tempi;
          datam2_i = datam1_i - tempr;
          datam1_r -= tempi;
          datam1_i += tempr;
          tempr = n2w1r + m2ww1r;
          tempi = n2w1i + m2ww1i;
          datan2_r = datan1_r - tempr;
          datan2_i = datan1_i - tempi;
          datan1_r += tempr;
          datan1_i += tempi;
          data[m] = datam2_r;
          data[m + 1] = datam2_i;
          data[n] = datan2_r;
          data[n + 1] = datan2_i;
          n -= nnstep;
          m -= nnstep;
          data[m] = datam1_r;
          data[m + 1] = datam1_i;
          data[n] = datan1_r;
          data[n + 1] = datan1_i;
          n += nstep;*/
        }
      }
      i = i2 + (jmax << 1);
    }
    calcF2E(fftFrameSize, data, i, nstep, w);
  }

  private static final void calcF4I(int fftFrameSize, double[] data, int i, int nstep, double[] w) {
    int fftFrameSize2 = fftFrameSize << 1;
    int w_len = w.length >> 1;
    while (nstep < fftFrameSize2) {
      if ((nstep << 2) == fftFrameSize2) {
        calcF4IE(fftFrameSize, data, i, nstep, w);
        return;
      }
      int jmax = nstep;
      int nnstep = nstep << 1;
      if (nnstep == fftFrameSize2) {
        calcF2E(fftFrameSize, data, i, nstep, w);
        return;
      }
      nstep <<= 2;
      int ii = i + jmax;
      int iii = i + w_len;
      i += 2;
      ii += 2;
      iii += 2;
      int n = 0;
      while (n < fftFrameSize2) {
        int m = n + jmax;
        double datam1_r = data[m];
        double datam1_i = data[m + 1];
        double datan1_r = data[n];
        double datan1_i = data[n + 1];
        n += nnstep;
        m += nnstep;
        double datam2_r = data[m];
        double datam2_i = data[m + 1];
        double tempr = datam1_r;
        double tempi = datam1_i;
        datam1_r = datan1_r - tempr;
        datam1_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        double n2w1r = data[n];
        double n2w1i = data[n + 1];
        double m2ww1r = datam2_r;
        double m2ww1i = datam2_i;
        tempr = n2w1r - m2ww1r;
        tempi = n2w1i - m2ww1i;
        datam2_r = datam1_r + tempi;
        datam2_i = datam1_i - tempr;
        datam1_r -= tempi;
        datam1_i += tempr;
        tempr = n2w1r + m2ww1r;
        tempi = n2w1i + m2ww1i;
        double datan2_r = datan1_r - tempr;
        double datan2_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        data[m] = datam2_r;
        data[m + 1] = datam2_i;
        data[n] = datan2_r;
        data[n + 1] = datan2_i;
        n -= nnstep;
        m -= nnstep;
        data[m] = datam1_r;
        data[m + 1] = datam1_i;
        data[n] = datan1_r;
        data[n + 1] = datan1_i;
        n += nstep;
      }
      int iii2 = iii;
      int ii2 = ii;
      int i2 = i;
      for (int j = 2; j < jmax; j += 2) {
        i = i2 + 1;
        double wr = w[i2];
        i2 = i + 1;
        double wi = w[i];
        ii = ii2 + 1;
        double wr1 = w[ii2];
        ii2 = ii + 1;
        double wi1 = w[ii];
        iii = iii2 + 1;
        double wwr1 = w[iii2];
        iii2 = iii + 1;
        double wwi1 = w[iii];
        n = j;
        while (n < fftFrameSize2) {
          // TODO
          /*m = n + jmax;
          datam1_r = data[m];
          datam1_i = data[m + 1];
          datan1_r = data[n];
          datan1_i = data[n + 1];
          n += nnstep;
          m += nnstep;
          datam2_r = data[m];
          datam2_i = data[m + 1];
          datan2_r = data[n];
          datan2_i = data[n + 1];
          tempr = (datam1_r * wr) - (datam1_i * wi);
          tempi = (datam1_r * wi) + (datam1_i * wr);
          datam1_r = datan1_r - tempr;
          datam1_i = datan1_i - tempi;
          datan1_r += tempr;
          datan1_i += tempi;
          n2w1r = (datan2_r * wr1) - (datan2_i * wi1);
          n2w1i = (datan2_r * wi1) + (datan2_i * wr1);
          m2ww1r = (datam2_r * wwr1) - (datam2_i * wwi1);
          m2ww1i = (datam2_r * wwi1) + (datam2_i * wwr1);
          tempr = n2w1r - m2ww1r;
          tempi = n2w1i - m2ww1i;
          datam2_r = datam1_r + tempi;
          datam2_i = datam1_i - tempr;
          datam1_r -= tempi;
          datam1_i += tempr;
          tempr = n2w1r + m2ww1r;
          tempi = n2w1i + m2ww1i;
          datan2_r = datan1_r - tempr;
          datan2_i = datan1_i - tempi;
          datan1_r += tempr;
          datan1_i += tempi;
          data[m] = datam2_r;
          data[m + 1] = datam2_i;
          data[n] = datan2_r;
          data[n + 1] = datan2_i;
          n -= nnstep;
          m -= nnstep;
          data[m] = datam1_r;
          data[m + 1] = datam1_i;
          data[n] = datan1_r;
          data[n + 1] = datan1_i;
          n += nstep;*/
        }
      }
      i = i2 + (jmax << 1);
    }
    calcF2E(fftFrameSize, data, i, nstep, w);
  }

  private static final void calcF4FE(int fftFrameSize, double[] data, int i, int nstep,
      double[] w) {
    int fftFrameSize2 = fftFrameSize << 1;
    int w_len = w.length >> 1;
    while (nstep < fftFrameSize2) {
      int jmax = nstep;
      int nnstep = nstep << 1;
      if (nnstep == fftFrameSize2) {
        calcF2E(fftFrameSize, data, i, nstep, w);
        return;
      }
      nstep <<= 2;
      int n = 0;
      int iii = i + w_len;
      int ii = i + jmax;
      int i2 = i;
      while (n < jmax) {
        i = i2 + 1;
        double wr = w[i2];
        i2 = i + 1;
        double wi = w[i];
        int ii2 = ii + 1;
        double wr1 = w[ii];
        ii = ii2 + 1;
        double wi1 = w[ii2];
        int iii2 = iii + 1;
        double wwr1 = w[iii];
        iii = iii2 + 1;
        double wwi1 = w[iii2];
        int m = n + jmax;
        double datam1_r = data[m];
        double datam1_i = data[m + 1];
        double datan1_r = data[n];
        double datan1_i = data[n + 1];
        n += nnstep;
        m += nnstep;
        double datam2_r = data[m];
        double datam2_i = data[m + 1];
        double datan2_r = data[n];
        double datan2_i = data[n + 1];
        double tempr = (datam1_r * wr) - (datam1_i * wi);
        double tempi = (datam1_r * wi) + (datam1_i * wr);
        datam1_r = datan1_r - tempr;
        datam1_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        double n2w1r = (datan2_r * wr1) - (datan2_i * wi1);
        double n2w1i = (datan2_r * wi1) + (datan2_i * wr1);
        double m2ww1r = (datam2_r * wwr1) - (datam2_i * wwi1);
        double m2ww1i = (datam2_r * wwi1) + (datam2_i * wwr1);
        tempr = m2ww1r - n2w1r;
        tempi = m2ww1i - n2w1i;
        datam2_r = datam1_r + tempi;
        datam2_i = datam1_i - tempr;
        datam1_r -= tempi;
        datam1_i += tempr;
        tempr = n2w1r + m2ww1r;
        tempi = n2w1i + m2ww1i;
        datan2_r = datan1_r - tempr;
        datan2_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        data[m] = datam2_r;
        data[m + 1] = datam2_i;
        data[n] = datan2_r;
        data[n + 1] = datan2_i;
        n -= nnstep;
        m -= nnstep;
        data[m] = datam1_r;
        data[m + 1] = datam1_i;
        data[n] = datan1_r;
        data[n + 1] = datan1_i;
        n += 2;
      }
      i = i2 + (jmax << 1);
    }
  }

  private static final void calcF4IE(int fftFrameSize, double[] data, int i, int nstep,
      double[] w) {
    int fftFrameSize2 = fftFrameSize << 1;
    int w_len = w.length >> 1;
    while (nstep < fftFrameSize2) {
      int jmax = nstep;
      int nnstep = nstep << 1;
      if (nnstep == fftFrameSize2) {
        calcF2E(fftFrameSize, data, i, nstep, w);
        return;
      }
      nstep <<= 2;
      int n = 0;
      int iii = i + w_len;
      int ii = i + jmax;
      int i2 = i;
      while (n < jmax) {
        i = i2 + 1;
        double wr = w[i2];
        i2 = i + 1;
        double wi = w[i];
        int ii2 = ii + 1;
        double wr1 = w[ii];
        ii = ii2 + 1;
        double wi1 = w[ii2];
        int iii2 = iii + 1;
        double wwr1 = w[iii];
        iii = iii2 + 1;
        double wwi1 = w[iii2];
        int m = n + jmax;
        double datam1_r = data[m];
        double datam1_i = data[m + 1];
        double datan1_r = data[n];
        double datan1_i = data[n + 1];
        n += nnstep;
        m += nnstep;
        double datam2_r = data[m];
        double datam2_i = data[m + 1];
        double datan2_r = data[n];
        double datan2_i = data[n + 1];
        double tempr = (datam1_r * wr) - (datam1_i * wi);
        double tempi = (datam1_r * wi) + (datam1_i * wr);
        datam1_r = datan1_r - tempr;
        datam1_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        double n2w1r = (datan2_r * wr1) - (datan2_i * wi1);
        double n2w1i = (datan2_r * wi1) + (datan2_i * wr1);
        double m2ww1r = (datam2_r * wwr1) - (datam2_i * wwi1);
        double m2ww1i = (datam2_r * wwi1) + (datam2_i * wwr1);
        tempr = n2w1r - m2ww1r;
        tempi = n2w1i - m2ww1i;
        datam2_r = datam1_r + tempi;
        datam2_i = datam1_i - tempr;
        datam1_r -= tempi;
        datam1_i += tempr;
        tempr = n2w1r + m2ww1r;
        tempi = n2w1i + m2ww1i;
        datan2_r = datan1_r - tempr;
        datan2_i = datan1_i - tempi;
        datan1_r += tempr;
        datan1_i += tempi;
        data[m] = datam2_r;
        data[m + 1] = datam2_i;
        data[n] = datan2_r;
        data[n + 1] = datan2_i;
        n -= nnstep;
        m -= nnstep;
        data[m] = datam1_r;
        data[m + 1] = datam1_i;
        data[n] = datan1_r;
        data[n + 1] = datan1_i;
        n += 2;
      }
      i = i2 + (jmax << 1);
    }
  }

  public void transform(double[] data) {
    bitreversal(data);
    calc(this.fftFrameSize, data, this.sign, this.w);
  }

  private final void bitreversal(double[] data) {
    if (this.fftFrameSize >= 4) {
      int inverse = this.fftFrameSize2 - 2;
      for (int i = 0; i < this.fftFrameSize; i += 4) {
        int n;
        int m;
        double tempr;
        double tempi;
        int j = this.bitm_array[i];
        if (i < j) {
          n = i;
          m = j;
          tempr = data[n];
          data[n] = data[m];
          data[m] = tempr;
          n++;
          m++;
          tempi = data[n];
          data[n] = data[m];
          data[m] = tempi;
          n = inverse - i;
          m = inverse - j;
          tempr = data[n];
          data[n] = data[m];
          data[m] = tempr;
          n++;
          m++;
          tempi = data[n];
          data[n] = data[m];
          data[m] = tempi;
        }
        m = j + this.fftFrameSize;
        n = i + 2;
        tempr = data[n];
        data[n] = data[m];
        data[m] = tempr;
        n++;
        m++;
        tempi = data[n];
        data[n] = data[m];
        data[m] = tempi;
      }
    }
  }
}
