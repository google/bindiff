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

import com.google.common.base.Preconditions;

/**
 * Default graph edge implementation.
 * 
 * @param <NodeType> Type of the graph nodes connected by the edge.
 */
public class DefaultEdge<NodeType> implements IGraphEdge<NodeType> {
  /**
   * Source node of the edge.
   */
  private final NodeType m_source;

  /**
   * Target node of the edge.
   */
  private final NodeType m_target;

  /**
   * Creates a new default edge.
   * 
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   */
  public DefaultEdge(final NodeType source, final NodeType target) {
    m_source = Preconditions.checkNotNull(source, "Error: Source argument can not be null");
    m_target = Preconditions.checkNotNull(target, "Error: Target argument can not be null");
  }

  @Override
  public NodeType getSource() {
    return m_source;
  }

  @Override
  public NodeType getTarget() {
    return m_target;
  }
}
