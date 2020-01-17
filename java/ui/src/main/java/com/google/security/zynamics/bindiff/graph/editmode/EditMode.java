// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.editmode;

import com.google.security.zynamics.bindiff.graph.eventhandlers.DerivedLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CSelectionMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.view.MoveViewPortMode;

/**
 * Base edit mode class for both combined/single graphs that allows to enable left-drag panning of
 * the viewport.
 */
public class EditMode<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends ZyEditMode<NodeType, EdgeType> {

  protected boolean invertMoveViewPort;

  public EditMode(AbstractZyGraph<NodeType, EdgeType> graph) {
    super(graph);

    assignNodeLabel(false);
    allowResizeNodes(false);

    setSelectionBoxMode(new CSelectionMode<NodeType>(graph));

    invertMoveViewPort = true; // TODO(cblichmann): Expose this as a setting
    if (invertMoveViewPort) {
      moveViewPortMode = new InvertMoveViewPortMode();
    }
  }

  public boolean isInvertMoveViewPort() {
    return invertMoveViewPort;
  }

  @Override
  protected DerivedLabelEventHandler createNodeKeyHandler(
      final AbstractZyGraph<NodeType, EdgeType> graph) {
    return new DerivedLabelEventHandler(graph);
  }

  /** Move the viewport on mouse left-drag instead of default right-drag. */
  protected final class InvertMoveViewPortMode extends MoveViewPortMode {
    @Override
    public void mousePressedLeft(double x, double y) {
      super.mousePressedRight(x, y);
    }

    @Override
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedRight(x, y);
      CMouseCursorHelper.setDefaultCursor(getGraph());
    }

    @Override
    public void mouseDraggedLeft(double x, double y) {
      super.mouseDraggedRight(x, y);
    }

    @Override
    public void mousePressedRight(double x, double y) {}

    @Override
    public void mouseReleasedRight(double x, double y) {}

    @Override
    public void mouseDraggedRight(double x, double y) {}
  }

  @Override
  public void mousePressedLeft(final double x, final double y) {
    final MouseEvent e = getLastPressEvent();
    if (!invertMoveViewPort || e == null || (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
      super.mousePressedLeft(x, y);
    } else {
      setChild(moveViewPortMode, e, null);
    }
  }

  @Override
  public void mousePressedRight(final double x, final double y) {
    if (!invertMoveViewPort) {
      super.mousePressedRight(x, y);
    }
  }
}
