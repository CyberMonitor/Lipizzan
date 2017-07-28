package com.android.mediaserver.musicg.math.rank;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapRankDouble implements MapRank {
  private boolean acsending = true;
  private Map map;

  public MapRankDouble(Map<?, Double> map, boolean acsending) {
    this.map = map;
    this.acsending = acsending;
  }

  public List getOrderedKeyList(int numKeys, boolean sharpLimit) {
    Set<Entry> mapEntrySet = this.map.entrySet();
    List keyList = new LinkedList();
    if (numKeys > this.map.size()) {
      numKeys = this.map.size();
    }
    if (this.map.size() > 0) {
      Entry entry;
      int targetindex;
      int index;
      double[] array = new double[this.map.size()];
      int count = 0;
      for (Entry entry2 : mapEntrySet) {
        int count2 = count + 1;
        array[count] = ((Double) entry2.getValue()).doubleValue();
        count = count2;
      }
      if (this.acsending) {
        targetindex = numKeys;
      } else {
        targetindex = array.length - numKeys;
      }
      double passValue = getOrderedValue(array, targetindex);
      Map passedMap = new HashMap();
      List<Double> valueList = new LinkedList();
      for (Entry entry22 : mapEntrySet) {
        double value = ((Double) entry22.getValue()).doubleValue();
        if ((this.acsending && value <= passValue) || (!this.acsending && value >= passValue)) {
          passedMap.put(entry22.getKey(), Double.valueOf(value));
          valueList.add(Double.valueOf(value));
        }
      }
      Double[] listArr = new Double[valueList.size()];
      valueList.toArray(listArr);
      Arrays.sort(listArr);
      int resultCount = 0;
      if (this.acsending) {
        index = 0;
      } else {
        index = listArr.length - 1;
      }
      if (!sharpLimit) {
        numKeys = listArr.length;
      }
      do {
        double targetValue = listArr[index].doubleValue();
        Iterator<Entry> passedMapIterator = passedMap.entrySet().iterator();
        while (passedMapIterator.hasNext()) {
          Entry entry22 = (Entry) passedMapIterator.next();
          if (((Double) entry22.getValue()).doubleValue() == targetValue) {
            keyList.add(entry22.getKey());
            passedMapIterator.remove();
            resultCount++;
            break;
          }
        }
        if (this.acsending) {
          index++;
        } else {
          index--;
        }
      } while (resultCount < numKeys);
    }
    return keyList;
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
