// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.io;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class FilesizeSorter implements Comparator<File>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7060651903531011219L;

  @Override
  public int compare(final File lhs, final File rhs) {
    return Long.compare(lhs.length(), rhs.length());
  }
}
