package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class InstructionObject extends AbstractEditableLineObject {
  private final RawInstruction rawInstruction;

  public InstructionObject(final RawInstruction rawInstruction, final int start, final int length) {
    super(start, length);
    Preconditions.checkNotNull(rawInstruction);

    this.rawInstruction = rawInstruction;
  }

  @Override
  public RawInstruction getPersistentModel() {
    return rawInstruction;
  }

  @Override
  public boolean update(final String newContent) {
    return false;
  }

  @Override
  public boolean updateComment(final String newContent, final ECommentPlacement placement) {
    Preconditions.checkNotNull(newContent);

    rawInstruction.setComment(newContent, placement);
    return true;
  }
}
