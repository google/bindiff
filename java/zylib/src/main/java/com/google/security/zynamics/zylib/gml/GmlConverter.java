// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.IGraphEdge;
import com.google.security.zynamics.zylib.types.trees.ITree;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * This class can be used to generate GML files from input objects.
 */
public final class GmlConverter {
  /**
   * Creates GML code that represents a given directed graph.
   * 
   * @param graph The input graph.
   * 
   * @return The code generated for the input graph.
   */
  public static String toGml(final IDirectedGraph<?, ? extends IGraphEdge<?>> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    final StringBuilder sb = new StringBuilder();

    sb.append("graph\n");
    sb.append("[\n");

    int currentId = 0;

    final Map<Object, Integer> nodeMap = new HashMap<Object, Integer>();

    for (final Object node : graph.getNodes()) {
      sb.append("\tnode\n");
      sb.append("\t[\n");
      sb.append("\tid " + currentId + "\n");
      sb.append("\tlabel \"" + node + "\"\n");
      sb.append("\t]\n");

      nodeMap.put(node, currentId);

      ++currentId;
    }

    for (final IGraphEdge<?> edge : graph.getEdges()) {
      sb.append("\tedge\n");
      sb.append("\t[\n");
      sb.append("\tsource " + nodeMap.get(edge.getSource()) + "\n");
      sb.append("\ttarget " + nodeMap.get(edge.getTarget()) + "\n");
      sb.append("\tgraphics\n");
      sb.append("\t[\n");
      sb.append("\t\tfill \"#000000\"\n");
      sb.append("\t\ttargetArrow \"standard\"\n");
      sb.append("\t]\n");
      sb.append("\t]\n");
    }

    sb.append("]\n");

    return sb.toString();
  }

  public static String toGml(final ITree<?> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    final StringBuilder sb = new StringBuilder();

    sb.append("graph\n");
    sb.append("[\n");

    int currentId = 0;

    final Map<Object, Integer> nodeMap = new HashMap<Object, Integer>();

    final Stack<ITreeNode<?>> stack = new Stack<ITreeNode<?>>();

    stack.push(graph.getRootNode());

    while (!stack.isEmpty()) {
      final ITreeNode<?> node = stack.pop();

      sb.append("\tnode\n");
      sb.append("\t[\n");
      sb.append("\tid " + currentId + "\n");
      sb.append("\tlabel \"" + node + "\"\n");
      sb.append("\t]\n");

      nodeMap.put(node, currentId);

      ++currentId;

      final ITreeNode<?> parent = node.getParent();

      if (parent != null) {
        sb.append("\tedge\n");
        sb.append("\t[\n");
        sb.append("\tsource " + nodeMap.get(parent) + "\n");
        sb.append("\ttarget " + nodeMap.get(node) + "\n");
        sb.append("\tgraphics\n");
        sb.append("\t[\n");
        sb.append("\t\tfill \"#000000\"\n");
        sb.append("\t\ttargetArrow \"standard\"\n");
        sb.append("\t]\n");
        sb.append("\t]\n");
      }

      for (final ITreeNode<?> treeNode : node.getChildren()) {
        stack.push(treeNode);
      }
    }

    sb.append("]\n");

    return sb.toString();
  }
}
