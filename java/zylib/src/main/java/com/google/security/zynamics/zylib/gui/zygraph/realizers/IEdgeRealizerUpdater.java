// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

/**
 * Interface that must be implemented by all classes that want to update the content of edge
 * realizers.
 */
public interface IEdgeRealizerUpdater<EdgeType> {
  /**
   * Regenerates the content of the realizer.
   * 
   * @param realizer The realizers whose content is updated.
   * 
   * @return The new content of the realizer.
   */
  ZyLabelContent generateContent(ZyEdgeRealizer<EdgeType> realizer);

  /**
   * Called by the realizer to set the realizer updater.
   * 
   * @param realizer The realizer to be updated.
   */
  void setRealizer(ZyEdgeRealizer<EdgeType> realizer);
}
