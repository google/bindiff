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

import java.util.List;

/**
 * Interface for directed graphs.
 * 
 * @param <NodeType> Type of the nodes in the graph.
 * @param <EdgeType> Type of the edges in the graph.
 */
public interface IDirectedGraph<NodeType, EdgeType> extends Iterable<NodeType> {
  /**
   * Returns the number of edges in the graph.
   * 
   * @return The number of edges in the graph.
   */
  int edgeCount();

  /**
   * Returns the edges of the graph.
   * 
   * @return The edges of the graph.
   */
  List<EdgeType> getEdges();

  /**
   * Returns the nodes of the graph.
   * 
   * @return The nodes of the graph.
   */
  List<NodeType> getNodes();

  /**
   * Returns the number of nodes in the graph.
   * 
   * @return The number of nodes in the graph.
   */
  int nodeCount();
}
