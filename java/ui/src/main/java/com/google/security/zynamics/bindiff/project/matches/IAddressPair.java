package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.IAddress;

// TODO(cblichmann): Check if we can just use Pair<IAddress, IAddress> throughout.
public interface IAddressPair {
  long getAddress(ESide side);

  IAddress getIAddress(ESide side);
}
