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

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


public class CDefaultNodePressedLeftAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodePressedLeftState<NodeType, EdgeType>> {
  @Override
  public void execute(final CNodePressedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final NodeType draggedNode = graph.getNode(state.getNode());

    if (draggedNode != null) {
      final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

      if (graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
        CEditNodeHelper.setCaretStart(graph, state.getNode(), event);
        CEditNodeHelper.setCaretEnd(graph, state.getNode(), event);
      } else {
        CEditNodeHelper.removeCaret(graph);
      }
    } else {
      CEditNodeHelper.removeCaret(graph);
    }
  }
}
