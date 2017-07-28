package com.android.mediaserver.musicg.serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectSerializer {
  public void dump(Object object, String dumpFile) {
    try {
      FileOutputStream fout = new FileOutputStream(dumpFile);
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(object);
      oos.close();
      fout.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Object load(String dumpFile) {
    Object obj = null;
    try {
      FileInputStream fin = new FileInputStream(dumpFile);
      ObjectInputStream ois = new ObjectInputStream(fin);
      obj = ois.readObject();
      ois.close();
      fin.close();
      return obj;
    } catch (Exception e) {
      e.printStackTrace();
      return obj;
    }
  }
}
