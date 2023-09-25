// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.processes;

import com.google.common.flogger.FluentLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessOutputStreamReader implements Runnable {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

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

        logger.atInfo().log("[%s] %s", name, line);
      }
    } catch (final Exception e) {
      logger.atSevere().withCause(e).log("Couldn't read process output stream");
    } finally {
      if (inStream != null) {
        try {
          inStream.close();
        } catch (final IOException e) {
          logger.atSevere().withCause(e).log("Couldn't close process output stream");
        }
      }
    }
  }

  public void start() {
    thread = new Thread(this);
    thread.start();
  }
}
