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

public class InstructionObject extends AbstractEditableLineObject {
  private final RawInstruction rawInstruction;

  public InstructionObject(final RawInstruction rawInstruction, final int start, final int length) {
    super(start, length);
    checkNotNull(rawInstruction);

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
    checkNotNull(newContent);

    rawInstruction.setComment(newContent, placement);
    return true;
  }
}
