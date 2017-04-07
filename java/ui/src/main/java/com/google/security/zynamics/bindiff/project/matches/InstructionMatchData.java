package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class InstructionMatchData implements IAddressPair {
  private final IAddressPair addrPair;

  public InstructionMatchData(final long priAddr, final long secAddr) {
    addrPair = new AddressPair(priAddr, secAddr);
  }

  @Override
  public long getAddress(final ESide side) {
    return addrPair.getAddress(side);
  }

  @Override
  public IAddress getIAddress(final ESide side) {
    return new CAddress(getAddress(side));
  }
}
