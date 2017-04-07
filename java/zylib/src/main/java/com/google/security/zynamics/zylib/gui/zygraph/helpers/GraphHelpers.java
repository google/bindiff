// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.functions.IteratorFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.common.IFilteredItemCallback;
import com.google.security.zynamics.zylib.types.common.IIterableCollection;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides a number of helper functions that are useful when working with graphs.
 */
public class GraphHelpers {
  public static <NodeType> boolean any(final IIterableGraph<NodeType> graph,
      final INodeFilter<NodeType> filter) {
    final ArrayList<Object> lameHack = new ArrayList<Object>();

    graph.iterate(new INodeCallback<NodeType>() {

      @Override
      public IterationMode next(final NodeType item) {
        if (filter.qualifies(item)) {
          lameHack.add(item);

          return IterationMode.STOP;
        }

        return IterationMode.CONTINUE;
      }

    });

    return lameHack.size() != 0;
  }

  /**
   * Determines whether all children of a node are deselected.
   *
   * @param <NodeType> The type of the node and its children.
   *
   * @param node The node in question.
   *
   * @return True, if all children of the node are deselected. False, otherwise.
   */
  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> boolean areAllChildrenDeselected(
      final NodeType node) {
    final INodeFilter<NodeType> filter = StandardFilters.getSelectedFilter();

    return GraphAlgorithms.collectChildren(node, filter).size() == 0;
  }

  /**
   * Determines whether all parents of a node are deselected.
   *
   * @param <NodeType> The type of the node and its parents.
   *
   * @param node The node in question.
   *
   * @return True, if all parents of the node are deselected. False, otherwise.
   */
  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> boolean areAllParentsDeselected(
      final NodeType node) {
    final INodeFilter<NodeType> filter = StandardFilters.getSelectedFilter();

    return GraphAlgorithms.collectParents(node, filter).size() == 0;
  }

  /**
   * Calculates the bounding box of a list of nodes.
   *
   * @param <NodeType> The type of the nodes in the list.
   *
   * @param nodes The list of nodes.
   *
   * @return The bounding box of the list of nodes.
   */
  public static <NodeType extends IViewableNode> Rectangle2D calculateBoundingBox(
      final Collection<NodeType> nodes) {
    Preconditions.checkNotNull(nodes, "Node list argument can't be null");
    Preconditions.checkArgument(nodes.size() != 0, "Node list argument can't be empty");

    final Rectangle2D box = new Rectangle2D.Double();

    for (final NodeType node : nodes) {
      node.calcUnionRect(box);
    }

    return box;
  }

