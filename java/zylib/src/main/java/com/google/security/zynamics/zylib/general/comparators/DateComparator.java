// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class DateComparator implements Comparator<Date>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -846090338272302586L;

  @Override
  public int compare(final Date o1, final Date o2) {
    if (o1.before(o2)) {
      return -1;
    } else if (o1.equals(o2)) {
      return 0;
    } else {
      return 1;
    }
  }
}
