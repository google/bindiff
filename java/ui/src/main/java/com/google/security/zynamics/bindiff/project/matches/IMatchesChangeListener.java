package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.zylib.disassembly.IAddress;

public interface IMatchesChangeListener {
  void addedBasicblockMatch(
      IAddress priFunctionAddr,
      IAddress secFunctionAddr,
      IAddress priBasicblockAddr,
      IAddress secBasicBlockaddr);

  void removedBasicblockMatch(
      IAddress priFunctionAddr,
      IAddress secFunctionAddr,
      IAddress priBasicblockAddr,
      IAddress secBasicBlockaddr);
}
