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

package com.google.security.zynamics.bindiff.graph.edges;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import java.awt.Color;
import java.util.ArrayList;

public final class SuperViewEdge<NodeType extends SuperViewNode> extends CViewEdge<NodeType> {
  private final CombinedViewEdge<? extends CombinedViewNode> combinedEdge;

  public SuperViewEdge(
      final CombinedViewEdge<? extends CombinedViewNode> combinedEdge,
      final NodeType sourceNode,
      final NodeType targetNode) {
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

    this.combinedEdge = checkNotNull(combinedEdge);

    SuperViewNode.link(sourceNode, targetNode);

    sourceNode.addOutgoingEdge(this);
    targetNode.addIncomingEdge(this);
  }

  public CombinedViewEdge<? extends CombinedViewNode> getCombinedEdge() {
    return combinedEdge;
  }

  public SingleViewEdge<? extends SingleViewNode> getSingleEdge(final ESide side) {
    return side == ESide.PRIMARY ? combinedEdge.getPrimaryEdge() : combinedEdge.getSecondaryEdge();
  }
}
