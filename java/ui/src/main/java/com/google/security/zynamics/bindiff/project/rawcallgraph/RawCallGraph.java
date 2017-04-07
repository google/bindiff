package com.google.security.zynamics.bindiff.project.rawcallgraph;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import java.util.List;

public class RawCallGraph extends MutableDirectedGraph<RawFunction, RawCall> {
  private final ImmutableMap<IAddress, RawFunction> addressToFunction;

  private final ESide side;

  public RawCallGraph(final List<RawFunction> nodes, final List<RawCall> edges, final ESide side) {
    super(nodes, edges);

    this.side = Preconditions.checkNotNull(side);
    this.addressToFunction =
        Maps.uniqueIndex(
            nodes,
            new Function<RawFunction, IAddress>() {
              @Override
              public IAddress apply(final RawFunction input) {
                return input.getAddress();
              }
            });
  }

  public RawFunction getFunction(final IAddress addr) {
    return addressToFunction.get(addr);
  }

  public ESide getSide() {
    return side;
  }

  /** Resets the visibility of all nodes to their default state (true) */
  public void resetVisibilityAndSelection() {
    for (final RawCall call : getEdges()) {
      call.removeAllListeners();

      call.setVisible(true);
      call.setSelected(false);
    }

    for (final RawFunction function : getNodes()) {
      function.removeAllListeners();

      function.setVisible(true);
      function.setSelected(false);
    }
  }
}
