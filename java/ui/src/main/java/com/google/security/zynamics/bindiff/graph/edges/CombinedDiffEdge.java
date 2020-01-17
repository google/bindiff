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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;

public class CombinedDiffEdge
    extends ZyGraphEdge<
        CombinedDiffNode, CombinedDiffEdge, CombinedViewEdge<? extends CombinedViewNode>> {
  private final SingleDiffEdge primaryDiffEdge;
  private final SingleDiffEdge secondaryDiffEdge;
  private final SuperDiffEdge superDiffEdge;

  public CombinedDiffEdge(
      final CombinedDiffNode source,
      final CombinedDiffNode target,
      final Edge edge,
      final ZyEdgeRealizer<CombinedDiffEdge> realizer,
      final CombinedViewEdge<? extends CombinedViewNode> rawEdge,
      final SuperDiffEdge superDiffEdge) {
    super(source, target, edge, realizer, rawEdge);

    this.superDiffEdge = Preconditions.checkNotNull(superDiffEdge);

    this.primaryDiffEdge = superDiffEdge.getPrimaryDiffEdge();
    this.secondaryDiffEdge = superDiffEdge.getSecondaryDiffEdge();
  }

  public SingleDiffEdge getPrimaryDiffEdge() {
    return primaryDiffEdge;
  }

  public SingleDiffEdge getSecondaryDiffEdge() {
    return secondaryDiffEdge;
  }

  public SuperDiffEdge getSuperDiffEdge() {
    return superDiffEdge;
  }
}
