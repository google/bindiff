// Copyright 2011-2024 Google LLC
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
