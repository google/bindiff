// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.functions;

import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.ArrayList;

public class SelectionFunctions {
  /**
   * Inverts the selected nodes of a graph.
   * 
   * @param <NodeType> The type of the nodes in the graph.
   * 
   * @param graph The graph in question.
   */
  public static <NodeType extends ZyGraphNode<?>> void invertSelection(
      final AbstractZyGraph<NodeType, ?> graph) {
    final ArrayList<NodeType> toSelect = new ArrayList<NodeType>();
    final ArrayList<NodeType> toUnselect = new ArrayList<NodeType>();

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (node.isSelected()) {
          toUnselect.add(node);
        } else {
          toSelect.add(node);
        }

        return IterationMode.CONTINUE;
      }
    });

    graph.selectNodes(toSelect, toUnselect);
  }

}
