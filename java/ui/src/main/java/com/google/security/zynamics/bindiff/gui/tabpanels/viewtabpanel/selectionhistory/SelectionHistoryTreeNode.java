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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.CallGraphPopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.popupmenus.FlowGraphPopupMenu;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public class SelectionHistoryTreeNode extends AbstractSelectionHistoryTreeNode {
  private static final ImageIcon ICON_SELECTED_GRAPHNODE =
      ResourceUtils.getImageIcon("data/selectionicons/graphnode-selection.png");
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES =
      ResourceUtils.getImageIcon("data/selectionicons/no-selected-graphnodes.png");
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES_GRAY =
      ResourceUtils.getImageIcon("data/selectionicons/no-selected-graphnodes-gray.png");

  private final ZyGraphNode<?> node;

  public SelectionHistoryTreeNode(final ZyGraphNode<?> node) {
    super(getNodeName(node));

    this.node = node;
  }

  private static String getCombinedNodeName(final CombinedDiffNode node) {
    String name = "";

    final CombinedViewNode viewNode = node.getRawNode();

    final SingleViewNode primaryNode = viewNode.getRawNode(ESide.PRIMARY);
    if (primaryNode == null) {
      name += "Unmatched";
    } else {
      name += getSingleNodeName(primaryNode);
    }

    name += " \u2194 ";

    final SingleViewNode secondaryNode = viewNode.getRawNode(ESide.SECONDARY);
    if (secondaryNode == null) {
      name += "Unmatched";
    } else {
      name += getSingleNodeName(secondaryNode);
    }

    return name;
  }

  private static String getNodeName(final ZyGraphNode<?> node) {
    String name = "";

    if (node instanceof SingleDiffNode) {
      name += getSingleNodeName((SingleViewNode) node.getRawNode());
    } else if (node instanceof CombinedDiffNode) {
      name += getCombinedNodeName((CombinedDiffNode) node);
    }

    return name;
  }

  private static String getSingleNodeName(final SingleViewNode node) {
    String name = "";

    if (node instanceof RawFunction) {
      name += node.getAddress() + " " + ((RawFunction) node).getName();
    } else if (node instanceof RawBasicBlock) {
      name += node.getAddress();
    }

    return name;
  }

  @Override
  public ImageIcon getIcon() {
    if (!node.isVisible()) {
      return ICON_NO_SELECTED_GRAPHNODES_GRAY;
    }

    if (node.isSelected()) {
      return ICON_SELECTED_GRAPHNODE;
    }

    return ICON_NO_SELECTED_GRAPHNODES;
  }

  public ZyGraphNode<?> getNode() {
    return node;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    final SelectionHistoryRootNode rootNode = getRootNode();
    final BinDiffGraph<?, ?> graph = rootNode.getGraph();

    if (graph.getGraphType() == EGraphType.CALL_GRAPH) {
      return new CallGraphPopupMenu(getRootNode().getController(), graph, node);
    } else if (graph.getGraphType() == EGraphType.FLOW_GRAPH) {
      return new FlowGraphPopupMenu(getRootNode().getController(), graph, node);
    }

    return null;
  }
}
