// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.types.graphs.IGraphEdge;

import java.awt.Color;
import java.util.List;


public interface IViewEdge<NodeType> extends IGraphEdge<NodeType>, ICodeEdge<NodeType> {
  void addBend(double x, double y);

  void addListener(IViewEdgeListener listener);

  void clearBends();

  int getBendCount();

  List<CBend> getBends();

  Color getColor();

  int getId();

  @Override
  EdgeType getType();

  double getX1();

  double getX2();

  double getY1();

  double getY2();

  void insertBend(int index, double x, double y);

  boolean isSelected();

  boolean isVisible();

  void removeBend(int index);

  void removeListener(IViewEdgeListener listener);

  void setColor(Color color);

  void setEdgeType(EdgeType type);

  void setId(int id);

  void setSelected(boolean selected);

  void setVisible(boolean visible);

  void setX1(double x1);

  void setX2(double x2);

  void setY1(double y1);

  void setY2(double y2);
}
