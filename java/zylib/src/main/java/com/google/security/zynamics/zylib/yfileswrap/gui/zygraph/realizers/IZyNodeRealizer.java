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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import y.view.NodeRealizer;

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
