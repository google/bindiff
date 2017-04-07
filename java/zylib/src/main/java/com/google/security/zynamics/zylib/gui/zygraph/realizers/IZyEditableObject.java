// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

public interface IZyEditableObject {
  int getEnd();

  int getLength();

  Object getPersistentModel();

  int getStart();

  boolean isCommentDelimiter();

  boolean isPlaceholder();

  boolean update(String newContent);

  boolean updateComment(String newContent, ECommentPlacement placement);
}
