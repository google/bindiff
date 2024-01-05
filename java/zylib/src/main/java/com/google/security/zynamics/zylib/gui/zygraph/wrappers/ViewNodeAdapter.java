// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph.wrappers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ViewNodeAdapter implements IGraphNode<ViewNodeAdapter> {
  private final IViewNode<?> m_viewNode;

  public ViewNodeAdapter(final IViewNode<?> viewNode) {
    Preconditions.checkNotNull(viewNode, "Error: View node argument can not be null");

    m_viewNode = viewNode;
  }

  private static <NodeType extends ZyGraphNode<? extends IViewNode<?>>>
      Collection<NodeType> convert(
          final AbstractZyGraph<NodeType, ?> graph,
          final Collection<? extends IViewNode<?>> nodes) {
    final List<NodeType> list = new ArrayList<NodeType>();

    for (final IViewNode<?> node : nodes) {
      list.add(graph.getNode(node));
    }

    return list;
  }

  public static <NodeType extends ZyGraphNode<? extends IViewNode<?>>> Collection<NodeType> unwrap(
      final AbstractZyGraph<NodeType, ?> graph, final Collection<ViewNodeAdapter> successors) {
    final List<IViewNode<?>> nodes = new FilledList<IViewNode<?>>();

    for (final ViewNodeAdapter fooWrapper : successors) {
      nodes.add(fooWrapper.getNode());
    }

    return convert(graph, nodes);
  }

  public static List<ViewNodeAdapter> wrap(final List<? extends IViewNode<?>> sel) {
    final List<ViewNodeAdapter> l = new ArrayList<ViewNodeAdapter>();

    for (final IViewNode<?> viewNode : sel) {
      l.add(new ViewNodeAdapter(viewNode));
    }

    return l;
  }

  @Override
  public boolean equals(final Object ohs) {
    return (ohs instanceof ViewNodeAdapter) && (((ViewNodeAdapter) ohs).getNode() == getNode());
  }

  @Override
  public List<ViewNodeAdapter> getChildren() {
    final List<? extends IViewEdge<? extends IViewNode<?>>> edges = m_viewNode.getOutgoingEdges();

    final List<IViewNode<?>> nodes = new ArrayList<IViewNode<?>>();

    for (final IViewEdge<? extends IViewNode<?>> viewEdge : edges) {
      nodes.add(viewEdge.getTarget());
    }

    return wrap(nodes);
  }

  public IViewNode<?> getNode() {
    return m_viewNode;
  }

  @Override
  public List<ViewNodeAdapter> getParents() {
    final List<? extends IViewEdge<? extends IViewNode<?>>> edges = m_viewNode.getIncomingEdges();

    final List<IViewNode<?>> nodes = new ArrayList<IViewNode<?>>();

    for (final IViewEdge<? extends IViewNode<?>> viewEdge : edges) {
      nodes.add(viewEdge.getSource());
    }

    return wrap(nodes);
  }

  @Override
  public int hashCode() {
    return m_viewNode.hashCode();
  }
}
