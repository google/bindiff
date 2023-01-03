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
