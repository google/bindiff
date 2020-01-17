// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.CProximityNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyProximityNodeRealizer;

import y.base.Node;

import java.util.List;

/**
 * Represents a visible proximity node in ZyGraphs.
 * 
 * @param <T> Type of the raw nodes in the graph.
 */
public class ZyProximityNode<T extends IViewNode<?>> extends ZyGraphNode<CProximityNode<T>> {
  /**
   * Default vertical distance from the attached node.
   */
  private static final int DEFAULT_DISTANCE = 50;

  /**
   * Flag that signals whether the attached node is a parent or a child of the proximity browsing
   * node.
   */
  private final boolean m_isIncoming;

  /**
   * Creates a new proximity node object.
   * 
   * @param node YNode of the proximity node.
   * @param realizer Realizer used by this node.
   * @param attachedNode Real graph node the proximity node is attached to.
   * @param isIncoming Signals the position of the proximity node relative to the attached node.
   */
  public ZyProximityNode(final Node node, final ZyProximityNodeRealizer<T> realizer,
      final ZyGraphNode<T> attachedNode, final boolean isIncoming) {
    super(node, realizer, new CProximityNode<T>(getRawNode(attachedNode)));

    m_isIncoming = isIncoming;

    final IZyNodeRealizer attachedRealizer = attachedNode.getRealizer();

    final double x = attachedRealizer.getCenterX();
    final double y;
    if (isIncoming) {
      y = attachedRealizer.getCenterY() + (attachedRealizer.getHeight() / 2) + DEFAULT_DISTANCE;
    } else {
      y = attachedRealizer.getCenterY() - (attachedRealizer.getHeight() / 2) - DEFAULT_DISTANCE;
    }
    realizer.moveBy(x - (realizer.getWidth() / 2), y - (realizer.getHeight() / 2));
  }

  private static <T extends IViewNode<?>> T getRawNode(final ZyGraphNode<T> attachedNode) {
    @SuppressWarnings("unchecked")
    final T result = (T) attachedNode.getRawNode();
    return result;
  }

  @Override
  public List<? extends ZyGraphNode<CProximityNode<T>>> getChildren() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<? extends ZyGraphNode<CProximityNode<T>>> getParents() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  @SuppressWarnings("unchecked")
  public CProximityNode<T> getRawNode() {
    return (CProximityNode<T>) super.getRawNode();
  }

  /**
   * Signals the relative position of the proximity browsing node to the attached node.
   * 
   * @return True, to signal an incoming node. False, to signal an outgoing node.
   */
  public boolean isIncoming() {
    return m_isIncoming;
  }
}
