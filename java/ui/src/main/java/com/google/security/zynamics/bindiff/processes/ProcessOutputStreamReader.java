package com.google.security.zynamics.bindiff.processes;

import com.google.security.zynamics.bindiff.log.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessOutputStreamReader implements Runnable {
  // See:
  // http://www.javakb.com/Uwe/Forum.aspx/java-programmer/7243/Process-waitFor-vs-Process-destroy
  private final String name;
  private final InputStream inStream;
  private Thread thread;

  public ProcessOutputStreamReader(final String name, final InputStream inStream) {
    this.name = name;
    this.inStream = inStream;
  }

  public void interruptThread() {
    if (thread != null) {
      thread.interrupt();
    }
  }

  @Override
  public void run() {
    try {
      final InputStreamReader inStreamReader = new InputStreamReader(inStream);
      final BufferedReader bufferedReader = new BufferedReader(inStreamReader);

      while (!thread.isInterrupted()) {
        final String line = bufferedReader.readLine();

        if (line == null) {
          break;
        }

        Logger.logInfo("[" + name + "] " + line.replace("%", "%%"));
      }
    } catch (final Exception e) {
      Logger.logException(e, "Could't read process output stream.");
    } finally {
      if (inStream != null) {
        try {
          inStream.close();
        } catch (final IOException e) {
          Logger.logException(e, "Could't close process output stream.");
        }
      }
    }
  }

  public void start() {
    thread = new Thread(this);
    thread.start();
  }
}
