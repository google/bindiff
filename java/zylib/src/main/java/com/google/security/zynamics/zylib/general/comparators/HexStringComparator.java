// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.comparators;

import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.io.Serializable;
import java.util.Comparator;


public class HexStringComparator implements Comparator<String>, Serializable {
  /**
   * Used for serialization
   */
  private static final long serialVersionUID = 8204731939693038511L;

  @Override
  public int compare(final String o1, final String o2) {
    if (o1.isEmpty() || o2.isEmpty()) {
      return o1.equals(o2) ? 0 : o2.isEmpty() ? 1 : -1;
    }

    return CAddress.compare(Long.valueOf(o1, 16), Long.valueOf(o2, 16));
  }
}
