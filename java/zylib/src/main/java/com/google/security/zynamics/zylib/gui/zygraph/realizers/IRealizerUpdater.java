// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Interface that must be implemented by all classes that want to update the content of realizers.
 * 
 * @param <NodeType>
 */
public interface IRealizerUpdater<NodeType extends ZyGraphNode<?>> {
  /**
   * Frees all allocated resources.
   */
  void dispose();

  /**
   * Regenerates the content of the realizer.
   * 
   * @param realizer The realizers whose content is updated.
   */
  void generateContent(IZyNodeRealizer realizer, ZyLabelContent content);

  /**
   * Called by the realizer to set the realizer updater.
   * 
   * @param realizer The realizer to be updated.
   */
  void setRealizer(IZyNodeRealizer realizer);
}
