// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph.wrappers;

import com.google.security.zynamics.zylib.gui.zygraph.functions.IteratorFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.Collection;

public class SelectableGraph<NodeType extends ZyGraphNode<?>>
    implements ISelectableGraph<NodeType> {
  private final AbstractZyGraph<NodeType, ?> m_graph;

  private SelectableGraph(final AbstractZyGraph<NodeType, ?> graph) {
    m_graph = graph;
  }

  public static <NodeType extends ZyGraphNode<?>> SelectableGraph<NodeType> wrap(
      final AbstractZyGraph<NodeType, ?> graph) {
    return new SelectableGraph<NodeType>(graph);
  }

  @Override
  public void iterateSelected(final INodeCallback<NodeType> callback) {
    IteratorFunctions.iterateSelected(m_graph, callback);
  }

  @Override
  public void selectNodes(final Collection<NodeType> nodes, final boolean selected) {
    m_graph.selectNodes(nodes, selected);
  }

  @Override
  public void selectNodes(
      final Collection<NodeType> toSelect, final Collection<NodeType> toDeselect) {
    m_graph.selectNodes(toSelect, toDeselect);
  }
}
