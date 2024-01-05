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

package com.google.security.zynamics.bindiff.project.rawflowgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.project.matches.AddressPair;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RawFlowGraph extends MutableDirectedGraph<RawBasicBlock, RawJump> {
  private final IAddress address;
  private final String name;
  private final EFunctionType functionType;
  private final ESide side;

  private final Map<IAddress, RawBasicBlock> addrToBasicblockMap = new HashMap<>();
  private final Map<AddressPair, RawJump> addrPairToJumpMap = new HashMap<>();

  public RawFlowGraph(
      final IAddress addr,
      final String name,
      final EFunctionType functionType,
      final List<RawBasicBlock> basicblocks,
      final List<RawJump> jumps,
      final ESide side) {
    super(basicblocks, jumps);

    this.address = checkNotNull(addr);
    this.name = checkNotNull(name);
    this.functionType = checkNotNull(functionType);
    this.side = checkNotNull(side);

    for (final RawBasicBlock basicblock : basicblocks) {
      addrToBasicblockMap.put(basicblock.getAddress(), basicblock);
    }

    for (final RawJump jump : jumps) {
      final long srcAddr = jump.getSource().getAddress().toLong();
      final long tarAddr = jump.getTarget().getAddress().toLong();

      addrPairToJumpMap.put(new AddressPair(srcAddr, tarAddr), jump);
    }
  }

  private void removeFromMap(final SingleViewEdge<? extends SingleViewNode> edge) {
    final IAddress srcAddr = edge.getSource().getAddress();
    final IAddress tarAddr = edge.getTarget().getAddress();
    final AddressPair addrPair = new AddressPair(srcAddr.toLong(), tarAddr.toLong());

    addrPairToJumpMap.remove(addrPair);
  }

  @Override
  public void addEdge(final RawJump edge) {
    final AddressPair addressPair =
        new AddressPair(
            edge.getSource().getAddress().toLong(), edge.getTarget().getAddress().toLong());
    addrPairToJumpMap.put(addressPair, edge);

    super.addEdge(edge);
  }

  @Override
  public void addNode(final RawBasicBlock node) {
    addrToBasicblockMap.put(node.getAddress(), node);

    super.addNode(node);
  }

  public IAddress getAddress() {
    return address;
  }

  public RawBasicBlock getBasicBlock(final IAddress basicBlockAddr) {
    return addrToBasicblockMap.get(basicBlockAddr);
  }

  public EFunctionType getFunctionType() {
    return functionType;
  }

  public RawJump getJump(final IAddress sourceJumpAddr, final IAddress targetJumpAddr) {
    if (sourceJumpAddr == null || targetJumpAddr == null) {
      return null;
    }

    return addrPairToJumpMap.get(new AddressPair(sourceJumpAddr.toLong(), targetJumpAddr.toLong()));
  }

  public String getName() {
    return name;
  }

  public ESide getSide() {
    return side;
  }

  @Override
  public void removeNode(final RawBasicBlock node) {
    // removes entry from address to raw basicblock map
    addrToBasicblockMap.remove(node.getAddress());

    // removes node from nodes parent's children list
    // removes node's incoming edges from parent's outgoing edge list
    // removes entry from address pair to raw jump map
    for (final SingleViewEdge<? extends SingleViewNode> edge : node.getIncomingEdges()) {
      SingleViewNode.unlink(edge.getSource(), edge.getTarget());
      edge.getSource().removeOutgoingEdge(edge);
      edge.getTarget().removeIncomingEdge(edge);
      removeFromMap(edge);
    }

    // removes node from nodes child's parent list
    // removes node's outgoing edges from child's incoming edge list
    for (final SingleViewEdge<? extends SingleViewNode> edge : node.getOutgoingEdges()) {
      SingleViewNode.unlink(edge.getSource(), edge.getTarget());
      edge.getSource().removeOutgoingEdge(edge);
      edge.getTarget().removeIncomingEdge(edge);
      removeFromMap(edge);
    }

    // removes node from node list
    // removes all incoming and outgoing edges of this node from edge list
    // removes entry from node to edges map
    super.removeNode(node);
  }
}
