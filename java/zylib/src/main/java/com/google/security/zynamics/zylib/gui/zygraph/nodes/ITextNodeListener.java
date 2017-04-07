// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

public interface ITextNodeListener<NodeType> {
  void changedText(NodeType node, String text);
}
