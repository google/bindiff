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

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

public class CUndoStateData {
  private final ZyLabelContent m_labelContent;

  private final Object m_persistentModel;
  private final IZyEditableObject m_editableObject;

  private final String m_text;

  private final boolean m_isAboveLineComment;
  private final boolean m_isBehindLineComment;
  private final boolean m_isLabelComment;

  private final int m_caretStartX;
  private final int m_caretMousePressedX;
  private final int m_caretMousePressedY;

  private final int m_caretEndX;
  private final int m_caretMouseReleasedX;
  private final int m_caretMouseReleasedY;

  public CUndoStateData(final ZyLabelContent labelContent, final Object persistentModel,
      final IZyEditableObject editableObject, final String text, final boolean isAboveLineComment,
      final boolean isBehindLineComment, final boolean isLabelComment, final int caretStartX,
      final int caretMousePressedX, final int caretMousePressedY, final int caretEndX,
      final int caretMouseReleasedX, final int caretMouseReleasedY) {
    Preconditions.checkNotNull(labelContent, "Error: Label content can't be null.");
    Preconditions.checkNotNull(persistentModel, "Error: Persistent model can't be null.");
    Preconditions.checkNotNull(editableObject, "Error: Editable object cant be null.");
    Preconditions.checkNotNull(text, "Error: Text can't be null.");

    m_labelContent = labelContent;
    m_persistentModel = persistentModel;
    m_editableObject = editableObject;

    m_text = text;
    m_isAboveLineComment = isAboveLineComment;
    m_isBehindLineComment = isBehindLineComment;
    m_isLabelComment = isLabelComment;

    m_caretStartX = caretStartX;
    m_caretMousePressedX = caretMousePressedX;
    m_caretMousePressedY = caretMousePressedY;

    m_caretEndX = caretEndX;
    m_caretMouseReleasedX = caretMouseReleasedX;
    m_caretMouseReleasedY = caretMouseReleasedY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof CUndoStateData) {
      final CUndoStateData undoData = (CUndoStateData) obj;

      return

      (m_isAboveLineComment == undoData.m_isAboveLineComment)
          && (m_isBehindLineComment == undoData.m_isBehindLineComment)
          && (m_isLabelComment == undoData.m_isLabelComment)
          && (m_persistentModel == undoData.m_persistentModel) && m_text.equals(undoData.m_text);
    }

    return false;
  }

  @Override
  public int hashCode() {
    int hash = 0;

    int tempHash = 64 + m_text.hashCode();
    tempHash *= m_persistentModel.hashCode();

    hash += m_isAboveLineComment ? 1 : 2;
    hash += m_isBehindLineComment ? 4 : 8;
    hash += m_isBehindLineComment ? 16 : 32;
    hash += tempHash;

    return hash;
  }

  public void restore() {
    if (m_isAboveLineComment) {
      m_editableObject.updateComment(m_text, ECommentPlacement.ABOVE_LINE);
    } else if (m_isBehindLineComment) {
      m_editableObject.updateComment(m_text, ECommentPlacement.BEHIND_LINE);
    } else if (m_isLabelComment) {
      m_editableObject.update(m_text);
    } else {
      throw new RuntimeException("Not implemented yet.");
    }

    m_labelContent.getLineEditor().recreateLabelLines(m_labelContent, m_persistentModel);

    m_labelContent.getCaret().setCaret(m_caretStartX, m_caretMousePressedX, m_caretMousePressedY,
        m_caretEndX, m_caretMouseReleasedX, m_caretMouseReleasedY);
  }
}
