package com.google.security.zynamics.bindiff.types;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.Comparator;

// TODO(cblichmann): See com.google.security.zynamics.bindiff.project.matches.AddressPairComparator
public class AddressPairComparator implements Comparator<Pair<IAddress, IAddress>> {
  final ESide sortBySide;

  public AddressPairComparator(final ESide sortBySide) {
    this.sortBySide = sortBySide;
  }

  @Override
  public int compare(final Pair<IAddress, IAddress> addr1, final Pair<IAddress, IAddress> addr2) {
    final IAddress o1 = sortBySide == ESide.PRIMARY ? addr1.first() : addr1.second();
    final IAddress o2 = sortBySide == ESide.PRIMARY ? addr2.first() : addr2.second();

    return o1.compareTo(o2);
  }
}
