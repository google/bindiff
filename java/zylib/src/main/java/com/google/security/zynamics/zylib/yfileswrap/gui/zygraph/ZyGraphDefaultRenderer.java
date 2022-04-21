// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyGroupNodeRealizer;

import y.base.Node;
import y.base.NodeCursor;
import y.view.DefaultGraph2DRenderer;
import y.view.Graph2D;
import y.view.NodeRealizer;

import java.util.LinkedHashSet;

public class ZyGraphDefaultRenderer extends DefaultGraph2DRenderer {
  /**
   * This set contains the nodes in drawing order. The nodes to be drawn first are in the set early.
   * The nodes drawn over the earlier nodes are in the set late.
   */
  private final LinkedHashSet<Node> m_nodesInDrawingOrder = new LinkedHashSet<Node>();

  public ZyGraphDefaultRenderer() {
    setLayeredPainting(true);
  }

  @Override
  protected int getLayer(final Graph2D graph, final Node n) {
    // The drawing order of nodes in the graph depends on the
    // order of the nodes in the drawing order set. Nodes
    // at the beginning of the set are drawn before nodes at
    // the end of the set.

    if (graph.getRealizer(n) instanceof ZyGroupNodeRealizer<?>) {
      // Group nodes are always drawn first so they don't hide
      // their members.

      return super.getLayer(graph, n);
    }

    int counter = 1;

    for (final Node node : m_nodesInDrawingOrder) {
      if (node == n) {
        return counter;
      }

      ++counter;
    }

    return super.getLayer(graph, n);
  }

  public void bringNodeToFront(final Node node) {
    final Graph2D g = (Graph2D) node.getGraph();
    final NodeRealizer r = g.getRealizer(node);

    if (r.isSelected()) {
      // This does not work properly! Why? NH
      for (final NodeCursor nc = g.selectedNodes(); nc.ok(); nc.next()) {
        m_nodesInDrawingOrder.remove(nc.node());
        m_nodesInDrawingOrder.add(nc.node());
      }
    } else {
      // This seems to work correctly.
      m_nodesInDrawingOrder.remove(node);
      m_nodesInDrawingOrder.add(node);
    }
  }
}
