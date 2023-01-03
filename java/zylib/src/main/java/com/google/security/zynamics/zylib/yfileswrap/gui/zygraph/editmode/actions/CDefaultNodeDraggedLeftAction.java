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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphLayeredRenderer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CNodeMover;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import y.base.Node;
import y.view.Bend;

public class CDefaultNodeDraggedLeftAction<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> {
  protected void moveToFront(final ZyGraphLayeredRenderer<?> renderer, final Node node) {
    renderer.bringNodeToFront(node);
  }

  @Override
  public void execute(
      final CNodeDraggedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    CMouseCursorHelper.setHandCursor(state.getGraph());

    moveToFront(
        (ZyGraphLayeredRenderer<?>) state.getGraph().getView().getGraph2DRenderer(),
        state.getNode());

    // 1. The dragged node is always moved
    // 2. The selected nodes are only moved if the dragged node is selected too

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();
    final Set<Bend> movedBends = new HashSet<Bend>();
    final NodeType draggedNode = graph.getNode(state.getNode());
    final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

    if (graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
      CEditNodeHelper.setCaretEnd(graph, state.getNode(), event);
      CEditNodeHelper.select(graph, state.getNode(), event);
    } else if (draggedNode.isSelected()) {
      for (final NodeType n : graph.getSelectedNodes()) {
        CNodeMover.moveNode(graph, n, state.getDistanceX(), state.getDistanceY(), movedBends);
      }
    } else {
      CNodeMover.moveNode(
          graph, draggedNode, state.getDistanceX(), state.getDistanceY(), movedBends);
    }

    graph.updateViews();
  }
}
