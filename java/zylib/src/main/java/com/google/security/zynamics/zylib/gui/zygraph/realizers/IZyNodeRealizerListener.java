// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Listener interface for classes that want to be notified about changes in realizers.
 * 
 * @param <NodeType>
 */
public interface IZyNodeRealizerListener<NodeType extends ZyGraphNode<?>> {
  void changedLocation(IZyNodeRealizer realizer, double x, double y);

  void changedSelection(IZyNodeRealizer realizer);

  void changedSize(IZyNodeRealizer realizer, double x, double y);

  void changedVisibility(IZyNodeRealizer realizer);

  /**
   * Invoked when the content of the realizer changed.
   * 
   * @param realizer The realizer whose content changed.
   */
  void regenerated(IZyNodeRealizer realizer);
}
