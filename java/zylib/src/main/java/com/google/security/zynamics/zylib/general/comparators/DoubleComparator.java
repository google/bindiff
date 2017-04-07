package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class DoubleComparator implements Comparator<Double>, Serializable {
  @Override
  public int compare(final Double d1, final Double d2) {
    return Double.compare(d1, d2);
  }
}
