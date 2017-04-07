// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;


/**
 * Nodes that implement this interface unlock {@link GraphHelpers} functions that require nodes that
 * can be displayed on the screen.
 */
public interface IViewableNode {
  /**
   * Enlarges the given rectangle such that it will contain the bounding box of the node.
   * 
   * @param rectangle The rectangle.
   */
  void calcUnionRect(Rectangle2D rectangle);

  /**
   * Returns the bounding box of the node.
   * 
   * @return The bounding box of the node.
   */
  Double getBoundingBox();
}
