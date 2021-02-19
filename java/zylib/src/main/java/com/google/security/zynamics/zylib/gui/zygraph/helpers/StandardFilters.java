// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.CProximityNode;

/**
 * Provides common node filters.
 */
public class StandardFilters {
  /**
   * Returns a filter that can be used to filter selected nodes.
   * 
   * @param <NodeType> The type of the nodes that are filtered by the returned filter.
   * 
   * @return A filter that can be used to filter selected nodes.
   */
  public static <NodeType extends ISelectableNode> INodeFilter<NodeType> getDeselectedFilter() {
    final INodeFilter<NodeType> selectedFilter = getSelectedFilter();

    return getNegatedFilter(selectedFilter);
  }

  public static <NodeType> INodeFilter<NodeType> getInfoNodeFilter() {
    return new INodeFilter<NodeType>() {
      @Override
      public boolean qualifies(final NodeType node) {
        return node instanceof CProximityNode;
      }
    };
  }

  /**
   * Creates a filter that negates another filter.
   * 
   * @param <NodeType> The type of the nodes that are filtered by the returned filter.
   * 
   * @param filter The filter that's negated.
   * 
   * @return A filter that negates the input filter.
   */
  public static <NodeType> INodeFilter<NodeType> getNegatedFilter(final INodeFilter<NodeType> filter) {
    Preconditions.checkNotNull(filter, "Error: Filter argument can't be null");

    return new INodeFilter<NodeType>() {
      @Override
      public boolean qualifies(final NodeType node) {
        return !filter.qualifies(node);
      }
    };
  }

  /**
   * Returns a filter that can be used to filter deselected nodes.
   * 
   * @param <NodeType> The type of the nodes that are filtered by the returned filter.
   * 
   * @return A filter that can be used to filter deselected nodes.
   */
  public static <NodeType extends ISelectableNode> INodeFilter<NodeType> getSelectedFilter() {
    return new INodeFilter<NodeType>() {
      @Override
      public boolean qualifies(final NodeType node) {
        return node.isSelected();
      }
    };
  }

  /**
   * Returns a filter that accepts every node.
   * 
   * @param <NodeType> The type of the nodes that are filtered by the returned filter.
   * 
   * @return A filter that accepts all nodes.
   */
  public static <NodeType> INodeFilter<NodeType> getTrueFilter() {
    return new INodeFilter<NodeType>() {
      @Override
      public boolean qualifies(final NodeType node) {
        return true;
      }
    };
  }

}
