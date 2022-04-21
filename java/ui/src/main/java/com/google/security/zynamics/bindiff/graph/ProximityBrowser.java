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

package com.google.security.zynamics.bindiff.graph;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyDefaultProximityBrowser;
import java.util.List;
import y.base.Node;

public class ProximityBrowser<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends ZyDefaultProximityBrowser<NodeType, EdgeType> {
  public ProximityBrowser(final BinDiffGraph<NodeType, EdgeType> graph) {
    super(graph, graph.getSettings());
    removeSettingsListener();
  }

  @Override
  public void addSettingsListener() {
    super.addSettingsListener();
  }

  @Override
  public void removeSettingsListener() {
    super.removeSettingsListener();
  }

  @Override
  public void createProximityBrowsingNodes(final List<NodeType> allNodes) {
    super.createProximityBrowsingNodes(allNodes);
  }

  @Override
  public void deleteProximityBrowsingNode(final Node yProxyNode) {
    super.deleteProximityBrowsingNode(yProxyNode);
  }

  @Override
  public void deleteProximityBrowsingNodes() {
    super.deleteProximityBrowsingNodes();
  }
}
