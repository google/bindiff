package com.google.security.zynamics.bindiff.enums.comparators;

import com.google.security.zynamics.bindiff.enums.EFunctionType;

import java.util.Comparator;

public class RawFunctionTypeComparator implements Comparator<EFunctionType> {
  @Override
  public int compare(final EFunctionType o1, final EFunctionType o2) {
    return EFunctionType.getOrdinal(o1) - EFunctionType.getOrdinal(o2);
  }
}
