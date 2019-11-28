package com.google.security.zynamics.bindiff.graph.filter;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper functions for filtering edges by visibility state, etc.
 *
 * @author nilsheumer@google.com (Nils Heumer)
 */
public class GraphNodeFilter {
  /**
   * Constructs a new instance of this class. This constructor is private to prevent instantiation.
   */
  private GraphNodeFilter() {
    // Do nothing here
  }

  /** Enum with filter criteria */
  public enum Criterion {
    VISIBLE,
    INVISIBLE,
    SELECTED,
    SELECTED_VISIBLE,
    UNSELECTED_VISIBLE
  }

  /**
   * Filter the nodes from a list that match a given filter criterion.
   *
   * @param nodes a {@code Collection} containing the nodes to be filtered
   * @param filterBy the criterion to filter by
   * @param resultSet a {@code Collection} of nodes that match the specified criterion. If {@code
   *     null}, this method only counts the matching nodes.
   * @return The number of nodes in the graph matching the filter criterion
   */
  private static <NodeType extends ZyGraphNode<?>> int internalFilterNodes(
      final Collection<NodeType> nodes,
      final Criterion filterBy,
      final Collection<NodeType> resultSet) {
    Preconditions.checkNotNull(filterBy);

    int count = 0;
    boolean addToResult;
    for (final NodeType node : nodes) {
      switch (filterBy) {
        case VISIBLE:
          addToResult = node.isVisible();
          break;
        case INVISIBLE:
          addToResult = !node.isVisible();
          break;
        case SELECTED:
          addToResult = node.isSelected();
          break;
        case SELECTED_VISIBLE:
          addToResult = node.isSelected() && node.isVisible();
          break;
        case UNSELECTED_VISIBLE:
          addToResult = !node.isSelected() && node.isVisible();
          break;
        default:
          // Should not happen
          throw new IllegalArgumentException("Invalid node filter criterion");
      }
      if (addToResult) {
        count++;
        if (resultSet != null) {
          resultSet.add(node);
        }
      }
    }
    return count;
  }

  /**
   * Filter the nodes of an {@code AbstractZyGraph} that match a given filter criterion.
   *
   * @param graph the graph containing the nodes to be filtered
   * @param filterBy the criterion to filter by
   * @param resultSet a {@code Collection} of nodes that match the specified criterion. If {@code
   *     null}, this method only counts the matching nodes.
   * @return The number of nodes in the graph matching the filter criterion
   */
  private static <NodeType extends ZyGraphNode<?>> int internalFilterNodes(
      final AbstractZyGraph<NodeType, ?> graph,
      final Criterion filterBy,
      final Collection<NodeType> resultSet) {
    return internalFilterNodes(graph.getNodes(), filterBy, resultSet);
  }

  /**
   * Filter the nodes of an {@code AbstractZyGraph} that match a given filter criterion.
   *
   * @param graph the graph containing the nodes to be filtered
   * @param filterBy the criterion to filter by
   * @return a {@code Collection} of nodes that match the specified criterion. If {@code null}, this
   *     method only counts the matching nodes.
   */
  public static <NodeType extends ZyGraphNode<?>> List<NodeType> filterNodes(
      final AbstractZyGraph<NodeType, ?> graph, final Criterion filterBy) {
    final List<NodeType> resultSet = new ArrayList<>();
    internalFilterNodes(graph, filterBy, resultSet);
    return resultSet;
  }

  /**
   * Count the nodes of an {@code AbstractZyGraph} matching a specified filter criterion.
   *
   * @param graph the graph containing the nodes to be filtered
   * @param filterBy the criterion to filter by
   * @return the number of nodes matching the criterion
   */
  public static int filterNodesCountOnly(
      final AbstractZyGraph<?, ?> graph, final Criterion filterBy) {
    return internalFilterNodes(graph, filterBy, null);
  }

  /**
   * Filter the nodes from a list that match a given filter criterion.
   *
   * @param nodes a {@code Collection} containing the nodes to be filtered
   * @param filterBy the criterion to filter by
   * @return a {@code Collection} of nodes that match the specified criterion. If {@code null}, this
   *     method only counts the matching nodes.
   */
  public static <NodeType extends ZyGraphNode<?>> List<NodeType> filterNodes(
      final Collection<NodeType> nodes, final Criterion filterBy) {
    final List<NodeType> resultSet = new ArrayList<>();
    internalFilterNodes(nodes, filterBy, resultSet);
    return resultSet;
  }
}
