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

package com.google.security.zynamics.zylib.gui.zygraph.functions;

import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;

public class SelectionFunctions {
  /**
   * Inverts the selected nodes of a graph.
   *
   * @param <NodeType> The type of the nodes in the graph.
   * @param graph The graph in question.
   */
  public static <NodeType extends ZyGraphNode<?>> void invertSelection(
      final AbstractZyGraph<NodeType, ?> graph) {
    final ArrayList<NodeType> toSelect = new ArrayList<NodeType>();
    final ArrayList<NodeType> toUnselect = new ArrayList<NodeType>();

    graph.iterate(
        new INodeCallback<NodeType>() {
          @Override
          public IterationMode next(final NodeType node) {
            if (node.isSelected()) {
              toUnselect.add(node);
            } else {
              toSelect.add(node);
            }

            return IterationMode.CONTINUE;
          }
        });

    graph.selectNodes(toSelect, toUnselect);
  }
}
