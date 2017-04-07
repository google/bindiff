// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

/**
 * Listener interface for classes that want to be notified about changes in realizers.
 * 
 * @param <EdgeType>
 */
public interface IZyEdgeRealizerListener<EdgeType> {
  void addedBend(double x, double y);

  void bendChanged(int index, double x, double y);

  void changedLocation(ZyEdgeRealizer<EdgeType> realizer);

  void changedVisibility(ZyEdgeRealizer<EdgeType> realizer);

  void clearedBends();

  void insertedBend(int index, double x, double y);

  /**
   * Invoked when the content of the realizer changed.
   * 
   * @param realizer The realizer whose content changed.
   */
  void regenerated(ZyEdgeRealizer<EdgeType> realizer);

  void removedBend(ZyEdgeRealizer<EdgeType> realizer, int position);
}
