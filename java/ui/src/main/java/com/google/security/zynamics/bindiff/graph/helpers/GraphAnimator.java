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

package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.LayoutFunctions;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.util.DefaultMutableValue2D;
import y.util.Value2D;
import y.view.Graph2DView;
import y.view.ViewAnimationFactory;

public class GraphAnimator {
  public static void moveGraph(final BinDiffGraph<?, ?> graph, final Point2D.Double center) {
    final GraphSettings settings = graph.getSettings();
    final boolean animate = settings.getLayoutSettings().getAnimateLayout();

    if (settings.isAsync() || !(graph instanceof SingleGraph)) {
      graph.getView().focusView(graph.getView().getZoom(), center, animate);
    } else {
      final SingleGraph singleGraph = (SingleGraph) graph;

      final Graph2DView primaryView = singleGraph.getPrimaryGraph().getView();
      final Graph2DView secondaryView = singleGraph.getSecondaryGraph().getView();

      final AnimationPlayer animationPlayer = new AnimationPlayer();
      animationPlayer.setSynchronized(false);
      animationPlayer.addAnimationListener(primaryView);
      animationPlayer.addAnimationListener(secondaryView);

      final int factor1 = LayoutFunctions.PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS;
      final int factor2 = settings.getDisplaySettings().getAnimationSpeed();

      final CompositeAnimationObject compositeLayoutAnimationObject =
          AnimationFactory.createConcurrency();

      final Value2D centerCast = DefaultMutableValue2D.createView(center);
      final AnimationObject primaryViewPortAnimation =
          new ViewAnimationFactory(primaryView)
              .focusView(primaryView.getZoom(), centerCast, factor1 * factor2);
      final AnimationObject secondaryViewPortAnimation =
          new ViewAnimationFactory(secondaryView)
              .focusView(secondaryView.getZoom(), centerCast, factor1 * factor2);

      compositeLayoutAnimationObject.addAnimation(primaryViewPortAnimation);
      compositeLayoutAnimationObject.addAnimation(secondaryViewPortAnimation);

      animationPlayer.animate(compositeLayoutAnimationObject);
    }
  }

  public static void zoomGraph(
      final BinDiffGraph<?, ?> graph, final Point2D.Double center, final double zoom) {
    final GraphSettings settings = graph.getSettings();
    final boolean animate = settings.getLayoutSettings().getAnimateLayout();

    if (settings.isAsync() || !(graph instanceof SingleGraph)) {
      graph.getView().focusView(zoom, center, animate);
    } else {
      final SingleGraph singleGraph = (SingleGraph) graph;

      final Graph2DView primaryView = singleGraph.getPrimaryGraph().getView();
      final Graph2DView secondaryView = singleGraph.getSecondaryGraph().getView();

      final AnimationPlayer animationPlayer = new AnimationPlayer();
      animationPlayer.setSynchronized(false);
      animationPlayer.addAnimationListener(primaryView);
      animationPlayer.addAnimationListener(secondaryView);

      final int factor1 = LayoutFunctions.PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS;
      final int factor2 = settings.getDisplaySettings().getAnimationSpeed();

      final CompositeAnimationObject compositeLayoutAnimationObject =
          AnimationFactory.createConcurrency();

      final Value2D centerCast = DefaultMutableValue2D.createView(center);
      final AnimationObject primaryViewPortAnimation =
          new ViewAnimationFactory(primaryView).focusView(zoom, centerCast, factor1 * factor2);
      final AnimationObject secondaryViewPortAnimation =
          new ViewAnimationFactory(secondaryView).focusView(zoom, centerCast, factor1 * factor2);

      compositeLayoutAnimationObject.addAnimation(primaryViewPortAnimation);
      compositeLayoutAnimationObject.addAnimation(secondaryViewPortAnimation);

      animationPlayer.animate(compositeLayoutAnimationObject);
    }
  }

  public static void zoomGraph(
      final SuperGraph superGraph,
      final Rectangle2D area,
      final double primaryZoom,
      final double secondaryZoom) {
    final Graph2DView primaryView = superGraph.getPrimaryGraph().getView();
    final Graph2DView secondaryView = superGraph.getSecondaryGraph().getView();

    final DefaultMutableValue2D areaCenter =
        DefaultMutableValue2D.create(area.getCenterX(), area.getCenterY());

    final int factor =
        LayoutFunctions.PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS
            * superGraph.getSettings().getDisplaySettings().getAnimationSpeed();

    final AnimationObject primaryViewPortAnimation =
        new ViewAnimationFactory(primaryView)
            .focusView(Math.max(primaryZoom, secondaryZoom), areaCenter, factor);
    final AnimationObject secondaryViewPortAnimation =
        new ViewAnimationFactory(secondaryView)
            .focusView(Math.max(primaryZoom, secondaryZoom), areaCenter, factor);

    final CompositeAnimationObject compositeLayoutAnimationObject =
        AnimationFactory.createConcurrency();
    compositeLayoutAnimationObject.addAnimation(primaryViewPortAnimation);
    compositeLayoutAnimationObject.addAnimation(secondaryViewPortAnimation);

    final AnimationPlayer animationPlayer = new AnimationPlayer();
    animationPlayer.setSynchronized(false);
    animationPlayer.addAnimationListener(primaryView);
    animationPlayer.addAnimationListener(secondaryView);
    animationPlayer.animate(compositeLayoutAnimationObject);
  }
}
