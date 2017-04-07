package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class BasicBlockCommentLineObject extends AbstractEditableLineObject {
  private final RawBasicBlock rawBasicblock;

  public BasicBlockCommentLineObject(
      final RawBasicBlock rawBasicblock, final int start, final int length) {
    super(start, length);
    Preconditions.checkNotNull(rawBasicblock);

    this.rawBasicblock = rawBasicblock;
  }

  @Override
  public final Object getPersistentModel() {
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
