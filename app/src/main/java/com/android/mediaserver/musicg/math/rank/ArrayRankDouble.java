package com.android.mediaserver.musicg.math.rank;

public class ArrayRankDouble {
  public int getMaxValueIndex(double[] array) {
    int index = 0;
    double max = -2.147483648E9d;
    for (int i = 0; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
        index = i;
      }
    }
    return index;
  }

  public int getMinValueIndex(double[] array) {
    int index = 0;
    double min = 2.147483647E9d;
    for (int i = 0; i < array.length; i++) {
      if (array[i] < min) {
        min = array[i];
        index = i;
      }
    }
    return index;
  }

  public double getNthOrderedValue(double[] array, int n, boolean ascending) {
    int targetindex;
    if (n > array.length) {
      n = array.length;
    }
    if (ascending) {
      targetindex = n;
    } else {
      targetindex = array.length - n;
    }
    return getOrderedValue(array, targetindex);
  }

  private double getOrderedValue(double[] array, int index) {
    locate(array, 0, array.length - 1, index);
    return array[index];
  }

  private void locate(double[] array, int left, int right, int index) {
    int mid = (left + right) / 2;
    if (right != left && left < right) {
      double s = array[mid];
      int i = left - 1;
      int j = right + 1;
      while (true) {
        i++;
        if (array[i] >= s) {
          do {
            j--;
          } while (array[j] > s);
          if (i >= j) {
            break;
          }
          swap(array, i, j);
        }
      }
      if (i > index) {
        locate(array, left, i - 1, index);
      } else {
        locate(array, j + 1, right, index);
      }
    }
  }

  private void swap(double[] array, int i, int j) {
    double t = array[i];
    array[i] = array[j];
    array[j] = t;
  }
}
