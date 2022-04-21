// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.project.rawflowgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

public class RawInstructionComment {
  private final ECommentPlacement commentPlacement;
  private String text;
  private boolean modified = false;

  public RawInstructionComment(final String text, final ECommentPlacement commentPlacement) {
    this.text = checkNotNull(text);
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
