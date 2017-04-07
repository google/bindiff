// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.view.EdgeLabel;

import java.awt.event.MouseEvent;

public interface IZyEditModeListener<NodeType, EdgeType> {
  void edgeClicked(EdgeType node, MouseEvent event, double x, double y);

  void edgeLabelEntered(EdgeLabel label, MouseEvent event);

  void edgeLabelLeft(EdgeLabel label);

  void nodeClicked(NodeType node, MouseEvent event, double x, double y);

  void nodeEntered(NodeType node, MouseEvent event);

  void nodeHovered(NodeType node, double x, double y);

  void nodeLeft(NodeType node);

  void proximityBrowserNodeClicked(ZyProximityNode<?> proximityNode, MouseEvent e, double x,
      double y);

}
