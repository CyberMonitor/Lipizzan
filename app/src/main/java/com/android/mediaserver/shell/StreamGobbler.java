package com.android.mediaserver.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StreamGobbler extends Thread {
  private OnLineListener listener = null;
  private BufferedReader reader = null;
  private String shell = null;
  private List<String> writer = null;

  public StreamGobbler(String shell, InputStream inputStream, List<String> outputList) {
    shell = shell;
    reader = new BufferedReader(new InputStreamReader(inputStream));
    writer = outputList;
  }

  public StreamGobbler(String shell, InputStream inputStream, OnLineListener onLineListener) {
    shell = shell;
    reader = new BufferedReader(new InputStreamReader(inputStream));
    listener = onLineListener;
  }

  public void run() {
    while (true) {
      try {
        String line = reader.readLine();
        if (line != null) {
          Debug.logOutput(String.format("[%s] %s", new Object[] { shell, line }));
          if (writer != null) {
            writer.add(line);
          }
          if (listener != null) {
            listener.onLine(line);
          }
        }
      } catch (IOException e) {
      }
    }
  }

  public interface OnLineListener {
    void onLine(String str);
  }
}
