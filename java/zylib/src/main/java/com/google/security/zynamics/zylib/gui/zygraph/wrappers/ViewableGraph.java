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

package com.google.security.zynamics.zylib.gui.zygraph.wrappers;

import com.google.security.zynamics.zylib.gui.zygraph.functions.IteratorFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class ViewableGraph<NodeType extends ZyGraphNode<?>> implements IViewableGraph<NodeType> {
  private final AbstractZyGraph<NodeType, ?> m_graph;

  private ViewableGraph(final AbstractZyGraph<NodeType, ?> graph) {
    m_graph = graph;
  }

  public static <NodeType extends ZyGraphNode<?>> ViewableGraph<NodeType> wrap(
      final AbstractZyGraph<NodeType, ?> graph) {
    return new ViewableGraph<NodeType>(graph);
  }

  @Override
  public void iterateInvisible(final INodeCallback<NodeType> callback) {
    IteratorFunctions.iterateInvisible(m_graph, callback);
  }

  @Override
  public void iterateVisible(final INodeCallback<NodeType> callback) {
    IteratorFunctions.iterateVisible(m_graph, callback);
  }
}
