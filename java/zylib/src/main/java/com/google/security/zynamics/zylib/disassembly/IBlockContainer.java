// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import java.util.List;

public interface IBlockContainer<InstructionType extends IInstruction> {
  List<? extends ICodeEdge<?>> getBasicBlockEdges();

  List<? extends ICodeContainer<InstructionType>> getBasicBlocks();

  String getName();
}
