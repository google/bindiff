package com.google.security.zynamics.zylib.general.comparators;

import com.google.security.zynamics.zylib.disassembly.CAddress;
import java.util.Comparator;

/** Compares two hexadecimal strings, typically memory addresses. */
public class HexStringComparator implements Comparator<String> {

  @Override
  public int compare(final String o1, final String o2) {
    return new CAddress(o1, 16).compareTo(new CAddress(o2, 16));
  }
}