// Copyright 2011-2020 Google LLC
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

import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

/**
 * Objects that implement this interface can be used to filter nodes from node lists.
 * 
 * @param <NodeType> The type of the nodes in the list.
 */
public interface INodeFilter<NodeType> extends ICollectionFilter<NodeType> {
  /**
   * Determines whether a node passes the filter check.
   * 
   * @param node The node in question.
   * @return True, if the node passes the filter check. False, if the node does not pass the filter
   *         check.
   */
  @Override
  boolean qualifies(NodeType node);
}
