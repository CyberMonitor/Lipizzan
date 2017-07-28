package com.android.mediaserver.musicg.math.quicksort;

public class QuickSortInteger extends QuickSort {
  private int[] array;
  private int[] indexes;

  public QuickSortInteger(int[] array) {
    this.array = array;
    this.indexes = new int[array.length];
    for (int i = 0; i < this.indexes.length; i++) {
      this.indexes[i] = i;
    }
  }

  public int[] getSortIndexes() {
    sort();
    return this.indexes;
  }

  private void sort() {
    quicksort(this.array, this.indexes, 0, this.indexes.length - 1);
  }

  private void quicksort(int[] a, int[] indexes, int left, int right) {
    if (right > left) {
      int i = partition(a, indexes, left, right);
      quicksort(a, indexes, left, i - 1);
      quicksort(a, indexes, i + 1, right);
    }
  }

  private int partition(int[] a, int[] indexes, int left, int right) {
    int i = left - 1;
    int j = right;
    while (true) {
      i++;
      if (a[indexes[i]] >= a[indexes[right]]) {
        do {
          j--;
          if (a[indexes[right]] >= a[indexes[j]]) {
            break;
          }
        } while (j != left);
        if (i >= j) {
          swap(a, indexes, i, right);
          return i;
        }
        swap(a, indexes, i, j);
      }
    }
  }

  private void swap(int[] a, int[] indexes, int i, int j) {
    int swap = indexes[i];
    indexes[i] = indexes[j];
    indexes[j] = swap;
  }
}
