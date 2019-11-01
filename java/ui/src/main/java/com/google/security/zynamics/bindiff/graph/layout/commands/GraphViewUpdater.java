package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.helpers.GraphViewFitter;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.graph.layout.LayoutMorpher;
import com.google.security.zynamics.bindiff.graph.layout.SuperLayoutMorpher;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.LayoutFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import y.anim.AnimationFactory;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.layout.CanonicMultiStageLayouter;
import y.layout.GraphLayout;
import y.layout.LabelLayoutTranslator;
import y.layout.LayoutTool;
import y.view.Graph2D;
import y.view.Graph2DView;

public class GraphViewUpdater implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>
      referenceGraph;

  private final GraphLayoutCalculator layoutCalculator;

  public GraphViewUpdater(final GraphLayoutCalculator layoutCalculator) {
    Preconditions.checkNotNull(layoutCalculator);

    this.layoutCalculator = layoutCalculator;
    referenceGraph = layoutCalculator.getReferenceGraph();
  }

  public static void updateViews(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph) {
    graph.getPrimaryGraph().getGraph().updateViews();
    graph.getSecondaryGraph().getGraph().updateViews();
    graph.getCombinedGraph().getGraph().updateViews();
    graph.getSuperGraph().getGraph().updateViews();
  }

  private void applyGraphLayout() {
    final GraphSettings settings = referenceGraph.getSettings();

    if (settings.isSync()) {
      LayoutTool.applyGraphLayout(
          referenceGraph.getPrimaryGraph().getGraph(), layoutCalculator.getPrimaryGraphLayout());
      LayoutTool.applyGraphLayout(
          referenceGraph.getSecondaryGraph().getGraph(),
          layoutCalculator.getSecondaryGraphLayout());
      LayoutTool.applyGraphLayout(
          referenceGraph.getCombinedGraph().getGraph(), layoutCalculator.getCombinedGraphLayout());

      referenceGraph.getCombinedGraph().getView().fitContent();
    } else {
      LayoutTool.applyGraphLayout(referenceGraph.getGraph(), getReferenceGraphLayout());

      referenceGraph.getView().fitContent();
      referenceGraph
          .getView()
          .setZoom(referenceGraph.getView().getZoom() * GraphZoomer.ZOOM_OUT_FACTOR);
    }
  }

  private GraphLayout getReferenceGraphLayout() {
    if (referenceGraph instanceof SingleGraph) {
      final ESide side = ((SingleGraph) referenceGraph).getSide();

      return side == ESide.PRIMARY
          ? layoutCalculator.getPrimaryGraphLayout()
          : layoutCalculator.getSecondaryGraphLayout();
    }
    return layoutCalculator.getCombinedGraphLayout();
  }

  private void morphGraphLayout() throws GraphLayoutException {
    try {
      final GraphSettings settings = referenceGraph.getSettings();

      if (settings.isSync()) {
        if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
          LayoutTool.applyGraphLayout(
              referenceGraph.getCombinedGraph().getGraph(),
              layoutCalculator.getCombinedGraphLayout());
          referenceGraph.getCombinedGraph().getView().fitContent();
          final double zoom = referenceGraph.getCombinedGraph().getView().getZoom() * 0.95;
          referenceGraph.getCombinedGraph().getView().setZoom(zoom);

          morphTwoGraphLayouts();
        } else {
          LayoutTool.applyGraphLayout(
              referenceGraph.getPrimaryGraph().getGraph(),
              layoutCalculator.getPrimaryGraphLayout());
          LayoutTool.applyGraphLayout(
              referenceGraph.getSecondaryGraph().getGraph(),
              layoutCalculator.getSecondaryGraphLayout());

          morphOneGraphLayout(referenceGraph, layoutCalculator.getCombinedGraphLayout());
        }
      } else {
        morphOneGraphLayout(referenceGraph, getReferenceGraphLayout());
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphLayoutException(e, "Graph layout failed. Could not morph graph layout.");
    }
  }

  private void morphOneGraphLayout(final BinDiffGraph<?, ?> graph, final GraphLayout graphLayout) {
    final GraphSettings settings = graph.getSettings();
    final CanonicMultiStageLayouter layouter = settings.getLayoutSettings().getCurrentLayouter();

    final Graph2DView graphView = graph.getView();

    layouter.setLabelLayouter(new LabelLayoutTranslator());
    layouter.setLabelLayouterEnabled(true);

    final int factor1 = LayoutFunctions.PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS;
    final int factor2 = graph.getSettings().getDisplaySettings().getAnimationSpeed();

    final AnimationPlayer animationPlayer = new AnimationPlayer();
    animationPlayer.setSynchronized(true);
    animationPlayer.addAnimationListener(graphView);

    final LayoutMorpher morpher = new LayoutMorpher(graphView, graphLayout);

    morpher.setKeepZoomFactor(false);
    morpher.setEasedExecution(true);
    morpher.setPreferredDuration(factor1 * factor2);
    morpher.setSmoothViewTransform(true);

    final CompositeAnimationObject compositeLayoutAnimationObject =
        AnimationFactory.createConcurrency();
    compositeLayoutAnimationObject.addAnimation(morpher);

    animationPlayer.animate(compositeLayoutAnimationObject);
  }

  private void morphTwoGraphLayouts() {
    final Graph2D ySuperGraph = referenceGraph.getSuperGraph().getGraph();

    final GraphLayout primaryLayout = layoutCalculator.getPrimaryGraphLayout();
    final GraphLayout secondaryLayout = layoutCalculator.getSecondaryGraphLayout();

    final Graph2DView primaryView = referenceGraph.getPrimaryGraph().getView();
    final Graph2DView secondaryView = referenceGraph.getSecondaryGraph().getView();

    final AnimationPlayer animationPlayer = new AnimationPlayer();
    animationPlayer.setSynchronized(true);
    animationPlayer.addAnimationListener(primaryView);
    animationPlayer.addAnimationListener(secondaryView);

    final SuperLayoutMorpher primaryMorpher =
        new SuperLayoutMorpher(primaryView, primaryLayout, ySuperGraph);
    final SuperLayoutMorpher secondaryMorpher =
        new SuperLayoutMorpher(secondaryView, secondaryLayout, ySuperGraph);

    final int factor2 = referenceGraph.getSettings().getDisplaySettings().getAnimationSpeed();
    final int factor1 = LayoutFunctions.PREFERRED_ANIMATION_TIME_CONSTANT_FACTOR_MS;

    primaryMorpher.setSmoothViewTransform(true);
    primaryMorpher.setPreferredDuration(factor1 * factor2);
    primaryMorpher.setEasedExecution(true);

    secondaryMorpher.setSmoothViewTransform(true);
    secondaryMorpher.setPreferredDuration(factor1 * factor2);
    secondaryMorpher.setEasedExecution(true);

    // duration must be equal to the duration of the morpher animation objects, otherwise the view
    // port animation is not performed
    final CompositeAnimationObject compositeLayoutAnimationObject =
        AnimationFactory.createConcurrency();

    compositeLayoutAnimationObject.addAnimation(primaryMorpher);
    compositeLayoutAnimationObject.addAnimation(secondaryMorpher);

    animationPlayer.animate(compositeLayoutAnimationObject);
  }

  @Override
  public void execute() throws GraphLayoutException {
    try {
      final GraphSettings settings = referenceGraph.getSettings();

      GraphViewFitter.adoptSuperViewCanvasProperties(referenceGraph.getSuperGraph());
      GraphViewFitter.fitSingleViewToSuperViewContent(referenceGraph.getSuperGraph());

      if (settings.getLayoutSettings().getAnimateLayout()) {
        morphGraphLayout();
      } else {
        applyGraphLayout();
      }

      updateViews(referenceGraph);
    } catch (final GraphLayoutException e) {
      throw e;
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphLayoutException(e, "Could update graph view.");
    }
  }
}
