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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import y.base.Edge;

public class SuperDiffEdge
    extends ZyGraphEdge<SuperDiffNode, SuperDiffEdge, SuperViewEdge<? extends SuperViewNode>> {
  private final SingleDiffEdge primaryDiffEdge;
  private final SingleDiffEdge secondaryDiffEdge;

  private CombinedDiffEdge combinedDiffEdge;

  public SuperDiffEdge(
      final SuperDiffNode source,
      final SuperDiffNode target,
      final Edge edge,
      final ZyEdgeRealizer<SuperDiffEdge> realizer,
      final SuperViewEdge<? extends SuperViewNode> rawEdge,
      final SingleDiffEdge primaryDiffEdge,
      final SingleDiffEdge secondaryDiffEdge) {
    super(source, target, edge, realizer, rawEdge);

    checkArgument(
        primaryDiffEdge != null || secondaryDiffEdge != null,
        "Primary and secondary edge cannot both be null.");

    this.primaryDiffEdge = primaryDiffEdge;
    this.secondaryDiffEdge = secondaryDiffEdge;
  }

  public CombinedDiffEdge getCombinedDiffEdge() {
    return combinedDiffEdge;
  }

  public SingleDiffEdge getPrimaryDiffEdge() {
    return primaryDiffEdge;
  }

  @Override
  public SuperViewEdge<? extends SuperViewNode> getRawEdge() {
    return super.getRawEdge();
  }

  public SingleDiffEdge getSecondaryDiffEdge() {
    return secondaryDiffEdge;
  }

  public void setCombinedDiffEdge(final CombinedDiffEdge combinedDiffEdge) {
    this.combinedDiffEdge = combinedDiffEdge;
  }
}
