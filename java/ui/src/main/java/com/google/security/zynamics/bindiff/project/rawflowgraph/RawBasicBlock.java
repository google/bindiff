package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.common.base.Preconditions;
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

    this.functionAddr = Preconditions.checkNotNull(functionAddr);
    this.functionName = Preconditions.checkNotNull(functionName);
    this.instructions = Preconditions.checkNotNull(instructions);
    this.side = Preconditions.checkNotNull(side);
    this.matchState = Preconditions.checkNotNull(matchState);
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
