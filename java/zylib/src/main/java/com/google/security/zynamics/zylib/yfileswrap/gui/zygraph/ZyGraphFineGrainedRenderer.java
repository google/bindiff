// Copyright 2011-2024 Google LLC
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

import com.google.security.zynamics.zylib.gui.zygraph.IFineGrainedSloppyGraph2DView;
import java.awt.Graphics2D;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;

public class ZyGraphFineGrainedRenderer<ViewType extends IFineGrainedSloppyGraph2DView>
    extends DefaultGraph2DRenderer {
  /**
   * This class implements the more fine-grained rendering by overloading the regular "paint"
   * methods for nodes & edges. Within this function, the decision on whether to paint the
   * nodes/edges in sloppy or non-sloppy mode is then made.
   */
  protected ViewType m_view;

  private boolean m_drawEdges = true;

  public ZyGraphFineGrainedRenderer(final ViewType view) {
    m_view = view;
  }

  @Override
  protected void paint(final Graphics2D gfx, final EdgeRealizer er) {
    if (!m_drawEdges) {
      return;
    }
    if (m_view.isEdgeSloppyPaintMode()) {
      er.paintSloppy(gfx);
    } else {
      er.paint(gfx);
    }
  }

  @Override
  protected void paint(final Graphics2D gfx, final NodeRealizer nr) {
    if (m_view.isNodeSloppyPaintMode()) {
      nr.paintSloppy(gfx);
    } else {
      nr.paint(gfx);
    }
  }

  @Override
  protected void paintLayered(final Graphics2D gfx, final int layer, final EdgeRealizer er) {
    super.paintLayered(gfx, layer, er);
  }

  // Layered painting methods
  @Override
  protected void paintLayered(final Graphics2D gfx, final int layer, final NodeRealizer nr) {
    super.paintLayered(gfx, layer, nr);
  }

  @Override
  protected void paintSloppy(final Graphics2D gfx, final EdgeRealizer er) {
    assert false : "The sloppy paint methods should not be called";
  }

  @Override
  protected void paintSloppy(final Graphics2D gfx, final NodeRealizer nr) {
    assert false : "The sloppy paint methods should not be called";
  }

  @Override
  protected void paintSloppyLayered(final Graphics2D gfx, final int layer, final EdgeRealizer er) {
    assert false : "The sloppy paint methods should not be called";
  }

  // Sloppy Layered painting methods. These shouldn't actually be reached any
  // more due to the ZyGraph2DView deferring the sloppy/non-sloppy decision to
  // this renderer.
  @Override
  protected void paintSloppyLayered(final Graphics2D gfx, final int layer, final NodeRealizer nr) {
    assert false : "The sloppy paint methods should not be called";
  }

  // Regular painting methods
  @Override
  public void paint(final Graphics2D gfx, final Graph2D graph) {
    // Get & cache these values in the local object to prevent excessive method
    // invocation overhead when all the edges & nodes are drawn below
    m_drawEdges = m_view.drawEdges();
    super.paint(gfx, graph);
  }
}
