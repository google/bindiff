// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.IFineGrainedSloppyGraph2DView;

import y.view.Graph2D;
import y.view.Graph2DView;

import java.awt.Cursor;

/**
 * This is a specialized version of the yFiles Graph2DView. It provides the additional functionality
 * of fine-grained control over sloppy edge/node drawing.
 * 
 * The original yFiles Graph2DView allows us to configure a zoom threshold after which the nodes and
 * edges are drawn using "paintSloppy". This thresh- old is the same for both nodes and edges.
 * 
 * For various reasons, we wish to be able to configure this threshold sepa- rately for nodes and
 * edges.
 * 
 * Furthermore, BinNavi implements it's own logic for not drawing sloppy edges at all if the graph
 * to be displayed is too large. This is implemented in the Realizer currently.
 * 
 * We end up providing the required functionality in the following way:
 * 
 * 1) We derive from the original Graph2DView. We disable the internal decision for sloppy/nonsloppy
 * drawing, and perform it ourselves in the renderer instead.
 * 
 * 2) We implement a custom renderer (ZyFineGrainedGraphRenderer) that makes the more fine-grained
 * sloppy/non-sloppy decision. This renderer needs to be provided a ZyGraph2DView to obtain the
 * values for this decision.
 * 
 * We hence provide this additional functionality:
 * 
 * 1) Separate setting of sloppy thresholds for edges and nodes This means a zoom level can be set
 * for both sloppy edge drawing and sloppy node drawing, independently of each other. 2) Separate
 * setting of "hide threshold", e.g. a zoom threshold and minimum number of edges. If the number of
 * edges is exceeded and the zoom thresh- old too, edges are no longer drawn. This is logic that is
 * currently implemented in the Realizers.
 * 
 */

public class ZyGraph2DView extends Graph2DView implements IFineGrainedSloppyGraph2DView {
  private static final long serialVersionUID = 9194672642118308276L;

  /**
   * The zoom level at which we will paint sloppy nodes.
   */
  private double m_nodeSloppyThreshold;

  /**
   * The zoom level at which we will paint sloppy edges.
   */
  private double m_edgeSloppyThreshold;

  /**
   * What number of edges is required so that we hide edges on zoomout ?
   */
  private int m_minEdgesForSloppyEdgeHiding;

  /**
   * At what zoom level do we hide edges ?
   */
  private double m_sloppyEdgeHidingThreshold;

  public ZyGraph2DView() {
    initDefaults();
  }

  public ZyGraph2DView(final Graph2D g) {
    super(g);
    initDefaults();
  }

  private void initDefaults() {
    m_nodeSloppyThreshold = getPaintDetailThreshold();
    m_edgeSloppyThreshold = 0.2;
    // Auto-enable edge hiding if there's more than 3000 edges around
    m_minEdgesForSloppyEdgeHiding = 3000;
    m_sloppyEdgeHidingThreshold = 0.1;
    // Disable the "sloppy/not sloppy" decision completely in the original view.
    // We will perform this decision again, individually, when rendering
    setPaintDetailThreshold(0.0);
  }

  @Override
  public boolean drawEdges() {
    if (!(m_minEdgesForSloppyEdgeHiding < getGraph2D().E())) {
      return true; // Insufficient edges for hiding
    }
    if (!(m_sloppyEdgeHidingThreshold > getZoom())) {
      return true; // Not zoomed out enough for hiding
    }

    return false;
  }

  @Override
  public boolean isEdgeSloppyPaintMode() {
    return getZoom() < m_edgeSloppyThreshold;
  }

  @Override
  public boolean isNodeSloppyPaintMode() {
    return getZoom() < m_nodeSloppyThreshold;
  }

  public void setCrossCursor(final boolean cross) {
    if (cross) {
      super.setViewCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    } else {
      super.setViewCursor(Cursor.getDefaultCursor());
    }
  }

  @Override
  public void setEdgeSloppyThreshold(final double edgeSloppyThreshold) {
    m_edgeSloppyThreshold = edgeSloppyThreshold;
  }

  @Override
  public void setMinEdgesForSloppyEdgeHiding(final int minEdges) {
    m_minEdgesForSloppyEdgeHiding = minEdges;
  }

  @Override
  public void setNodeSloppyThreshold(final double nodeSloppyThreshold) {
    m_nodeSloppyThreshold = nodeSloppyThreshold;
  }

  @Override
  public void setSloppyEdgeHidingThreshold(final double sloppyEdgeHidingThreshold) {
    m_sloppyEdgeHidingThreshold = sloppyEdgeHidingThreshold;
  }

  /**
   * This method takes care of case 4201. We try to not show a cross arrow pointer while hovering
   * over selected nodes.
   */
  @Override
  public void setViewCursor(final Cursor cursor) {
    // this function is overwritten to suppress mouse cursor changings by yFiles View.
  }
}
