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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.TooltipGenerator;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyProximityNodeRealizer;

import y.base.Edge;
import y.base.Node;

/**
 * Helper class to update the tool tip shown in a graph.
 */
public final class CTooltipUpdater {
  /**
   * Determines whether a given node is a proximity node.
   * 
   * @param graph The graph the node belongs to.
   * @param node The node to check.
   * 
   * @return True, if the node is a proximity node. False, otherwise.
   */
  public static boolean isProximityNode(final AbstractZyGraph<?, ?> graph, final Node node) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");
    Preconditions.checkNotNull(node, "Node argument can not be null");

    return graph.getGraph().getRealizer(node) instanceof ZyProximityNodeRealizer<?>;
  }

  public static String updateEdgeTooltip(final AbstractZyGraph<?, ?> graph, final Edge edge) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");
    Preconditions.checkNotNull(edge, "Edge argument can not be null");

    if (graph.getView() instanceof ZyGraph2DView) {
      if (((ZyGraph2DView) graph.getView()).isEdgeSloppyPaintMode()) {
        final String tooltip = TooltipGenerator.createTooltip(graph, edge);
        graph.getView().setToolTipText(tooltip);
        return tooltip;
      }
    }
    return null;
  }

  /**
   * Sets the tool tip of a node.
   * 
   * @param node The node whose tool tip is set.
   */
  public static String updateNodeTooltip(final AbstractZyGraph<?, ?> graph, final Node node) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");
    Preconditions.checkNotNull(node, "Node argument can not be null");

    if (isProximityNode(graph, node)) {
      return TooltipGenerator.createTooltip(graph, node);
    }
    if (graph.getView() instanceof ZyGraph2DView) {
      if (((ZyGraph2DView) graph.getView()).isNodeSloppyPaintMode()) {
        return TooltipGenerator.createTooltip(graph, node);
      }
    }

    return null;
  }
}
