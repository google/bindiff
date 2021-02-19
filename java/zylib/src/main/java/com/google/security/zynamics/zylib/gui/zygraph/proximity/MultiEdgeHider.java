// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph.proximity;

import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.HashSet;

/**
 * Class to form a single edge out of a number of edges.
 */
public class MultiEdgeHider {
  public static <
      NodeType extends ZyGraphNode<? extends IViewNode<?>>> void hideMultipleEdgesInternal(
      final AbstractZyGraph<NodeType, ?> graph) {
    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        hideMultipleEdgesInternal(node);

        return IterationMode.CONTINUE;
      }
    });
  }

  public static <
      NodeType extends ZyGraphNode<? extends IViewNode<?>>> void hideMultipleEdgesInternal(
      final NodeType node) {
    if (!node.isVisible() || (node.getRawNode() instanceof IGroupNode<?, ?>)) {
      // If the node is not visible, then none of the edges are visible.
      return;
    }

    final HashSet<Object> targets = new HashSet<Object>();

    for (final IViewEdge<?> edge : ((IViewNode<?>) node.getRawNode()).getOutgoingEdges()) {
      final Object target = edge.getTarget();

      if (targets.contains(target)) {
        edge.setVisible(false);
      } else {
        targets.add(target);
      }
    }

    final HashSet<Object> sources = new HashSet<Object>();

    for (final IViewEdge<?> edge : ((IViewNode<?>) node.getRawNode()).getIncomingEdges()) {
      if (sources.contains(edge.getSource())) {
        edge.setVisible(false);
      } else {
        sources.add(edge.getSource());
      }
    }
  }

  public static <NodeType extends ZyGraphNode<? extends IViewNode<?>>,
      EdgeType extends ZyGraphEdge<NodeType, EdgeType,
      ? extends IViewEdge<?>>> void unhideMultipleEdgesInternal(
      final AbstractZyGraph<NodeType, EdgeType> graph) {
    graph.iterateEdges(new IEdgeCallback<EdgeType>() {
      @Override
      public IterationMode nextEdge(final EdgeType edge) {
        edge.getRawEdge().setVisible(edge.getSource().isVisible() && edge.getTarget().isVisible());

        return IterationMode.CONTINUE;
      }
    });
  }
}
