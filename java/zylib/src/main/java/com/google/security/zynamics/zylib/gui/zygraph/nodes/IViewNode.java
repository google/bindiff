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

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

import java.awt.Color;
import java.util.List;



/**
 * Interface that must be implemented by all objects that represent nodes in views.
 */
public interface IViewNode<EdgeType extends IViewEdge<? extends IViewNode<?>>> {
  void addListener(IViewNodeListener listener);

  Color getBorderColor();

  /**
   * Returns the background color of the node.
   * 
   * @return The background color of the node.
   */
  Color getColor();

  double getHeight();

  /**
   * Returns the ID of the node.
   * 
   * @return The ID of the node.
   */
  int getId();

  /**
   * Returns the incoming edges of the node.
   * 
   * @return The incoming edges of the node.
   */
  List<EdgeType> getIncomingEdges();

  /**
   * Returns the outgoing edges of the node.
   * 
   * @return The outgoing edges of the node.
   */
  List<EdgeType> getOutgoingEdges();

  IGroupNode<?, ?> getParentGroup();

  double getWidth();

  /**
   * Returns the X coordinate of the node in a view.
   * 
   * @return The X coordinate of the node.
   */
  double getX();

  /**
   * Returns the Y coordinate of the node in a view.
   * 
   * @return The Y coordinate of the node.
   */
  double getY();

  /**
   * Indicates whether the node is selected or not.
   * 
   * @return True, if the node is selected. False, otherwise.
   */
  boolean isSelected();

  /**
   * Indicates whether the node is visible or not.
   * 
   * @return True, if the node is visible. False, otherwise.
   */
  boolean isVisible();

  void removeListener(IViewNodeListener listener);

  void setBorderColor(Color color);

  void setColor(Color color);

  void setHeight(double height);

  void setId(int id);

  void setSelected(boolean value);

  void setVisible(boolean value);

  void setWidth(double width);

  void setX(double xpos);

  void setY(double ypos);
}
