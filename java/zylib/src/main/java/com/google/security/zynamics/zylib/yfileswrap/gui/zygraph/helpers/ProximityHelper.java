// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

public class ProximityHelper {
  public static ZyProximityNode<?> getProximityNode(final Graph2D graph, final Node node) {
    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getRealizer(node);

    return (ZyProximityNode<?>) realizer.getUserData().getNode();
  }

  public static boolean isProximityEdge(final Graph2D graph, final Edge edge) {
    return isProximityNode(graph, edge.source()) || isProximityNode(graph, edge.target());
  }

  public static boolean isProximityNode(final Graph2D graph, final Node node) {
    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getRealizer(node);

    return realizer.getUserData().getNode() instanceof ZyProximityNode<?>;
  }
}
