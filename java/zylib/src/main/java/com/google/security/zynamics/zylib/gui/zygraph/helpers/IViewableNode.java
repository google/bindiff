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

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

/**
 * Nodes that implement this interface unlock {@link GraphHelpers} functions that require nodes that
 * can be displayed on the screen.
 */
public interface IViewableNode {
  /**
   * Enlarges the given rectangle such that it will contain the bounding box of the node.
   *
   * @param rectangle The rectangle.
   */
  void calcUnionRect(Rectangle2D rectangle);

  /**
   * Returns the bounding box of the node.
   *
   * @return The bounding box of the node.
   */
  Double getBoundingBox();
}
