package com.google.security.zynamics.bindiff.graph.realizers;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

public class FunctionNodeRealizerUpdater implements IRealizerUpdater<ZyGraphNode<?>> {
  @Override
  public void dispose() {}

  @Override
  public void generateContent(final IZyNodeRealizer realizer, ZyLabelContent content) {
    content = realizer.getNodeContent();
  }

  @Override
  public void setRealizer(final IZyNodeRealizer realizer) {}
}
