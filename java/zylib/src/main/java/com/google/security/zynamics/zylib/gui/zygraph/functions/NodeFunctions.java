// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.functions;

import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.ArrayList;
import java.util.List;

public class NodeFunctions {
  public static <NodeType extends ZyGraphNode<?>> List<NodeType> getInvisibleNodes(
      final AbstractZyGraph<NodeType, ?> graph) {
    final ArrayList<NodeType> nodes = new ArrayList<NodeType>();

    IteratorFunctions.iterateInvisible(graph, new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        nodes.add(node);
        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  public static <NodeType extends ZyGraphNode<?>> List<NodeType> getVisibleNodes(
      final AbstractZyGraph<NodeType, ?> graph) {
    final ArrayList<NodeType> nodes = new ArrayList<NodeType>();

    IteratorFunctions.iterateVisible(graph, new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        nodes.add(node);
        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }
}
