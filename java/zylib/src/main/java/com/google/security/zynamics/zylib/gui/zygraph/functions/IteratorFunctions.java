// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.functions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableNode;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class IteratorFunctions {
  public static <EdgeType extends ZyGraphEdge<?, ?, ?> & ISelectableNode & IViewableNode>
      void iterateInvisible(
          final AbstractZyGraph<?, EdgeType> graph, final IEdgeCallback<EdgeType> callback) {
    Preconditions.checkNotNull(callback, "Error: Callback argument can't be null");

    graph.iterateEdges(new IEdgeCallback<EdgeType>() {
      @Override
      public IterationMode nextEdge(final EdgeType edge) {
        if (edge.isVisible()) {
          return IterationMode.CONTINUE;
        }

        return callback.nextEdge(edge);
      }
    });
  }

  /**
   * Iterates over all invisible nodes in the graph.
   *
   * @param callback Callback object that is invoked once for each invisible node in the graph.
   */
  public static <NodeType extends ZyGraphNode<?> & ISelectableNode & IViewableNode>
      void iterateInvisible(
          final AbstractZyGraph<NodeType, ?> graph, final INodeCallback<NodeType> callback) {
    Preconditions.checkNotNull(callback, "Error: Callback argument can't be null");

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (node.isVisible()) {
          return IterationMode.CONTINUE;
        }

        return callback.next(node);
      }
    });
  }

  /**
   * Iterates over all selected nodes in the graph.
   *
   * @param callback Callback object that is invoked once for each selected node in the graph.
   */
  public static <NodeType extends ZyGraphNode<?> & ISelectableNode & IViewableNode>
      void iterateSelected(
          final AbstractZyGraph<NodeType, ?> graph, final INodeCallback<NodeType> callback) {
    Preconditions.checkNotNull(callback, "Error: Callback argument can't be null");

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (!node.isSelected()) {
          return IterationMode.CONTINUE;
        }

        return callback.next(node);
      }
    });
  }

  public static <EdgeType extends ZyGraphEdge<?, ?, ?> & ISelectableNode & IViewableNode>
      void iterateVisible(
          final AbstractZyGraph<?, EdgeType> graph, final IEdgeCallback<EdgeType> callback) {
    Preconditions.checkNotNull(callback, "Error: Callback argument can't be null");

    graph.iterateEdges(new IEdgeCallback<EdgeType>() {
      @Override
      public IterationMode nextEdge(final EdgeType edge) {
        if (!edge.isVisible()) {
          return IterationMode.CONTINUE;
        }

        return callback.nextEdge(edge);
      }
    });
  }

  /**
   * Iterates over all visible nodes in the graph.
   *
   * @param callback Callback object that is invoked once for each visible node in the graph.
   */
  public static <NodeType extends ZyGraphNode<?> & ISelectableNode & IViewableNode>
      void iterateVisible(
          final AbstractZyGraph<NodeType, ?> graph, final INodeCallback<NodeType> callback) {
    Preconditions.checkNotNull(callback, "Error: Callback argument can't be null");

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (!node.isVisible()) {
          return IterationMode.CONTINUE;
        }

        return callback.next(node);
      }
    });
  }
}
