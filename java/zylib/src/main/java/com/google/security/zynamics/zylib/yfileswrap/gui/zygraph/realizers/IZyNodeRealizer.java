// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

public interface IZyNodeRealizer {
  void addListener(IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener);

  void calcUnionRect(Rectangle2D rectangle);

  Double getBoundingBox();

  double getCenterX();

  double getCenterY();

  Color getFillColor();

  double getHeight();

  ZyLabelContent getNodeContent();

  NodeRealizer getRealizer();

  IRealizerUpdater<? extends ZyGraphNode<?>> getUpdater();

  ZyNodeData<?> getUserData();

  double getWidth();

  double getX();

  double getY();

  boolean isSelected();

  boolean isVisible();

  int positionToRow(double y);

  void regenerate();

  void removeListener(IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener);

  void repaint();

  double rowToPosition(int line);

  void setFillColor(Color color);

  void setHeight(double value);

  void setLineColor(Color borderColor);

  void setSelected(boolean selected);

  void setUpdater(IRealizerUpdater<? extends ZyGraphNode<?>> updater);

  void setUserData(final ZyNodeData<?> data);

  void setWidth(double value);

  void setX(double x);

  void setY(double y);

  void updateContentSelectionColor();
}
