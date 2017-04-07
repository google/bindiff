package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CUnlimitedProgressDialog;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.Window;

import javax.swing.SwingUtilities;

public class GraphLayoutUpdater implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>
      referenceGraph;

  private final boolean showProgress;

  public GraphLayoutUpdater(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> referenceGraph,
      final boolean showProgress) {
    this.referenceGraph = Preconditions.checkNotNull(referenceGraph);
    this.showProgress = showProgress;
  }

  private static Window getParentWindow(final BinDiffGraph<?, ?> graph) {
    // This function ensures that the progressbar dialog has a valid parent to center.
    if (graph.getSettings().getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      return BinDiffGraph.getParentWindow(graph.getPrimaryGraph());
    }

    return BinDiffGraph.getParentWindow(graph.getCombinedGraph());
  }

  public static void executeStatic(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph,
      final boolean showProgress)
      throws GraphLayoutException {
    final GraphLayoutCalculator layoutCalculator = new GraphLayoutCalculator(graph);

    final Window window = getParentWindow(graph.getPrimaryGraph());

    try {
      if (showProgress) {
        final CUnlimitedProgressDialog dlg =
            new CUnlimitedProgressDialog(
                window,
                Constants.DEFAULT_WINDOW_TITLE,
                "Calculating the new graph layout...",
                layoutCalculator);

        dlg.setVisible(true);

        if (dlg.wasCanceled()) {
          return;
        }

        if (dlg.getException() != null) {
          throw dlg.getException();
        }
      } else {
        layoutCalculator.execute();
      }
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphLayoutException(e, "Couldn't calculate graph layout.");
    }

    if (showProgress) {
      SwingUtilities.invokeLater(new InternalGraphViewUpdater(layoutCalculator, window));
    } else {
      final GraphViewUpdater viewUpdater = new GraphViewUpdater(layoutCalculator);
      viewUpdater.execute();
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(referenceGraph, showProgress);
  }

  private static final class InternalGraphViewUpdater implements Runnable {
    private final GraphLayoutCalculator layoutCalculator;

    private final Window window;

    public InternalGraphViewUpdater(
        final GraphLayoutCalculator layoutCalculator, final Window window) {
      this.layoutCalculator = layoutCalculator;
      this.window = window;
    }

    @Override
    public void run() {
      try {
        final GraphViewUpdater viewUpdater = new GraphViewUpdater(layoutCalculator);
        viewUpdater.execute();
      } catch (final GraphLayoutException e) {
        CMessageBox.showWarning(window, "Couldn't update graph layout.");
        Logger.logException(e, "Couldn't update graph layout.");
      }
    }
  }
}
