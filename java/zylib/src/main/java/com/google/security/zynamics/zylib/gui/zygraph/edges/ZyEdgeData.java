// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

/**
 * Collects all information that is passed to the user data field of a yfiles edge.
 */
public class ZyEdgeData<EdgeTypeT> {
  private final EdgeTypeT m_zyEdge;

  public ZyEdgeData(final EdgeTypeT zyEdge) {
    m_zyEdge = zyEdge;
  }

  public EdgeTypeT getEdge() {
    return m_zyEdge;
  }
}
