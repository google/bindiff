package com.google.security.zynamics.bindiff.graph;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyDefaultProximityBrowser;

import y.base.Node;

import java.util.List;

public class ProximityBrowser<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends ZyDefaultProximityBrowser<NodeType, EdgeType> {
  public ProximityBrowser(final BinDiffGraph<NodeType, EdgeType> graph) {
    super(graph, graph.getSettings());
    removeSettingsListener();
  }

  @Override
  public void addSettingsListener() {
    super.addSettingsListener();
  }

  @Override
  public void removeSettingsListener() {
    super.removeSettingsListener();
  }

  @Override
  public void createProximityBrowsingNodes(final List<NodeType> allNodes) {
    super.createProximityBrowsingNodes(allNodes);
  }

  @Override
  public void deleteProximityBrowsingNode(final Node yProxyNode) {
    super.deleteProximityBrowsingNode(yProxyNode);
  }

  @Override
  public void deleteProximityBrowsingNodes() {
    super.deleteProximityBrowsingNodes();
  }
}
