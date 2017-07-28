package com.android.mediaserver.musicg.math.quicksort;

public class QuickSortIndexPreserved {
  private QuickSort quickSort;

  public QuickSortIndexPreserved(int[] array) {
    this.quickSort = new QuickSortInteger(array);
  }

  public QuickSortIndexPreserved(double[] array) {
    this.quickSort = new QuickSortDouble(array);
  }

  public QuickSortIndexPreserved(short[] array) {
    this.quickSort = new QuickSortShort(array);
  }

  public int[] getSortIndexes() {
    return this.quickSort.getSortIndexes();
  }
}
