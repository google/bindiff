// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import java.util.List;

public interface ICodeContainer<InstructionType extends IInstruction> {
  IAddress getAddress();

  Iterable<InstructionType> getInstructions();

  InstructionType getLastInstruction();

  List<? extends ICodeEdge<?>> getOutgoingEdges();

  boolean hasInstruction(InstructionType instruction);
}
