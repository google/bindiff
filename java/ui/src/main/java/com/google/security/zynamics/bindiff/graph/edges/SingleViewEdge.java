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

package com.google.security.zynamics.bindiff.graph.edges;

import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import java.awt.Color;
import java.util.ArrayList;

/** Edge type for the graphs underlying a single diff view (primary or secondary). */
public abstract class SingleViewEdge<NodeType extends SingleViewNode> extends CViewEdge<NodeType> {
  public SingleViewEdge(final NodeType sourceNode, final NodeType targetNode) {
    super(
        -1,
        sourceNode,
        targetNode,
        EdgeType.DUMMY,
        0.,
        0.,
        0.,
        0.,
        Color.BLACK,
        false,
        true,
        new ArrayList<>());

    SingleViewNode.link(sourceNode, targetNode);

    sourceNode.addOutgoingEdge(this);
    targetNode.addIncomingEdge(this);
  }
}
