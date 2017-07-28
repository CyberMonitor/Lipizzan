package com.android.mediaserver.musicg.math.rank;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapRankShort implements MapRank {
  private boolean acsending = true;
  private Map map;

  public MapRankShort(Map<?, Short> map, boolean acsending) {
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
      short[] array = new short[this.map.size()];
      int count = 0;
      for (Entry entry2 : mapEntrySet) {
        int count2 = count + 1;
        array[count] = ((Short) entry2.getValue()).shortValue();
        count = count2;
      }
      if (this.acsending) {
        targetindex = numKeys;
      } else {
        targetindex = array.length - numKeys;
      }
      short passValue = getOrderedValue(array, targetindex);
      Map passedMap = new HashMap();
      List<Short> valueList = new LinkedList();
      for (Entry entry22 : mapEntrySet) {
        short value = ((Short) entry22.getValue()).shortValue();
        if ((this.acsending && value <= passValue) || (!this.acsending && value >= passValue)) {
          passedMap.put(entry22.getKey(), Short.valueOf(value));
          valueList.add(Short.valueOf(value));
        }
      }
      Short[] listArr = new Short[valueList.size()];
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
        short targetValue = listArr[index].shortValue();
        Iterator<Entry> passedMapIterator = passedMap.entrySet().iterator();
        while (passedMapIterator.hasNext()) {
          Entry entry22 = (Entry) passedMapIterator.next();
          if (((Short) entry22.getValue()).shortValue() == targetValue) {
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

  private short getOrderedValue(short[] array, int index) {
    locate(array, 0, array.length - 1, index);
    return array[index];
  }

  private void locate(short[] array, int left, int right, int index) {
    int mid = (left + right) / 2;
    if (right != left && left < right) {
      short s = array[mid];
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

  private void swap(short[] array, int i, int j) {
    short t = array[i];
    array[i] = array[j];
    array[j] = t;
  }
}
