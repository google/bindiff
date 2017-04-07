package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;

/**
 * Base class for all editable line objects.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public abstract class AbstractEditableLineObject implements IZyEditableObject {
  protected final int start;
  protected final int length;

  protected AbstractEditableLineObject(int start, int length) {
    this.start = start;
    this.length = length;
  }

  @Override
  public int getEnd() {
    return start + length;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getStart() {
    return start;
  }

  /**
   * Returns {@code true} if the editable object is a comment delimiter. As implemented in this
   * class, always returns {@code false}.
   */
  @Override
  public boolean isCommentDelimiter() {
    return false;
  }

  /**
   * Returns {@code true} if the editable object is a place holder. As implemented in this class,
   * always returns {@code false}.
   */
  @Override
  public boolean isPlaceholder() {
    return false;
  }
}
