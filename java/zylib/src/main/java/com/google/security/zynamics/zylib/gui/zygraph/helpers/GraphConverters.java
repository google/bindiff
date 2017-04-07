// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class GraphConverters {
  public static <RawType extends IViewNode<?>, NodeType extends ZyGraphNode<RawType>> Collection<NodeType> convert(
      final AbstractZyGraph<NodeType, ?> graph, final Collection<? extends RawType> nodes) {
    final List<NodeType> list = new ArrayList<NodeType>();

    for (final RawType node : nodes) {
      list.add(graph.getNode(node));
    }

    return list;
  }

  public static <NodeType extends IViewNode<?>> List<NodeType> convert(
      final Collection<? extends ZyGraphNode<? extends NodeType>> visibleNodes) {
    final List<NodeType> list = new ArrayList<NodeType>();

    for (final ZyGraphNode<? extends NodeType> node : visibleNodes) {
      @SuppressWarnings("unchecked")
      final NodeType rawNode = (NodeType) node.getRawNode();
      list.add(rawNode);
    }

    return list;
  }
}
