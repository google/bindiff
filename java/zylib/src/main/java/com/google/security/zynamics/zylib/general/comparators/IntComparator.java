// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class IntComparator implements Comparator<Integer>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -9039204490352575348L;

  @Override
  public int compare(final Integer o1, final Integer o2) {
    return o1 - o2;
  }
}
