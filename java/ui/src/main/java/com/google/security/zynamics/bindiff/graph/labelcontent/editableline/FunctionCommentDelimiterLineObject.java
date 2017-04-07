package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class FunctionCommentDelimiterLineObject extends AbstractEditableLineObject {
  private final RawFunction rawFunction;

  public FunctionCommentDelimiterLineObject(
      final RawFunction rawFunction, final int start, final int length) {
    super(start, length);
    Preconditions.checkNotNull(rawFunction);

    this.rawFunction = rawFunction;
  }

  @Override
  public Object getPersistentModel() {
    return rawFunction;
  }

  @Override
  public boolean isCommentDelimiter() {
    return true;
  }

  @Override
  public boolean update(final String newContent) {
    Preconditions.checkNotNull(newContent);

    rawFunction.setComment(newContent);
    return true;
  }

  @Override
  public boolean updateComment(final String newContent, final ECommentPlacement placement) {
    return false;
  }
}
