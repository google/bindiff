// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.grouping.GroupHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.Collection;


public class SelectedVisibleFilter<NodeType extends ZyGraphNode<? extends IViewNode<?>>> implements
    ICollectionFilter<NodeType> {
  public static <NodeType extends ZyGraphNode<? extends IViewNode<?>>> Collection<NodeType> filter(
      final Collection<NodeType> collection) {
    return CollectionHelpers.filter(collection, new SelectedVisibleFilter<NodeType>());
  }

  @Override
  public boolean qualifies(final NodeType node) {
    final IViewNode<?> rawNode = (IViewNode<?>) node.getRawNode();

    return (rawNode.getParentGroup() == null) || GroupHelpers.isExpanded(rawNode.getParentGroup());
  }
}
