package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EPlaceholderType;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class PlaceholderObject extends AbstractEditableLineObject {
  private final EPlaceholderType placeholderType;

  public PlaceholderObject(final EPlaceholderType placeholderType) {
    super(0, 0);
    this.placeholderType = Preconditions.checkNotNull(placeholderType);
  }

  @Override
  public Object getPersistentModel() {
    return null;
  }

  public EPlaceholderType getPlaceholderType() {
    return placeholderType;
  }

  @Override
  public boolean isPlaceholder() {
    return true;
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
