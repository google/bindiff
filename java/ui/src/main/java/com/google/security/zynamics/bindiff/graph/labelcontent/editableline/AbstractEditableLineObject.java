// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
