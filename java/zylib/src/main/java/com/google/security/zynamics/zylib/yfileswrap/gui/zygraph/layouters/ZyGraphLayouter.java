// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.layouters;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.CircularStyle;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicStyle;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalStyle;
import y.layout.CanonicMultiStageLayouter;
import y.layout.OrientationLayouter;
import y.layout.SelfLoopLayouter;
import y.layout.circular.CircularLayouter;
import y.layout.circular.SingleCycleLayouter;
import y.layout.hierarchic.AlignmentDrawer;
import y.layout.hierarchic.HierarchicLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.tree.BalloonLayouter;
import y.view.Graph2D;

/**
 * Creates graph layouter objects that are used to layout ZyGraphs.
 */
public class ZyGraphLayouter {
  public static void alignNodesToTopLayer(final Graph2D graph,
      final CanonicMultiStageLayouter multiStageLayouter) {
    if (multiStageLayouter instanceof HierarchicLayouter) {
      final HierarchicLayouter layouter = (HierarchicLayouter) multiStageLayouter;
      layouter.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_OPTIMAL);

      final OrientationLayouter ol = (OrientationLayouter) layouter.getOrientationLayouter();
      layouter.setDrawer(new AlignmentDrawer(layouter.getDrawer()));
      graph.addDataProvider(AlignmentDrawer.NODE_ALIGNMENT_POINT_DPKEY,
          ol.getOrientation() == OrientationLayouter.TOP_TO_BOTTOM
              ? new AlignmentDrawer.TopAlignmentDataProvider()
              : new AlignmentDrawer.LeftAlignmentDataProvider());
    } else if (multiStageLayouter instanceof IncrementalHierarchicLayouter) {
      final IncrementalHierarchicLayouter layouter =
          (IncrementalHierarchicLayouter) multiStageLayouter;
      layouter
          .setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_OPTIMAL);
      // Sets the alignment of the node within its layer (0 means top-aligned with respect to the
      // drawing direction).
      layouter.getNodeLayoutDescriptor().setLayerAlignment(0);
    }
  }

  public static CanonicMultiStageLayouter createCircularLayouter(final CircularStyle style,
      final long minNodeDist) {
    Preconditions.checkNotNull(style, "Internal Error: Layout style can't be null");
    Preconditions.checkArgument(minNodeDist >= 0,
        "Internal Error: Minimum node distance can't be negative");

    final CircularLayouter layouter = new CircularLayouter();

    switch (style) {
      case COMPACT:
        layouter.setLayoutStyle(CircularLayouter.BCC_COMPACT);
        break;
      case ISOLATED:
        layouter.setLayoutStyle(CircularLayouter.BCC_ISOLATED);
        break;
      case SINGLE_CIRCLE:
        layouter.setLayoutStyle(CircularLayouter.SINGLE_CYCLE);
        break;
      default:
        throw new IllegalStateException("Internal Error: Unknown layout style");
    }

    layouter.setPartitionLayoutStyle(CircularLayouter.PARTITION_LAYOUTSTYLE_CYCLIC);

    final SingleCycleLayouter cycleLayouter = layouter.getSingleCycleLayouter();
    cycleLayouter.setAutomaticRadius(true);

    cycleLayouter.setMinimalNodeDistance((int) minNodeDist);
    final BalloonLayouter bl = layouter.getBalloonLayouter();
    bl.setMinimalEdgeLength((int) minNodeDist);

    bl.setAllowOverlaps(false);

    return layouter;
  }

  public static CanonicMultiStageLayouter createHierarchicalLayouter(final HierarchicStyle style,
      final long minLayerDist, final long minNodeDist, final long minEdgeDist,
      final long minNodeEdgeDist, final HierarchicOrientation orientation) {
    Preconditions.checkNotNull(style, "Internal Error: Layout style can't be null");
    Preconditions.checkArgument(minLayerDist >= 0,
        "Internal Error: Minimum layer distance can't be negative");
    Preconditions.checkArgument(minNodeDist >= 0,
        "Internal Error: Minimum node distance can't be negative");
    Preconditions.checkArgument(minEdgeDist >= 0,
        "Internal Error: Minimum edge distance can't be negative");

    final IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    layouter.setConsiderNodeLabelsEnabled(true);
    layouter.setRecursiveGroupLayeringEnabled(true);
    layouter.setLayoutOrientation(orientation == HierarchicOrientation.HORIZONTAL
        ? OrientationLayouter.TOP_TO_BOTTOM : OrientationLayouter.LEFT_TO_RIGHT);
    layouter.setBackloopRoutingEnabled(true);

    final EdgeLayoutDescriptor edgeLayout = layouter.getEdgeLayoutDescriptor();
    edgeLayout.setSourcePortOptimizationEnabled(true);
    edgeLayout.setTargetPortOptimizationEnabled(true);
    edgeLayout.setMinimumFirstSegmentLength(25);
    edgeLayout.setMinimumLastSegmentLength(25);

    final NodeLayoutDescriptor nodeLayout = layouter.getNodeLayoutDescriptor();
    nodeLayout.setLayerAlignment(0.0);

    final SelfLoopLayouter sll = new SelfLoopLayouter(layouter);
    sll.setSmartSelfloopPlacementEnabled(true);

    switch (style) {
      case OCTLINEAR_OPTIMAL: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_OPTIMAL,
            RoutingStyle.EDGE_STYLE_OCTILINEAR, true, layouter, edgeLayout);
        break;
      }
      case ORTHOGONAL_OPTIMAL: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_OPTIMAL,
            RoutingStyle.EDGE_STYLE_ORTHOGONAL, true, layouter, edgeLayout);
        break;
      }
      case POLYLINE_OPTIMAL: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_OPTIMAL,
            RoutingStyle.EDGE_STYLE_POLYLINE, false, layouter, edgeLayout);
        break;
      }
      case OCTLINEAR_TOPMOST: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TOPMOST,
            RoutingStyle.EDGE_STYLE_OCTILINEAR, true, layouter, edgeLayout);
        break;
      }
      case ORTHOGONAL_TOPMOST: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TOPMOST,
            RoutingStyle.EDGE_STYLE_ORTHOGONAL, true, layouter, edgeLayout);
        break;
      }
      case POLYLINE_TOPMOST: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TOPMOST,
            RoutingStyle.EDGE_STYLE_POLYLINE, true, layouter, edgeLayout);
        break;
      }
      case OCTLINEAR_TIGHT_TREE: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TIGHT_TREE,
            RoutingStyle.EDGE_STYLE_OCTILINEAR, true, layouter, edgeLayout);
        break;
      }
      case ORTHOGONAL_TIGHT_TREE: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TIGHT_TREE,
            RoutingStyle.EDGE_STYLE_ORTHOGONAL, true, layouter, edgeLayout);
        break;
      }
      case POLYLINE_TIGHT_TREE: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TIGHT_TREE,
            RoutingStyle.EDGE_STYLE_POLYLINE, true, layouter, edgeLayout);
        break;
      }
      case OCTLINEAR_BFS: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_BFS,
            RoutingStyle.EDGE_STYLE_OCTILINEAR, true, layouter, edgeLayout);
        break;
      }
      case ORTHOGONAL_BFS: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_BFS,
            RoutingStyle.EDGE_STYLE_ORTHOGONAL, true, layouter, edgeLayout);
        break;
      }
      case POLYLINE_BFS: {
        setStyle(IncrementalHierarchicLayouter.LAYERING_STRATEGY_BFS,
            RoutingStyle.EDGE_STYLE_POLYLINE, false, layouter, edgeLayout);
        break;
      }
      default:
        throw new IllegalStateException("Internal Error: Unknown layout style");
    }

    layouter.setMinimumLayerDistance(minLayerDist);
    layouter.setNodeToNodeDistance(minNodeDist);
    layouter.setNodeToEdgeDistance(minEdgeDist);
    layouter.setEdgeToEdgeDistance(minNodeEdgeDist);

    return layouter;
  }

  private static void setStyle(final byte strategy, final byte routingStyle,
      final boolean routeOrthogonally, final IncrementalHierarchicLayouter layouter,
      final EdgeLayoutDescriptor edgeLayout) {

    layouter.setFromScratchLayeringStrategy(strategy);
    layouter.setOrthogonallyRouted(routeOrthogonally);
    edgeLayout.setRoutingStyle(new RoutingStyle(routingStyle));
  }

  public static CanonicMultiStageLayouter createIncrementalHierarchicalLayouter(
      final boolean orthogonalEdgeRooting, final long minLayerDist, final long minNodeDist,
      final HierarchicOrientation orientation) {
    final IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();

    if (HierarchicOrientation.HORIZONTAL.equals(orientation)) {
      // OrientationLayouter.TOP_TO_BOTTOM is the default
      ((OrientationLayouter) layouter.getOrientationLayouter())
          .setOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    }

    layouter.setMinimumLayerDistance(minLayerDist);
    layouter.setNodeToNodeDistance(minNodeDist);
    layouter.setEdgeToEdgeDistance(25);
    layouter.setNodeToEdgeDistance(25);

    layouter.setBackloopRoutingEnabled(true);
    layouter.setSelfLoopLayouterEnabled(true);
    layouter.setOrthogonallyRouted(orthogonalEdgeRooting);

    return layouter;
  }

  public static CanonicMultiStageLayouter createOrthoLayouter(final OrthogonalStyle style,
      final long gridSize, final boolean isVerticalOrientation) {
    Preconditions.checkArgument(gridSize > 0, "Internal Error: Grid size can not be 0 or lower.");
    Preconditions.checkNotNull(style, "Internal Error: Layout style can't be null");

    final OrthogonalLayouter layouter = new OrthogonalLayouter();
    if (OrthogonalStyle.TREE.equals(style)) {
      layouter.setTreeStyle(OrthogonalLayouter.TREE_STYLE_ASPECT_RATIO_TREE);
    }
    if (!isVerticalOrientation) {
      // OrientationLayouter.TOP_TO_BOTTOM is the default
      ((OrientationLayouter) layouter.getOrientationLayouter())
          .setOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    }
    layouter.setGrid((int) gridSize);

    return layouter;
  }
}
