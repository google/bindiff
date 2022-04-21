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

import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class GraphConverters {
  public static <RawType extends IViewNode<?>, NodeType extends ZyGraphNode<RawType>> Collection<NodeType> convert(
      final AbstractZyGraph<NodeType, ?> graph, final Collection<? extends RawType> nodes) {
    final List<NodeType> list = new ArrayList<NodeType>();

    for (final RawType node : nodes) {
      list.add(graph.getNode(node));
    }

    return list;
  }

  public static <NodeType extends IViewNode<?>> List<NodeType> convert(
      final Collection<? extends ZyGraphNode<? extends NodeType>> visibleNodes) {
    final List<NodeType> list = new ArrayList<NodeType>();

    for (final ZyGraphNode<? extends NodeType> node : visibleNodes) {
      @SuppressWarnings("unchecked")
      final NodeType rawNode = (NodeType) node.getRawNode();
      list.add(rawNode);
    }

    return list;
  }
}
