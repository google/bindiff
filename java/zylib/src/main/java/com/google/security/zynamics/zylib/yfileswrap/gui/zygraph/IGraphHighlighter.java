// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import y.view.EdgeRealizer;
import y.view.NodeRealizer;

public interface IGraphHighlighter {
  void highlightEdge(final EdgeRealizer r, final boolean state);

  void highlightNode(final NodeRealizer r, final boolean state);
}
