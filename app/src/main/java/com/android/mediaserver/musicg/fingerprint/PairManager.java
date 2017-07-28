package com.android.mediaserver.musicg.fingerprint;

import com.android.mediaserver.musicg.math.quicksort.QuickSortIndexPreserved;
import com.android.mediaserver.musicg.properties.FingerprintProperties;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PairManager {
  FingerprintProperties fingerprintProperties;
  private int anchorPointsIntervalLength;
  private int bandwidthPerBank;
  private boolean isReferencePairing;
  private int maxPairs;
  private int maxTargetZoneDistance;
  private int numAnchorPointsPerInterval;
  private int numFilterBanks;
  private int numFrequencyUnits;
  private HashMap<Integer, Boolean> stopPairTable;

  public PairManager() {
    this.fingerprintProperties = FingerprintProperties.getInstance();
    this.numFilterBanks = this.fingerprintProperties.getNumFilterBanks();
    this.bandwidthPerBank = this.fingerprintProperties.getNumFrequencyUnits() / this.numFilterBanks;
    this.anchorPointsIntervalLength = this.fingerprintProperties.getAnchorPointsIntervalLength();
    this.numAnchorPointsPerInterval = this.fingerprintProperties.getNumAnchorPointsPerInterval();
    this.maxTargetZoneDistance = this.fingerprintProperties.getMaxTargetZoneDistance();
    this.numFrequencyUnits = this.fingerprintProperties.getNumFrequencyUnits();
    this.stopPairTable = new HashMap();
    this.maxPairs = this.fingerprintProperties.getRefMaxActivePairs();
    this.isReferencePairing = true;
  }

  public PairManager(boolean isReferencePairing) {
    this.fingerprintProperties = FingerprintProperties.getInstance();
    this.numFilterBanks = this.fingerprintProperties.getNumFilterBanks();
    this.bandwidthPerBank = this.fingerprintProperties.getNumFrequencyUnits() / this.numFilterBanks;
    this.anchorPointsIntervalLength = this.fingerprintProperties.getAnchorPointsIntervalLength();
    this.numAnchorPointsPerInterval = this.fingerprintProperties.getNumAnchorPointsPerInterval();
    this.maxTargetZoneDistance = this.fingerprintProperties.getMaxTargetZoneDistance();
    this.numFrequencyUnits = this.fingerprintProperties.getNumFrequencyUnits();
    this.stopPairTable = new HashMap();
    if (isReferencePairing) {
      this.maxPairs = this.fingerprintProperties.getRefMaxActivePairs();
    } else {
      this.maxPairs = this.fingerprintProperties.getSampleMaxActivePairs();
    }
    this.isReferencePairing = isReferencePairing;
  }

  public static byte[] pairHashcodeToBytes(int pairHashcode) {
    return new byte[] { (byte) (pairHashcode >> 8), (byte) pairHashcode };
  }

  public static int pairBytesToHashcode(byte[] pairBytes) {
    return ((pairBytes[0] & 255) << 8) | (pairBytes[1] & 255);
  }

  public HashMap<Integer, List<Integer>> getPair_PositionList_Table(byte[] fingerprint) {
    List<int[]> pairPositionList = getPairPositionList(fingerprint);
    HashMap<Integer, List<Integer>> pair_positionList_table = new HashMap();
    for (int[] pair_position : pairPositionList) {
      if (pair_positionList_table.containsKey(Integer.valueOf(pair_position[0]))) {
        ((List) pair_positionList_table.get(Integer.valueOf(pair_position[0]))).add(
            Integer.valueOf(pair_position[1]));
      } else {
        List<Integer> positionList = new LinkedList();
        positionList.add(Integer.valueOf(pair_position[1]));
        pair_positionList_table.put(Integer.valueOf(pair_position[0]), positionList);
      }
    }
    return pair_positionList_table;
  }

  private List<int[]> getPairPositionList(byte[] fingerprint) {
    byte[] pairedFrameTable =
        new byte[((FingerprintManager.getNumFrames(fingerprint) / this.anchorPointsIntervalLength)
            + 1)];
    List<int[]> pairList = new LinkedList();
    List<int[]> sortedCoordinateList = getSortedCoordinateList(fingerprint);
    for (int[] anchorPoint : sortedCoordinateList) {
      int anchorX = anchorPoint[0];
      int anchorY = anchorPoint[1];
      int numPairs = 0;
      Iterator<int[]> targetPointListIterator = sortedCoordinateList.iterator();
      while (targetPointListIterator.hasNext() && numPairs < this.maxPairs) {
        if (this.isReferencePairing
            && pairedFrameTable[anchorX / this.anchorPointsIntervalLength]
            >= this.numAnchorPointsPerInterval) {
          break;
        }
        int[] targetPoint = (int[]) targetPointListIterator.next();
        int targetX = targetPoint[0];
        int targetY = targetPoint[1];
        if (anchorX != targetX || anchorY != targetY) {
          int x2;
          int y2;
          int x1;
          int y1;
          if (targetX >= anchorX) {
            x2 = targetX;
            y2 = targetY;
            x1 = anchorX;
            y1 = anchorY;
          } else {
            x2 = anchorX;
            y2 = anchorY;
            x1 = targetX;
            y1 = targetY;
          }
          if (x2 - x1 <= this.maxTargetZoneDistance
              && y1 / this.bandwidthPerBank == y2 / this.bandwidthPerBank) {
            int pairHashcode = ((((x2 - x1) * this.numFrequencyUnits) * this.numFrequencyUnits)
                + (this.numFrequencyUnits * y2)) + y1;
            if (this.isReferencePairing || !this.stopPairTable.containsKey(
                Integer.valueOf(pairHashcode))) {
              pairList.add(new int[] { pairHashcode, anchorX });
              int i = anchorX / this.anchorPointsIntervalLength;
              pairedFrameTable[i] = (byte) (pairedFrameTable[i] + 1);
              numPairs++;
            } else {
              numPairs++;
            }
          }
        }
      }
    }
    return pairList;
  }

  private List<int[]> getSortedCoordinateList(byte[] fingerprint) {
    int i;
    int numCoordinates = fingerprint.length / 8;
    int[] intensities = new int[numCoordinates];
    for (i = 0; i < numCoordinates; i++) {
      int pointer = (i * 8) + 4;
      intensities[i] =
          ((((fingerprint[pointer] & 255) << 24) | ((fingerprint[pointer + 1] & 255) << 16)) | ((
              fingerprint[pointer + 2]
                  & 255) << 8)) | (fingerprint[pointer + 3] & 255);
    }
    int[] sortIndexes = new QuickSortIndexPreserved(intensities).getSortIndexes();
    List<int[]> sortedCoordinateList = new LinkedList();
    for (i = sortIndexes.length - 1; i >= 0; i--) {
      int pointer = sortIndexes[i] * 8;
      int x = ((fingerprint[pointer] & 255) << 8) | (fingerprint[pointer + 1] & 255);
      int y = ((fingerprint[pointer + 2] & 255) << 8) | (fingerprint[pointer + 3] & 255);
      sortedCoordinateList.add(new int[] { x, y });
    }
    return sortedCoordinateList;
  }
}
