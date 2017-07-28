package com.android.mediaserver.musicg.fingerprint;

import com.android.mediaserver.musicg.math.rank.MapRankInteger;
import java.util.HashMap;
import java.util.List;

public class FingerprintSimilarityComputer {
  byte[] fingerprint1;
  byte[] fingerprint2;
  private FingerprintSimilarity fingerprintSimilarity = new FingerprintSimilarity();

  public FingerprintSimilarityComputer(byte[] fingerprint1, byte[] fingerprint2) {
    this.fingerprint1 = fingerprint1;
    this.fingerprint2 = fingerprint2;
  }

  public FingerprintSimilarity getFingerprintsSimilarity() {
    int numFrames;
    HashMap<Integer, Integer> offset_Score_Table = new HashMap();
    float score = 0.0f;
    int mostSimilarFramePosition = Integer.MIN_VALUE;
    if (this.fingerprint1.length > this.fingerprint2.length) {
      numFrames = FingerprintManager.getNumFrames(this.fingerprint2);
    } else {
      numFrames = FingerprintManager.getNumFrames(this.fingerprint1);
    }
    PairManager pairManager = new PairManager();
    HashMap<Integer, List<Integer>> this_Pair_PositionList_Table =
        pairManager.getPair_PositionList_Table(this.fingerprint1);
    HashMap<Integer, List<Integer>> compareWave_Pair_PositionList_Table =
        pairManager.getPair_PositionList_Table(this.fingerprint2);
    for (Integer intValue : compareWave_Pair_PositionList_Table.keySet()) {
      int compareWaveHashNumber = intValue.intValue();
      if (this_Pair_PositionList_Table.containsKey(Integer.valueOf(compareWaveHashNumber))
          && compareWave_Pair_PositionList_Table.containsKey(
          Integer.valueOf(compareWaveHashNumber))) {
        List<Integer> compareWavePositionList =
            (List) compareWave_Pair_PositionList_Table.get(Integer.valueOf(compareWaveHashNumber));
        for (Object intValue2 : (List) this_Pair_PositionList_Table.get(
            Integer.valueOf(compareWaveHashNumber))) {
          int thisPosition = (int) intValue2;
          for (Integer intValue22 : compareWavePositionList) {
            int offset = thisPosition - intValue22.intValue();
            if (offset_Score_Table.containsKey(Integer.valueOf(offset))) {
              offset_Score_Table.put(Integer.valueOf(offset), Integer.valueOf(
                  ((Integer) offset_Score_Table.get(Integer.valueOf(offset))).intValue() + 1));
            } else {
              offset_Score_Table.put(Integer.valueOf(offset), Integer.valueOf(1));
            }
          }
        }
      }
    }
    List<Integer> orderedKeyList =
        new MapRankInteger(offset_Score_Table, false).getOrderedKeyList(100, true);
    if (orderedKeyList.size() > 0) {
      int key = ((Integer) orderedKeyList.get(0)).intValue();
      if (Integer.MIN_VALUE == Integer.MIN_VALUE) {
        mostSimilarFramePosition = key;
        score = (float) ((Integer) offset_Score_Table.get(Integer.valueOf(key))).intValue();
        if (offset_Score_Table.containsKey(Integer.valueOf(key - 1))) {
          score +=
              (float) (((Integer) offset_Score_Table.get(Integer.valueOf(key - 1))).intValue() / 2);
        }
        if (offset_Score_Table.containsKey(Integer.valueOf(key + 1))) {
          score +=
              (float) (((Integer) offset_Score_Table.get(Integer.valueOf(key + 1))).intValue() / 2);
        }
      }
    }
    score /= (float) numFrames;
    float similarity = score;
    if (similarity > 1.0f) {
      similarity = 1.0f;
    }
    this.fingerprintSimilarity.setMostSimilarFramePosition(mostSimilarFramePosition);
    this.fingerprintSimilarity.setScore(score);
    this.fingerprintSimilarity.setSimilarity(similarity);
    return this.fingerprintSimilarity;
  }
}
