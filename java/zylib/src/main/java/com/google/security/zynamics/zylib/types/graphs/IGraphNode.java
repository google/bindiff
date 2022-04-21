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

package com.google.security.zynamics.zylib.types.graphs;

import java.util.List;

/**
 * Interface for nodes in the graph.
 * 
 * @param <T> The concrete graph node type itself.
 */
public interface IGraphNode<T> {
  /**
   * Returns the children of the graph node.
   * 
   * @return The children of the graph node.
   */
  List<? extends T> getChildren();

  /**
   * Returns the parents of the graph node.
   * 
   * @return The parents of the graph node.
   */
  List<? extends T> getParents();
}
