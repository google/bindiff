package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ZoomToNodeAction extends AbstractAction {
  private final BinDiffGraph<?, ?> graph;
  private final ZyGraphNode<?> node;

  public ZoomToNodeAction(final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> node) {
    super(getTitel(node));
    this.graph = Preconditions.checkNotNull(graph);
    this.node = Preconditions.checkNotNull(node);
  }

  private static String getTitel(final ZyGraphNode<?> node) {
    if (node != null) {
      if (node.getRawNode() instanceof RawBasicBlock
          || node.getRawNode() instanceof RawCombinedBasicBlock) {
        return "Zoom to Basicblock";
      }
      if (node.getRawNode() instanceof RawFunction
          || node.getRawNode() instanceof RawCombinedFunction) {
        return "Zoom to Function";
      }
    }

    return "Zoom to Node";
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    GraphZoomer.zoomToNode(graph, node);
  }
}
