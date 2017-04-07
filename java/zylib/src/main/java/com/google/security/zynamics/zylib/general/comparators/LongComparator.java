// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class LongComparator implements Comparator<Long>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2814764330420080628L;

  @Override
  public int compare(final Long o1, final Long o2) {
    final long value = o1 - o2;

    if (value > 0) {
      return 1;
    } else if (value < 0) {
      return -1;
    }
    return 0;
  }
}
