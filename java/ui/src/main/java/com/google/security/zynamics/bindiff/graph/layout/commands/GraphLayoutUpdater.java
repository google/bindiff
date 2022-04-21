// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CUnlimitedProgressDialog;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Window;
import javax.swing.SwingUtilities;

public class GraphLayoutUpdater implements ICommand {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>
      referenceGraph;

  private final boolean showProgress;

  public GraphLayoutUpdater(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> referenceGraph,
      final boolean showProgress) {
    this.referenceGraph = checkNotNull(referenceGraph);
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
        logger.atSevere().withCause(e).log("Couldn't update graph layout");
      }
    }
  }
}
