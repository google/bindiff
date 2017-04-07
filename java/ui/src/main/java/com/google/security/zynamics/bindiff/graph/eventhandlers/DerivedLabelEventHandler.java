package com.google.security.zynamics.bindiff.graph.eventhandlers;

import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.zylib.gui.zygraph.CDefaultLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyRegenerateableRealizer;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;

public class DerivedLabelEventHandler extends CDefaultLabelEventHandler {
  public DerivedLabelEventHandler(final AbstractZyGraph<?, ?> graph) {
    super(graph);

    // TODO: If Ctrl+Tab is pressed go to other side else use default Tab behavior
    // addKeyBehaviour(KeyEvent.VK_TAB, null);
  }

  @Override
  public void activateLabelContent(
      final ZyLabelContent labelContent, final IZyRegenerateableRealizer activeRealizer) {
    final SingleGraph priGraph = ((BinDiffGraph<?, ?>) getGraph()).getPrimaryGraph();
    final SingleGraph secGraph = ((BinDiffGraph<?, ?>) getGraph()).getSecondaryGraph();
    final CombinedGraph combinedGraph = ((BinDiffGraph<?, ?>) getGraph()).getCombinedGraph();

    final ZyEditMode<?, ?> priEditMode = priGraph.getEditMode();
    final ZyEditMode<?, ?> secEditMode = secGraph.getEditMode();
    final ZyEditMode<?, ?> combinedEditMode = combinedGraph.getEditMode();

    final CDefaultLabelEventHandler primaryLabelEventHandler = priEditMode.getLabelEventHandler();
    final CDefaultLabelEventHandler secondaryLabelEventHandler = secEditMode.getLabelEventHandler();
    final CDefaultLabelEventHandler combinedLabelEventHandler =
        combinedEditMode.getLabelEventHandler();

    if (!primaryLabelEventHandler.isActiveLabel(labelContent)) {
      primaryLabelEventHandler.deactivateLabelContent();
    }

    if (!secondaryLabelEventHandler.isActiveLabel(labelContent)) {
      secondaryLabelEventHandler.deactivateLabelContent();
    }

    if (!combinedLabelEventHandler.isActiveLabel(labelContent)) {
      combinedLabelEventHandler.deactivateLabelContent();
    }

    super.activateLabelContent(labelContent, activeRealizer);

    priGraph.updateViews();
    secGraph.updateViews();

    ((BinDiffGraph<?, ?>) getGraph()).getCombinedGraph().updateViews();
  }
}
