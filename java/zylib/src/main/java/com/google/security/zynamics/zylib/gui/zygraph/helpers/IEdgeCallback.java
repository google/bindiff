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

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Objects that implement this interface can be used as callback objects when iterating over the
 * edges in a graph.
 *
 * @param <EdgeType> The type of the nodes in the graph.
 */
public interface IEdgeCallback<EdgeType> {
  /**
   * This function is called by the iterator object for each edge of a graph that is considered
   * during iteration.
   *
   * @param edge An edge of the graph.
   * @return Information that's passed back to the iterator object to help the object to find out
   *     what to do next.
   */
  IterationMode nextEdge(EdgeType edge);
}
