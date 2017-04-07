package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public interface ISearchableTreeNode {
  ZyGraphNode<?> getGraphNode();

  ZyGraphNode<?> getGraphNode(ESide side);
}
