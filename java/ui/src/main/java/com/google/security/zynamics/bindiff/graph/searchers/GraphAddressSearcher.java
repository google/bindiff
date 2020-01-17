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

package com.google.security.zynamics.bindiff.graph.searchers;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class GraphAddressSearcher {
  public static CombinedDiffNode searchAddress(
      final CombinedGraph graph, final ESide side, final IAddress addr) {
    for (final CombinedDiffNode node : graph.getNodes()) {
      final CombinedViewNode rawNode = node.getRawNode();

      if (rawNode instanceof RawCombinedBasicBlock) {
        final RawBasicBlock basicblock = ((RawCombinedBasicBlock) rawNode).getRawNode(side);

        if (basicblock == null) {
          continue;
        }

        if (basicblock.getInstruction(addr) != null) {
          return node;
        }
      } else {
        final RawFunction function = ((RawCombinedFunction) rawNode).getRawNode(side);

        if (function == null) {
          continue;
        }

        if (function.getAddress().equals(addr)) {
          return node;
        }
      }
    }

    return null;
  }

  public static SingleDiffNode searchAddress(final SingleGraph graph, final IAddress addr) {
    for (final SingleDiffNode node : graph.getNodes()) {
      final SingleViewNode rawNode = node.getRawNode();

      if (rawNode instanceof RawBasicBlock) {
        if (((RawBasicBlock) rawNode).getInstruction(addr) != null) {
          return node;
        }
      } else if (node.getRawNode().getAddress().equals(addr)) {
        return node;
      }
    }

    return null;
  }
}
