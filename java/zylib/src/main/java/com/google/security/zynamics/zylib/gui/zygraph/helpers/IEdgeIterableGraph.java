// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

public interface IEdgeIterableGraph<EdgeType> {
  void iterateEdges(final IEdgeCallback<EdgeType> callback);
}
