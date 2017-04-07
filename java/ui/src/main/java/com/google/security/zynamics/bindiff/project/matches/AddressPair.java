package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class AddressPair implements IAddressPair {
  private final long priAddr;
  private final long secAddr;

  public AddressPair(final long priAddr, final long secAddr) {
    this.priAddr = priAddr;
    this.secAddr = secAddr;
  }

  @Override
  public boolean equals(final Object obj) {
    return priAddr == ((AddressPair) obj).getAddress(ESide.PRIMARY)
        && secAddr == ((AddressPair) obj).getAddress(ESide.SECONDARY);
  }

  @Override
  public long getAddress(final ESide side) {
    return side == ESide.PRIMARY ? priAddr : secAddr;
  }

  @Override
  public IAddress getIAddress(final ESide side) {
    return new CAddress(getAddress(side));
  }

  @Override
  public int hashCode() {
    return (int) ((priAddr ^ priAddr >>> 32) * (secAddr ^ secAddr >>> 32));
  }
}
