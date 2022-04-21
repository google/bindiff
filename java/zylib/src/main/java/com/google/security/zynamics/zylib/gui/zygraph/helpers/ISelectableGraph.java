// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import java.util.Collection;


/**
 * Graphs that implement this interface unlock {@link GraphHelpers} functions that require the
 * ability to iterate over selected nodes.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface ISelectableGraph<NodeType> {
  /**
   * Iterates over all selected nodes in the graph.
   * 
   * @param callback Callback object that is called once for each selected node in the graph.
   */
  void iterateSelected(final INodeCallback<NodeType> callback);

  /**
   * Selects or deselects a collection of nodes in the graph.
   * 
   * @param nodes The nodes to select or deselect.
   * @param selected True, to select the given nodes. False, to deselect the given nodes.
   */
  void selectNodes(final Collection<NodeType> nodes, final boolean selected);

  /**
   * Selects a list of nodes while deselecting another list of nodes.
   * 
   * @param toSelect The nodes to select.
   * @param toDeselect The nodes to deselect.
   */
  void selectNodes(final Collection<NodeType> toSelect, final Collection<NodeType> toDeselect);
}
