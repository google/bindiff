package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import java.util.Comparator;

// TODO(cblichmann): See com.google.security.zynamics.bindiff.types.AddressPairComparator
public class AddressPairComparator implements Comparator<IAddressPair> {
  final ESide sortBySide;

  public AddressPairComparator(final ESide sortBySide) {
    this.sortBySide = sortBySide;
  }

  @Override
  public int compare(final IAddressPair addrPair1, final IAddressPair addrPair2) {
    return CAddress.compare(addrPair1.getAddress(sortBySide), addrPair2.getAddress(sortBySide));
  }
}
