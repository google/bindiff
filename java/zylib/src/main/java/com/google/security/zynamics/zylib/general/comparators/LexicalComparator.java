package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class LexicalComparator implements Comparator<String>, Serializable {
  @Override
  public int compare(final String o1, final String o2) {
    return o1.compareToIgnoreCase(o2);
  }
}
