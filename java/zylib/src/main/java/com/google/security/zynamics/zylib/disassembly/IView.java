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

package com.google.security.zynamics.zylib.disassembly;

import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

public interface IView<NodeType extends IViewNode<?>, ListenerType extends IViewListener<?>> {
  void addListener(ListenerType listener);

  boolean close();

  int getEdgeCount();

  IDirectedGraph<? extends NodeType, ? extends IViewEdge<? extends NodeType>> getGraph();

  GraphType getGraphType();

  String getName();

  int getNodeCount();

  ViewType getType();

  boolean isLoaded();

  void removeListener(ListenerType listener);
}
