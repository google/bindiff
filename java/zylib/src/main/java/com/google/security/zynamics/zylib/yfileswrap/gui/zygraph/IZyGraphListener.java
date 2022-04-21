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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.view.EdgeLabel;

import java.awt.event.MouseEvent;

public interface IZyGraphListener<NodeType, EdgeType> {
  void edgeClicked(EdgeType node, MouseEvent event, double x, double y);

  void edgeLabelEntered(EdgeLabel label, MouseEvent event);

  void edgeLabelExited(EdgeLabel label);

  void nodeClicked(NodeType node, MouseEvent event, double x, double y);

  void nodeEntered(NodeType node, MouseEvent event);

  void nodeHovered(NodeType node, double x, double y);

  void nodeLeft(NodeType node);

  void proximityBrowserNodeClicked(ZyProximityNode<?> proximityNode, MouseEvent event, double x,
      double y);
}
