// Copyright 2011-2023 Google LLC
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

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

public class RawBasicBlock extends SingleViewNode implements Iterable<RawInstruction> {
  private final SortedMap<IAddress, RawInstruction> instructions;
  private final String functionName;
  private final IAddress functionAddr;
  private final ESide side;
  private final EMatchState matchState;
  private String comment = "";
  private boolean commentChanged = false;

  public RawBasicBlock(
      final IAddress functionAddr,
      final String functionName,
      final IAddress basicBlockAddress,
      final SortedMap<IAddress, RawInstruction> instructions,
      final ESide side,
      final EMatchState matchState) {
    super(basicBlockAddress, -1, 0, 0, Color.WHITE, Color.BLACK, false, true);

    this.functionAddr = checkNotNull(functionAddr);
    this.functionName = checkNotNull(functionName);
    this.instructions = checkNotNull(instructions);
    this.side = checkNotNull(side);
    this.matchState = checkNotNull(matchState);
    this.comment = "";
  }

  public RawBasicBlock clone(final EMatchState matchState) {

    final RawBasicBlock clone =
        new RawBasicBlock(
            getFunctionAddr(),
            getFunctionName(),
            getAddress(),
            getInstructions(),
            getSide(),
            matchState);

    clone.comment = getComment();

    return clone;
  }

  public String getComment() {
    return comment;
  }

  public IAddress getFunctionAddr() {
    return functionAddr;
  }

  public String getFunctionName() {
    return functionName;
  }

  public RawInstruction getInstruction(final IAddress addr) {
    return instructions.get(addr);
  }

  public SortedMap<IAddress, RawInstruction> getInstructions() {
    return instructions;
  }

  @Override
  public EMatchState getMatchState() {
    return matchState;
  }

  public int getMaxOperandLength() {
    int max = 0;

    for (final RawInstruction instruction : instructions.values()) {
      max = Math.max(max, instruction.getOperandLength());
    }

    return max;
  }

  @Override
  public List<SingleViewEdge<? extends SingleViewNode>> getOutgoingEdges() {
    return super.getOutgoingEdges();
  }

  @Override
  public ESide getSide() {
    return side;
  }

  public int getSizeOfInstructions() {
    return instructions.size();
  }

  public boolean isChangedComment() {
    return commentChanged;
  }

  @Override
  public Iterator<RawInstruction> iterator() {
    return instructions.values().iterator();
  }

  public void setComment(final String text) {
    comment = text;

    commentChanged = true;
  }
}
