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

import java.util.Collection;


/**
 * Graphs that implement this interface unlock {@link GraphHelpers} functions that require the
 * ability to zoom the graph.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface IZoomableGraph<NodeType> {
  /**
   * Zooms the graph so far that all nodes in the list are visible.
   * 
   * @param nodes List of nodes that should be displayed as big as possible.
   */
  public void zoomToNodes(final Collection<NodeType> nodes);
}
