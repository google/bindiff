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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.functions.NodeFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.SelectedVisibleFilter;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.MoveFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;

public class CRegisterHotKeys {
  public static <NodeType extends ZyGraphNode<?>> void register(
      final AbstractZyGraph<NodeType, ?> graph) {
    final Graph2DView view = graph.getView();

    final Graph2DViewActions actions = new Graph2DViewActions(view);
    final ActionMap amap = actions.createActionMap();
    final InputMap imap = actions.createDefaultInputMap(amap);

    view.setActionMap(amap);
    view.setInputMap(JComponent.WHEN_FOCUSED, imap);
    view.getCanvasComponent().setActionMap(amap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);

    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "+");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "-");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "m");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "s");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LESS, 0), "<");
    imap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "SELECT_VISIBLE_NODES");
    imap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
        "COPY_CONTENT_FROM_SELECTED_NODES");

    amap.remove(Graph2DViewActions.DELETE_SELECTION);
    amap.remove(Graph2DViewActions.EDIT_LABEL);

    registerActions(graph);
  }

  public static <NodeType extends ZyGraphNode<?>> void registerActions(
      final AbstractZyGraph<NodeType, ?> graph) {
    final ActionMap amap = graph.getView().getCanvasComponent().getActionMap();

    amap.put("DOWN", new CActionHotKey<NodeType>("DOWN", graph));
    amap.put("UP", new CActionHotKey<NodeType>("UP", graph));
    amap.put("LEFT", new CActionHotKey<NodeType>("LEFT", graph));
    amap.put("RIGHT", new CActionHotKey<NodeType>("RIGHT", graph));
    amap.put("+", new CActionHotKey<NodeType>("+", graph));
    amap.put("-", new CActionHotKey<NodeType>("-", graph));
    amap.put("m", new CActionHotKey<NodeType>("m", graph));
    amap.put("s", new CActionHotKey<NodeType>("s", graph));
    amap.put("<", new CActionHotKey<NodeType>("<", graph));
    amap.put("SELECT_VISIBLE_NODES", new CActionHotKey<NodeType>("SELECT_VISIBLE_NODES", graph));
    amap.put(
        "COPY_CONTENT_FROM_SELECTED_NODES",
        new CActionHotKey<NodeType>("COPY_CONTENT_FROM_SELECTED_NODES", graph));

    graph.getView().setActionMap(amap);
    graph.getView().getCanvasComponent().setActionMap(amap);
  }

  public static <NodeType extends ZyGraphNode<?>> void unregisterActions(
      final AbstractZyGraph<NodeType, ?> graph) {
    final ActionMap amap1 = graph.getView().getCanvasComponent().getActionMap();
    final ActionMap amap2 = graph.getView().getActionMap();

    amap1.remove("F2");
    amap1.remove("DOWN");
    amap1.remove("UP");
    amap1.remove("LEFT");
    amap1.remove("RIGHT");
    amap1.remove("+");
    amap1.remove("-");
    amap1.remove("m");
    amap1.remove("s");
    amap1.remove("<");
    amap1.remove("SELECT_VISIBLE_NODES");
    amap1.remove("COPY_CONTENT_FROM_SELECTED_NODES");

    amap2.remove("DOWN");
    amap2.remove("UP");
    amap2.remove("LEFT");
    amap2.remove("RIGHT");
    amap2.remove("+");
    amap2.remove("-");
    amap2.remove("m");
    amap2.remove("s");
    amap2.remove("<");
    amap2.remove("SELECT_VISIBLE_NODES");
    amap2.remove("COPY_CONTENT_FROM_SELECTED_NODES");
  }

  private static class CActionHotKey<NodeType extends ZyGraphNode<? extends IViewNode<?>>>
      extends AbstractAction {
    private static final long serialVersionUID = 4029488848855226091L;

    private final String m_action;

    private final AbstractZyGraph<NodeType, ?> m_graph;

    public CActionHotKey(final String action, final AbstractZyGraph<NodeType, ?> graph) {
      super(action);

      m_action = action;

      m_graph = graph;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (m_action.equals("UP")) {
        MoveFunctions.pan(m_graph, 0, -1);
      } else if (m_action.equals("DOWN")) {
        MoveFunctions.pan(m_graph, 0, 1);
      } else if (m_action.equals("LEFT")) {
        MoveFunctions.pan(m_graph, -1, 0);
      } else if (m_action.equals("RIGHT")) {
        MoveFunctions.pan(m_graph, 1, 0);
      } else if (m_action.equals("+")) {
        m_graph.zoomIn();
      } else if (m_action.equals("-")) {
        m_graph.zoomOut();
      } else if (m_action.equals("m")) {
        m_graph.getView().fitContent(true);
      } else if (m_action.equals("s")) {
        ZoomFunctions.zoomToNodes(
            m_graph, SelectedVisibleFilter.filter(m_graph.getSelectedNodes()));
      } else if (m_action.equals("SELECT_VISIBLE_NODES")) {
        // Use a temporary variable to work around OpenJDK build problem. Original code is:
        // m_graph.selectNodes(NodeFunctions.getVisibleNodes(m_graph), true);
        final Collection<NodeType> nodes = NodeFunctions.getVisibleNodes(m_graph);

        m_graph.selectNodes(nodes, true);
      } else if (m_action.equals("COPY_CONTENT_FROM_SELECTED_NODES")) {
        ClipboardHelpers.copyToClipboard(GraphHelpers.getSelectedContent(m_graph));
      }
    }
  }
}
