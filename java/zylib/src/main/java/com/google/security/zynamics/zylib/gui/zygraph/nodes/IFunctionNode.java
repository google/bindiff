// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

import java.util.List;


public interface IFunctionNode<EdgeType extends IViewEdge<? extends IViewNode<?>>, ListenerTyp extends IFunctionNodeListener<?, ?>>
    extends ILineNode<EdgeType> {
  IFunction getFunction();

  List<?> getLocalComment();
}
