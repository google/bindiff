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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CGraphSelector;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ProximityHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.base.Node;
import y.view.NodeLabel;
import y.view.hierarchy.GroupNodeRealizer;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.SwingUtilities;

public class CDefaultNodeClickedLeftAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeClickedLeftState<NodeType, EdgeType>> {
  /**
   * Toggles the expansion state of a given group node.
   * 
   * @param node The node to expand or collapse.
   */
  private void toggleGroup(final NodeType node) {
    final IGroupNode<?, ?> gnode = (IGroupNode<?, ?>) node.getRawNode();

    gnode.setCollapsed(!gnode.isCollapsed());
  }

  @Override
  public void execute(final CNodeClickedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();
    final Node n = state.getNode();

    final NodeType node = graph.getNode(n);

    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final CStateFactory<NodeType, EdgeType> factory = state.getStateFactory();

    if (ProximityHelper.isProximityNode(state.getGraph().getGraph(), n)) {
      CEditNodeHelper.removeCaret(graph);

      final ZyProximityNode<?> proximityNode =
          ProximityHelper.getProximityNode(graph.getGraph(), n);

      for (final IZyEditModeListener<NodeType, EdgeType> listener : factory.getListeners()) {
        try {
          listener.proximityBrowserNodeClicked(proximityNode, event, x, y);
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging here.
        }
      }
    } else if ((node != null) && (node.getRawNode() instanceof IGroupNode)) {
      CEditNodeHelper.removeCaret(graph);

      final GroupNodeRealizer gnr =
          (GroupNodeRealizer) graph.getGraph().getRealizer(node.getNode());

      final NodeLabel handle = gnr.getStateLabel();

      if (handle.getBox().contains(x, y)) {
        // Clicks on the X in the group node corner
        toggleGroup(node);
      } else if (SwingUtilities.isLeftMouseButton(event)) {
        if ((event.getClickCount() == 2) && event.isControlDown()) {
          // CTRL-LEFT-DOUBLECLICK
          toggleGroup(node);
        } else if (event.getClickCount() == 1) {
          CGraphSelector.selectNode(graph, node, event.isShiftDown());
        }
      }
    } else {
      if (node != null) {
        if (graph.getEditMode().getLabelEventHandler()
            .isActiveLabel(node.getRealizer().getNodeContent())) {
          CEditNodeHelper.setCaretEnd(graph, state.getNode(), event);
        } else {
          if (graph.getEditMode().getLabelEventHandler().isActive()) {
            CEditNodeHelper.removeCaret(graph);
          }

          final Set<NodeType> selectedNodes = graph.getSelectedNodes();

          if (event.isShiftDown() && (selectedNodes.size() >= 1)) {
            CGraphSelector.selectPath(graph, new ArrayList<NodeType>(selectedNodes), node);
          } else if (event.isControlDown()) {
            graph.selectNode(node, !node.isSelected());
          } else {
            final Collection<NodeType> toUnselect = new ArrayList<NodeType>(graph.getNodes());
            toUnselect.remove(node);

            final Collection<NodeType> toSelect = new ArrayList<NodeType>();
            toSelect.add(node);

            graph.selectNodes(toSelect, toUnselect);
          }
        }

        for (final IZyEditModeListener<NodeType, EdgeType> listener : factory.getListeners()) {
          // ESCA-JAVA0166: Catch Exception because we are calling a listener function
          try {
            listener.nodeClicked(node, event, x, y);
          } catch (final Exception exception) {
            // TODO: (timkornau): implement logging here.
          }
        }
      }
    }
  }
}
