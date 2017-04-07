package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class BasicBlockHeadLineObject extends AbstractEditableLineObject {
  private final RawBasicBlock rawBasicblock;

  public BasicBlockHeadLineObject(final RawBasicBlock rawBasicblock) {
    super(0, 0);
    Preconditions.checkNotNull(rawBasicblock);

    this.rawBasicblock = rawBasicblock;
  }

  @Override
  public final Object getPersistentModel() {
    return rawBasicblock;
  }

  @Override
  public boolean update(final String newContent) {
    return false;
  }

  @Override
  public boolean updateComment(final String newContent, final ECommentPlacement placement) {
    return false;
  }
}
