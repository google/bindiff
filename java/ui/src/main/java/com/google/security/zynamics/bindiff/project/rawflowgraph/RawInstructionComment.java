package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class RawInstructionComment {
  private final ECommentPlacement commentPlacement;
  private String text;
  private boolean modified = false;

  public RawInstructionComment(final String text, final ECommentPlacement commentPlacement) {
    this.text = Preconditions.checkNotNull(text);
    this.commentPlacement = commentPlacement;
  }

  public String getText() {
    return text;
  }

  public boolean isModified() {
    return modified;
  }

  public ECommentPlacement getPlacement() {
    return commentPlacement;
  }

  public void setText(final String text) {
    this.text = text;
    modified = true;
  }
}
