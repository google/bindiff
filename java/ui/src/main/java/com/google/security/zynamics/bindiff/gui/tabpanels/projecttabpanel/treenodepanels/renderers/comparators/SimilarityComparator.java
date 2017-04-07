package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.security.zynamics.zylib.general.Pair;
import java.util.Comparator;

// TODO(cblichmann): Generalize into PairDoubleComparator
public class SimilarityComparator implements Comparator<Pair<Double, Double>> {
  @Override
  public int compare(final Pair<Double, Double> o1, final Pair<Double, Double> o2) {
    if (o1.first() > o2.first()) {
      return -1;
    } else if (o1.first() < o2.first()) {
      return 1;
    }

    return 0;
  }
}
