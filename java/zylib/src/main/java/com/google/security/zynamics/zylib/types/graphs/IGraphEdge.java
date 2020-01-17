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

package com.google.security.zynamics.zylib.types.graphs;

/**
 * Interface for graph edges.
 * 
 * @param <T> The type of the nodes connected by the edge.
 */
public interface IGraphEdge<T> {
  /**
   * Returns the source node of the edge.
   * 
   * @return The source node of the edge.
   */
  T getSource();

  /**
   * Returns the target node of the edge.
   * 
   * @return The target node of the edge.
   */
  T getTarget();
}
