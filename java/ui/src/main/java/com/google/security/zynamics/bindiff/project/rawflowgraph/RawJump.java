package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.security.zynamics.bindiff.enums.EJumpType;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawJump extends SingleViewEdge<RawBasicBlock> {
  final EJumpType jumpType;

  public RawJump(final RawBasicBlock source, final RawBasicBlock target, final EJumpType jumpType) {
    super(source, target);

    this.jumpType = jumpType;
  }

  public EJumpType getJumpType() {
    return jumpType;
  }

  @Override
  public RawBasicBlock getSource() {
    return super.getSource();
  }

  public IAddress getSourceBasicblockAddress() {
    return getSource().getAddress();
  }

  @Override
  public RawBasicBlock getTarget() {
    return super.getTarget();
  }

  public IAddress getTargetBasicblockAddress() {
    return getTarget().getAddress();
  }
}
