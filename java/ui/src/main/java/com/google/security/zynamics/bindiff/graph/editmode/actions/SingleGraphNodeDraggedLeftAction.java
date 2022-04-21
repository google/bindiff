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

package com.google.security.zynamics.bindiff.graph.editmode.actions;

import com.google.security.zynamics.bindiff.graph.helpers.GraphElementMover;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphLayeredRenderer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeDraggedLeftAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.base.Node;

public class SingleGraphNodeDraggedLeftAction<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> {
  protected void moveToFront(final ZyGraphLayeredRenderer<?> renderer, final Node node) {
    renderer.bringNodeToFront(node);
  }

  @Override
  public void execute(
      final CNodeDraggedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    new CDefaultNodeDraggedLeftAction<NodeType, EdgeType>().execute(state, event);

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();
    final NodeType draggedNode = graph.getNode(state.getNode());
    final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

    if (!graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
      final Node node = state.getNode();
      moveToFront((ZyGraphLayeredRenderer<?>) graph.getView().getGraph2DRenderer(), node);

      GraphElementMover.moveNodes(graph, node, state.getDistanceX(), state.getDistanceY());
    }
  }
}
