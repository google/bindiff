package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class BasicBlockLineObject extends AbstractEditableLineObject {
  private final RawBasicBlock rawBasicblock;

  public BasicBlockLineObject(final RawBasicBlock rawBasicblock) {
    super(-1, 0);
    Preconditions.checkNotNull(rawBasicblock);

    this.rawBasicblock = rawBasicblock;
  }

  @Override
  public int getLength() {
    return -1;
  }

  @Override
  public final Object getPersistentModel() {
    return rawBasicblock;
  }

  public RawBasicBlock getRawBasicblock() {
    return rawBasicblock;
  }

  @Override
  public boolean update(final String newContent) {
    Preconditions.checkNotNull(newContent);

    rawBasicblock.setComment(newContent);
    return true;
  }

  @Override
  public boolean updateComment(final String newContent, final ECommentPlacement placement) {
    return false;
  }
}
