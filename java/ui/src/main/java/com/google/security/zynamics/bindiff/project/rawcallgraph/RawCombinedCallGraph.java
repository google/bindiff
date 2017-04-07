package com.google.security.zynamics.bindiff.project.rawcallgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawCombinedCallGraph
    extends MutableDirectedGraph<RawCombinedFunction, RawCombinedCall> {
  private final Map<Pair<IAddress, IAddress>, RawCombinedFunction> addrPairToCombinedFunction =
      new HashMap<>();

  public RawCombinedCallGraph(
      final List<RawCombinedFunction> nodes, final List<RawCombinedCall> edges) {
    super(nodes, edges);

    for (final RawCombinedFunction node : nodes) {
      addrPairToCombinedFunction.put(
          new Pair<>(node.getAddress(ESide.PRIMARY), node.getAddress(ESide.SECONDARY)), node);
    }
  }

  public RawCombinedFunction getCombinedFunction(
      final IAddress primaryAddr, final IAddress secondaryAddr) {
    return addrPairToCombinedFunction.get(new Pair<>(primaryAddr, secondaryAddr));
  }
}
