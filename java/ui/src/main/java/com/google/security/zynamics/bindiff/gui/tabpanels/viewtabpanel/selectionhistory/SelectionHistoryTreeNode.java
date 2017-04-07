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
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public class SelectionHistoryTreeNode extends AbstractSelectionHistoryTreeNode {
  private static final ImageIcon ICON_SELECTED_GRAPHNODE =
      ImageUtils.getImageIcon("data/selectionicons/graphnode-selection.png");
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES =
      ImageUtils.getImageIcon("data/selectionicons/no-selected-graphnodes.png");
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES_GRAY =
      ImageUtils.getImageIcon("data/selectionicons/no-selected-graphnodes-gray.png");

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

    if (graph.getGraphType() == EGraphType.CALLGRAPH) {
      return new CallGraphPopupMenu(getRootNode().getController(), graph, node);
    } else if (graph.getGraphType() == EGraphType.FLOWGRAPH) {
      return new FlowGraphPopupMenu(getRootNode().getController(), graph, node);
    }

    return null;
  }
}