  /**
   * Expands the current selection down. This means that all children of the currently selected
   * nodes are selected too.
   *
   * @param <NodeType> The node type of all nodes in the graph.
   *
   * @param graph The graph where the current selection is expanded.
   */
  public static <NodeType extends IGraphNode<NodeType> & ISelectableNode> void expandSelectionDown(
      final ISelectableGraph<NodeType> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");
    final ArrayList<NodeType> toSelect = new ArrayList<NodeType>();
    final INodeFilter<NodeType> deselectedFilter = StandardFilters.getDeselectedFilter();

    // Collect all the nodes that must be selected
    graph.iterateSelected(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        toSelect.addAll(GraphAlgorithms.collectChildren(node, deselectedFilter));

        return IterationMode.CONTINUE;
      }
    });

    graph.selectNodes(toSelect, true);
  }

  /**
   * Expands the current selection up. This means that all parents of the currently selected nodes
   * are selected too.
   *
   * @param <NodeType> Type of the nodes in the graph.
   *
   * @param graph The graph in question.
   */
  public static <NodeType extends IGraphNode<NodeType> & ISelectableNode> void expandSelectionUp(
      final ISelectableGraph<NodeType> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");
    final ArrayList<NodeType> toSelect = new ArrayList<NodeType>();
    final INodeFilter<NodeType> deselectedFilter = StandardFilters.getDeselectedFilter();

    graph.iterateSelected(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        toSelect.addAll(GraphAlgorithms.collectParents(node, deselectedFilter));

        return IterationMode.CONTINUE;
      }

    });

    graph.selectNodes(toSelect, true);
  }

  /**
   * Returns a list of nodes that match the given filter.
   *
   * @param <NodeType> Type of the nodes in the graph.
   *
   * @param graph The graph that provides the nodes.
   * @param filter The filter that selects the nodes.
   *
   * @return The list of nodes that passes the filter.
   */
  public static <NodeType> List<NodeType> filter(
      final IIterableCollection<INodeCallback<NodeType>> graph,
      final ICollectionFilter<NodeType> filter) {
    final List<NodeType> items = new ArrayList<NodeType>();

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (filter.qualifies(node)) {
          items.add(node);
        }

        return IterationMode.CONTINUE;
      }
    });

    return items;
  }

  public static <EdgeType> List<EdgeType> getEdges(final IEdgeIterableGraph<EdgeType> graph) {
    final INodeFilter<EdgeType> trueFilter = StandardFilters.getTrueFilter();

    return getEdges(graph, trueFilter);
  }

  public static <EdgeType> List<EdgeType> getEdges(final IEdgeIterableGraph<EdgeType> graph,
      final INodeFilter<EdgeType> filter) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");
    Preconditions.checkNotNull(filter, "Error: Filter argument can't be null");
    final ArrayList<EdgeType> nodes = new ArrayList<EdgeType>();

    graph.iterateEdges(new IEdgeCallback<EdgeType>() {
      @Override
      public IterationMode nextEdge(final EdgeType node) {
        if (filter.qualifies(node)) {
          nodes.add(node);
        }

        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  public static <NodeType extends ISelectableNode> List<NodeType> getInvisibleNodes(
      final IViewableGraph<NodeType> graph) {
    final ArrayList<NodeType> nodes = new ArrayList<NodeType>();

    graph.iterateInvisible(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        nodes.add(node);
        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  /**
   * Returns all nodes in the graph.
   *
   * @param <NodeType> Type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return A list of all nodes in the graph.
   */
  public static <NodeType> List<NodeType> getNodes(final IIterableGraph<NodeType> graph) {
    final INodeFilter<NodeType> trueFilter = StandardFilters.getTrueFilter();

    return getNodes(graph, trueFilter);
  }

  /**
   * Returns all nodes in the graph that pass a filter check.
   *
   * @param <NodeType> Type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return A list of all nodes in the graph that pass the filter check.
   */
  public static <NodeType> List<NodeType> getNodes(final IIterableGraph<NodeType> graph,
      final INodeFilter<NodeType> filter) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");
    Preconditions.checkNotNull(filter, "Error: Filter argument can't be null");

    final ArrayList<NodeType> nodes = new ArrayList<NodeType>();

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (filter.qualifies(node)) {
          nodes.add(node);
        }

        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  /**
   * Returns the predecessors of the selected nodes of a graph.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return A collection of all predecessor nodes of the selected graph nodes.
   */
  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> Collection<NodeType> getPredecessorsOfSelection(
      final ISelectableGraph<NodeType> graph) {
    return GraphAlgorithms.getPredecessors(getSelectedNodes(graph));
  }

  public static <NodeType extends ZyGraphNode<?>> String getSelectedContent(
      final AbstractZyGraph<NodeType, ?> graph) {
    final StringBuilder selection = new StringBuilder();

    IteratorFunctions.iterateSelected(graph, new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        final IZyNodeRealizer realizer = node.getRealizer();

        final ZyLabelContent content = realizer.getNodeContent();

        for (final ZyLineContent line : content) {
          selection.append(line.getText());
          selection.append("\n");
        }

        selection.append("\n");

        return IterationMode.CONTINUE;
      }
    });

    return selection.toString();
  }

  /**
   * Creates a list of all selected nodes of the graph.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return All selected nodes of the graph.
   */
  @Deprecated // Use m_graph.getSelectedNodes
  public static <NodeType extends ISelectableNode> IFilledList<NodeType> getSelectedNodes(
      final ISelectableGraph<NodeType> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");
    final IFilledList<NodeType> nodes = new FilledList<NodeType>();

    graph.iterateSelected(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        nodes.add(node);
        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> Collection<NodeType> getSuccessors(
      final NodeType node) {
    final List<NodeType> nodes = new ArrayList<NodeType>();

    nodes.add(node);

    return GraphAlgorithms.getSuccessors(nodes);
  }

  /**
   * Returns the successors of the selected nodes of a graph.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return A collection of all successor nodes of the selected graph nodes.
   */
  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> Collection<NodeType> getSuccessorsOfSelection(
      final ISelectableGraph<NodeType> graph) {
    return GraphAlgorithms.getSuccessors(getSelectedNodes(graph));
  }

  /**
   * Creates a list of all unselected nodes of the graph.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return All selected nodes of the graph.
   */
  public static <NodeType extends ISelectableNode> List<NodeType> getUnselectedNodes(
      final IIterableGraph<NodeType> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can't be null");

    final ArrayList<NodeType> nodes = new ArrayList<NodeType>();

    graph.iterate(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        if (!node.isSelected()) {
          nodes.add(node);
        }

        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  /**
   * Creates a list of all visible nodes of the graph.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   *
   * @return All visible nodes of the graph.
   */
  public static <NodeType extends ISelectableNode> Set<NodeType> getVisibleNodes(
      final IViewableGraph<NodeType> graph) {
    final Set<NodeType> nodes = new HashSet<NodeType>();

    graph.iterateVisible(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        nodes.add(node);
        return IterationMode.CONTINUE;
      }
    });

    return nodes;
  }

  public static <ItemType,
      CollectionType extends IIterableCollection<INodeCallback<ItemType>>> void iterate(
      final CollectionType collection, final IFilteredItemCallback<ItemType> callback) {
    collection.iterate(new INodeCallback<ItemType>() {
      @Override
      public IterationMode next(final ItemType node) {
        if (callback.qualifies(node)) {
          return callback.next(node);
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Selects all predecessors of a node.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   */
  public static <NodeType extends IGraphNode<NodeType> & ISelectableNode> void selectPredecessors(
      final ISelectableGraph<NodeType> graph, final NodeType node) {
    graph.selectNodes(GraphAlgorithms.getPredecessors(node), true);
  }

  /**
   * Selects all predecessors of the selected nodes.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   */
  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> void selectPredecessorsOfSelection(
      final ISelectableGraph<NodeType> graph) {
    graph.selectNodes(getPredecessorsOfSelection(graph), true);
  }

  public static <NodeType extends IGraphNode<NodeType> & ISelectableNode,
      GraphType extends ISelectableGraph<NodeType>> void selectSuccessors(final GraphType graph,
      final NodeType node) {
    graph.selectNodes(GraphAlgorithms.getSuccessors(node), true);
  }

  /**
   * Selects all successors of the selected nodes.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   */
  public static <
      NodeType extends IGraphNode<NodeType> & ISelectableNode> void selectSuccessorsOfSelection(
      final ISelectableGraph<NodeType> graph) {
    graph.selectNodes(getSuccessorsOfSelection(graph), true);
  }

  /**
   * Shrinks the current selection down. This means that all selected nodes without incoming edges
   * are unselected.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   */
  public static <NodeType extends IGraphNode<NodeType> & ISelectableNode> void shrinkSelectionDown(
      final ISelectableGraph<NodeType> graph) {
    final ArrayList<NodeType> deselect = new ArrayList<NodeType>();

    graph.iterateSelected(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        // Nodes without incoming edges into the subgraph formed by the selected nodes
        // are unselected. Those are just the nodes which have no selected parents.

        if (areAllParentsDeselected(node)) {
          if (!deselect.contains(node)) {
            deselect.add(node);
          }
        }

        return IterationMode.CONTINUE;
      }
    });

    graph.selectNodes(deselect, false);
  }

  /**
   * Shrinks the current selection up. This means that all selected nodes without outgoing edges are
   * unselected.
   *
   * @param <NodeType> The type of the nodes in the graph.
   *
   * @param graph The graph in question.
   */
  public static <NodeType extends IGraphNode<NodeType> & ISelectableNode> void shrinkSelectionUp(
      final ISelectableGraph<NodeType> graph) {
    final ArrayList<NodeType> deselect = new ArrayList<NodeType>();

    graph.iterateSelected(new INodeCallback<NodeType>() {
      @Override
      public IterationMode next(final NodeType node) {
        // Nodes without outgoing edges into the subgraph formed by the selected nodes
        // are unselected. Those are just the nodes which have no selected children.

        if (areAllChildrenDeselected(node)) {
          if (!deselect.contains(node)) {
            deselect.add(node);
          }
        }

        return IterationMode.CONTINUE;
      }
    });

    graph.selectNodes(deselect, false);
  }

  /**
   * Zooms so far into the graph that all selected nodes are displayed as big as possible.
   *
   * @param <NodeType> The type of the nodes in the graph.
   * @param <GraphType> The type of the graph.
   *
   * @param graph The graph in question.
   */
  public static <NodeType extends ISelectableNode,
      GraphType extends ISelectableGraph<NodeType> & IZoomableGraph<NodeType>> void zoomToSelected(
      final GraphType graph) {
    final List<NodeType> selectedNodes = getSelectedNodes(graph);

    if (selectedNodes.size() == 0) {
      throw new IllegalStateException("Error: There are no selected nodes");
    }

    graph.zoomToNodes(selectedNodes);
  }
}
