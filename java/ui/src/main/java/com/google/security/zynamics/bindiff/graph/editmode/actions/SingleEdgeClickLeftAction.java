package com.google.security.zynamics.bindiff.graph.editmode.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.helpers.GraphMover;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeClickedLeftState;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class SingleEdgeClickLeftAction implements IStateAction<CEdgeClickedLeftState> {
  @Override
  public void execute(final CEdgeClickedLeftState state, final MouseEvent event) {
    Preconditions.checkNotNull(event);
    final AbstractZyGraph<?, ?> graph = Preconditions.checkNotNull(state.getGraph());
    final ZyGraphEdge<?, ?, ?> edge = Preconditions.checkNotNull(state.getEdge());

    if (SwingUtilities.isLeftMouseButton(event)) {
      if (event.isShiftDown()) {
        // Shift-Click => Select edge
        graph.getGraph().setSelected(edge.getEdge(), !edge.isSelected());
      } else if (edge.getSource() != edge.getTarget()) {
        // When the user clicks on an edge, the graph scrolls to either
        // the source node or the target node of the edge, depending on
        // their distance from the visible part of the graph.

        final double x = graph.getView().toWorldCoordX(event.getX());
        final double y = graph.getView().toWorldCoordY(event.getY());

        GraphMover.moveToEdgeNode((BinDiffGraph<?, ?>) graph, edge.getEdge(), x, y);
      }
    }
  }
}