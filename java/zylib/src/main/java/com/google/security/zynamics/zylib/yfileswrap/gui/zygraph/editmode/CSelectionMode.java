// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.SelectionBoxMode;

/**
 * @param <NodeType> Necessary to get additional box selection using SHIFT+DRAGMOUSE
 */
public class CSelectionMode<NodeType extends ZyGraphNode<?>> extends SelectionBoxMode {
  /** The owning graph of the edit mode. */
  private final AbstractZyGraph<NodeType, ?> m_graph;

  public CSelectionMode(final AbstractZyGraph<NodeType, ?> graph) {
    m_graph = Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
  }

  @Override
  protected void selectionBoxAction(final Rectangle rect, final boolean shiftMode) {
    m_graph.getGraph().firePreEvent();

    final NodeList selectedNodes = new NodeList();

    for (final NodeCursor node = m_graph.getGraph().nodes(); node.ok(); node.next()) {
      final NodeType zyNode = m_graph.getNode(node.node());

      if ((zyNode == null) || (zyNode instanceof ZyProximityNode<?>)) {
        continue;
      }

      if (belongsToSelection(node.node(), rect)) {
        selectedNodes.add(node.node());
      }
    }

    if (((getLastDragEvent().getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0)
        && ((getLastDragEvent().getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0)) {
      m_graph.getGraph().unselectAll();
    }

    for (final Object nodeObject : selectedNodes) {
      final Node node = (Node) nodeObject;

      m_graph.getGraph().setSelected(node, true);
    }

    for (final EdgeCursor ec = m_graph.getGraph().selectedEdges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      final Node src = e.source();
      final Node dst = e.target();

      if (!m_graph.getGraph().getRealizer(src).isSelected()
          && !m_graph.getGraph().getRealizer(dst).isSelected()) {
        m_graph.getGraph().getRealizer(e).setSelected(false);
      }
    }

    m_graph.getGraph().firePostEvent();

    m_graph.getGraph().updateViews();
  }
}
