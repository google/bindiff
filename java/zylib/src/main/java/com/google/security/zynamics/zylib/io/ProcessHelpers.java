// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.io;

import java.io.IOException;

public class ProcessHelpers {
  public static void runBatchFile(final String filename) throws IOException, InterruptedException {
    final Process p = Runtime.getRuntime().exec(new String[] {"cmd", "/c", filename});

    p.waitFor();
  }
}
