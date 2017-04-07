package com.google.security.zynamics.bindiff.enums.comparators;

import com.google.security.zynamics.bindiff.enums.EMatchState;

import java.util.Comparator;

public class MatchStateComparator implements Comparator<EMatchState> {
  @Override
  public int compare(final EMatchState o1, final EMatchState o2) {
    final int i = EMatchState.getOrdinal(o1) - EMatchState.getOrdinal(o2);

    if (i < 0) {
      return 1;
    }
    if (i > 0) {
      return -1;
    }
    return 0;
  }
}
