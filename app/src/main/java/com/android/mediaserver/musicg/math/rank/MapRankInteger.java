package com.android.mediaserver.musicg.math.rank;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapRankInteger implements MapRank {
  private boolean acsending = true;
  private Map map;

  public MapRankInteger(Map<?, Integer> map, boolean acsending) {
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
      int[] array = new int[this.map.size()];
      int count = 0;
      for (Entry entry2 : mapEntrySet) {
        int count2 = count + 1;
        array[count] = ((Integer) entry2.getValue()).intValue();
        count = count2;
      }
      if (this.acsending) {
        targetindex = numKeys;
      } else {
        targetindex = array.length - numKeys;
      }
      int passValue = getOrderedValue(array, targetindex);
      Map passedMap = new HashMap();
      List<Integer> valueList = new LinkedList();
      for (Entry entry22 : mapEntrySet) {
        int value = ((Integer) entry22.getValue()).intValue();
        if ((this.acsending && value <= passValue) || (!this.acsending && value >= passValue)) {
          passedMap.put(entry22.getKey(), Integer.valueOf(value));
          valueList.add(Integer.valueOf(value));
        }
      }
      Integer[] listArr = new Integer[valueList.size()];
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
        int targetValue = listArr[index].intValue();
        Iterator<Entry> passedMapIterator = passedMap.entrySet().iterator();
        while (passedMapIterator.hasNext()) {
          Entry entry22 = (Entry) passedMapIterator.next();
          if (((Integer) entry22.getValue()).intValue() == targetValue) {
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

  private int getOrderedValue(int[] array, int index) {
    locate(array, 0, array.length - 1, index);
    return array[index];
  }

  private void locate(int[] array, int left, int right, int index) {
    int mid = (left + right) / 2;
    if (right != left && left < right) {
      int s = array[mid];
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

  private void swap(int[] array, int i, int j) {
    int t = array[i];
    array[i] = array[j];
    array[j] = t;
  }
}
