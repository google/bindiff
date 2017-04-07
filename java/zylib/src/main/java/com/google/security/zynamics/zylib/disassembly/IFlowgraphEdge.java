// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import com.google.security.zynamics.zylib.types.graphs.IGraphEdge;

public interface IFlowgraphEdge extends IGraphEdge<IBasicBlock> {
  int getDestination();

  IAddress getParentFunction();

  @Override
  IBasicBlock getSource();
}
