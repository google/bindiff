// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.project.rawcallgraph;

import static com.google.common.base.Preconditions.checkNotNull;

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

    this.side = checkNotNull(side);
    this.addressToFunction = Maps.uniqueIndex(nodes, input -> input.getAddress());
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
