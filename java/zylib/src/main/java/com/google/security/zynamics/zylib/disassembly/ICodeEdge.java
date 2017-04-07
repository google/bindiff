// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

public interface ICodeEdge<NodeType> {
  NodeType getSource();

  NodeType getTarget();

  EdgeType getType();
}
