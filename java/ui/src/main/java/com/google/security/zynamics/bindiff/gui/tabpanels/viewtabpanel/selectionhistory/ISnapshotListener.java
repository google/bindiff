package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public interface ISnapshotListener {
  void addedNode(ZyGraphNode<? extends IViewNode<?>> node);

  void finished();

  void removedNode(ZyGraphNode<? extends IViewNode<?>> node);
}
