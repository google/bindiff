package com.google.security.zynamics.bindiff.enums.comparators;

import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;

import java.util.Comparator;

public class RawFunctionComparator implements Comparator<RawFunction> {
  @Override
  public int compare(final RawFunction o1, final RawFunction o2) {
    return o1.getAddress().compareTo(o2.getAddress());
  }
}
