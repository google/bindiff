package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.zylib.disassembly.IAddress;

public interface IMatchesChangeListener {
  void addedBasicBlockMatch(
      IAddress priFunctionAddr,
      IAddress secFunctionAddr,
      IAddress priBasicblockAddr,
      IAddress secBasicBlockaddr);

  void removedBasicBlockMatch(
      IAddress priFunctionAddr,
      IAddress secFunctionAddr,
      IAddress priBasicblockAddr,
      IAddress secBasicBlockaddr);
}
