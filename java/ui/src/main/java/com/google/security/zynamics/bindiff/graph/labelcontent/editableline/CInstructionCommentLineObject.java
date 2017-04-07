package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class CInstructionCommentLineObject extends AbstractEditableLineObject {
  private final RawInstruction rawInstruction;

  private final ECommentPlacement commentPlacement;

  public CInstructionCommentLineObject(
      final RawInstruction rawInstruction,
      final ECommentPlacement commentPlacement,
      final int start,
      final int length) {
    super(start, length);
    this.rawInstruction = Preconditions.checkNotNull(rawInstruction);
    this.commentPlacement = Preconditions.checkNotNull(commentPlacement);
  }

  @Override
  public Object getPersistentModel() {
    return rawInstruction;
  }

  @Override
  public boolean update(final String newContent) {
    Preconditions.checkNotNull(newContent);

    rawInstruction.setComment(newContent, commentPlacement);
    return true;
  }

  @Override
  public boolean updateComment(final String newContent, final ECommentPlacement placement) {
    return update(newContent);
  }
}
