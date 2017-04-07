package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.graph.edges.CombinedViewEdge;

public class RawCombinedJump<NodeType extends RawCombinedBasicBlock>
    extends CombinedViewEdge<NodeType> {
  private final RawJump primaryJump;

  private final RawJump secondaryJump;

  public RawCombinedJump(
      final NodeType source,
      final NodeType target,
      final RawJump primaryJump,
      final RawJump secondaryJump) {
    super(source, target);

    if (primaryJump == null && secondaryJump == null) {
      throw new IllegalArgumentException("Primary and secondary jump cannot both be null.");
    }

    this.primaryJump = primaryJump;
    this.secondaryJump = secondaryJump;
  }

  @Override
  public EMatchState getMatchState() {
    if (primaryJump == null) {
      return EMatchState.SECONDRAY_UNMATCHED;
    } else if (secondaryJump == null) {
      return EMatchState.PRIMARY_UNMATCHED;
    }

    return EMatchState.MATCHED;
  }

  @Override
  public RawJump getPrimaryEdge() {
    return primaryJump;
  }

  @Override
  public RawJump getSecondaryEdge() {
    return secondaryJump;
  }

  @Override
  public NodeType getSource() {
    return super.getSource();
  }

  @Override
  public NodeType getTarget() {
    return super.getTarget();
  }
}
