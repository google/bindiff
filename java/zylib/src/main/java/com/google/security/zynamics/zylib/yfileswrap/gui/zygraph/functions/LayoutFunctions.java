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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.settings.ILayoutSettings;
import y.layout.BufferedLayouter;
import y.layout.CanonicMultiStageLayouter;
import y.layout.GraphLayout;
import y.layout.IntersectionCalculator;
import y.layout.LabelLayoutTranslator;
import y.layout.LayoutOrientation;
import y.layout.LayoutTool;
import y.layout.PortCalculator;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.circular.CircularLayouter;
import y.util.DataProviders;
import y.view.DefaultGraph2DRenderer;
import y.view.Graph2D;
import y.view.LayoutMorpher;
import y.view.NodeRealizerIntersectionCalculator;

public class LayoutFunctions {
  public static final int PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS = 100;

  /** Layouts the graph using the last set layouter that was passed to setLayouter. */
  public static GraphLayout doLayout(
      final AbstractZyGraph<?, ?> graph, final CanonicMultiStageLayouter layouter) {
    Preconditions.checkNotNull(
        layouter,
        "Internal Error: Can not layout the graph without initializing the layouter first");

    GraphLayout graphLayout = null;

    final ILayoutSettings layoutSettings = graph.getSettings().getLayoutSettings();

    if (layoutSettings.getCurrentLayouter().getLayoutOrientation()
        == LayoutOrientation.TOP_TO_BOTTOM) {
      graph
          .getGraph()
          .addDataProvider(
              PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY,
              DataProviders.createConstantDataProvider(
                  PortConstraint.create(PortConstraint.SOUTH)));
      graph
          .getGraph()
          .addDataProvider(
              PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY,
              DataProviders.createConstantDataProvider(
                  PortConstraint.create(PortConstraint.NORTH)));
    }
    if (layoutSettings.getCurrentLayouter().getLayoutOrientation()
        == LayoutOrientation.LEFT_TO_RIGHT) {
      graph
          .getGraph()
          .addDataProvider(
              PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY,
              DataProviders.createConstantDataProvider(PortConstraint.create(PortConstraint.EAST)));
      graph
          .getGraph()
          .addDataProvider(
              PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY,
              DataProviders.createConstantDataProvider(PortConstraint.create(PortConstraint.WEST)));
    }

    layouter.setLabelLayouter(new LabelLayoutTranslator());
    layouter.setLabelLayouterEnabled(true);

    if ((graph.getNodes().size() < layoutSettings.getAnimateLayoutNodeThreshold())
        && (graph.getEdges().size() < layoutSettings.getAnimateLayoutEdgeThreshold())) {
      if (graph.getSettings().getLayoutSettings().getAnimateLayout()) {
        ((DefaultGraph2DRenderer) graph.getView().getGraph2DRenderer()).setDrawEdgesFirst(true);

        graphLayout = new BufferedLayouter(layouter).calcLayout(graph.getGraph());

        final LayoutMorpher layoutMorpher = new LayoutMorpher();
        layoutMorpher.setSmoothViewTransform(true);
        layoutMorpher.setPreferredDuration(
            PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS
                * graph.getSettings().getDisplaySettings().getAnimationSpeed());

        final GraphLayout morpherLayout = graphLayout;
        new SwingInvoker() {
          @Override
          protected void operation() {
            layoutMorpher.execute(graph.getView(), morpherLayout);
          }
        }.invokeLater();

        recalculatePorts(layouter, graph.getGraph());
      } else {
        graphLayout = new BufferedLayouter(layouter).calcLayout(graph.getGraph());
        LayoutTool.applyGraphLayout(graph.getGraph(), graphLayout);

        recalculatePorts(layouter, graph.getGraph());
      }
    } else {
      graphLayout = new BufferedLayouter(layouter).calcLayout(graph.getGraph());
      LayoutTool.applyGraphLayout(graph.getGraph(), graphLayout);

      final LayoutMorpher layoutMorpher = new LayoutMorpher();
      layoutMorpher.setPreferredDuration(
          PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS
              * graph.getSettings().getDisplaySettings().getAnimationSpeed());

      layoutMorpher.execute(graph.getView(), graphLayout);
    }

    return graphLayout;
  }

  public static void recalculatePorts(
      final CanonicMultiStageLayouter layouter, final Graph2D graph) {
    // Effect: Ensures that ports are drawn onto node borders, and not onto the node center. (Only
    // Circular layout!)
    // Justification: Circular layout uses as the standard port the center of the node, this will be
    // corrected by
    // calling the following function.
    // Exclusion: Port of nodes with non rectangle shapes, have to be additionally recalculated.

    if (layouter instanceof CircularLayouter) {
      // Port correction
      LayoutTool.clipEdgesOnBB(graph);

      // Recalculate ports (necessary for circular proximity nodes)
      final PortCalculator pc = new PortCalculator();
      final NodeRealizerIntersectionCalculator nrics =
          new NodeRealizerIntersectionCalculator(graph, true);
      graph.addDataProvider(IntersectionCalculator.SOURCE_INTERSECTION_CALCULATOR_DPKEY, nrics);
      final NodeRealizerIntersectionCalculator nrict =
          new NodeRealizerIntersectionCalculator(graph, false);
      graph.addDataProvider(IntersectionCalculator.TARGET_INTERSECTION_CALCULATOR_DPKEY, nrict);

      pc.doLayout(graph);
    }
  }
}
