// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import com.google.security.zynamics.zylib.disassembly.ICodeContainer;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

public interface ICodeNode<EdgeType extends IViewEdge<? extends IViewNode<?>>, InstructionType extends IInstruction, ListenerTyp extends ICodeNodeListener<?, ?, ?>>
    extends ILineNode<EdgeType>, ICodeContainer<InstructionType> {
  void addListener(ListenerTyp listener);

  @Override
  Iterable<InstructionType> getInstructions();

  int instructionCount();
}
