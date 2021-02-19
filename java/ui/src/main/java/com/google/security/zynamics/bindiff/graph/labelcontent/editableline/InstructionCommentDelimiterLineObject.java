// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.labelcontent.editableline;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class InstructionCommentDelimiterLineObject extends AbstractEditableLineObject {
  private final RawInstruction rawInstruction;
  private final ECommentPlacement commentPlacement;

  public InstructionCommentDelimiterLineObject(
      final RawInstruction rawInstruction,
      final ECommentPlacement commentPlacement,
      final int start,
      final int length) {
    super(start, length);
    this.rawInstruction = checkNotNull(rawInstruction);
    this.commentPlacement = checkNotNull(commentPlacement);
  }

  @Override
  public Object getPersistentModel() {
    return rawInstruction;
  }

  @Override
  public boolean isCommentDelimiter() {
    return true;
  }

  @Override
  public boolean update(final String newContent) {
    checkNotNull(newContent);

    rawInstruction.setComment(newContent, commentPlacement);
    return true;
  }

  @Override
  public boolean updateComment(final String newContent, final ECommentPlacement placement) {
    return update(newContent);
  }
}
