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

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import java.util.HashMap;
import java.util.Map;


public class CUndoManager {
  private final Map<ZyLabelContent, CUndoHistory> m_undoHistories =
      new HashMap<ZyLabelContent, CUndoHistory>();

  private ZyLabelContent m_labelContent = null;

  private CUndoHistory getUndoHistory() {
    if (m_labelContent == null) {
      return null;
    }

    CUndoHistory undoHistory = m_undoHistories.get(m_labelContent);

    if (undoHistory == null) {
      undoHistory = new CUndoHistory();

      m_undoHistories.put(m_labelContent, undoHistory);
    }

    return undoHistory;
  }

  public void addUndoState(final ZyLabelContent labelContent, final Object persistantModel,
      final IZyEditableObject editableObject, final String changedText,
      final boolean isAboveLineComment, final boolean isBehindLineComment,
      final boolean isLabelComment, final int caretStartX, final int caretMousePressedX,
      final int caretMousePressedY, final int caretEndX, final int caretMouseReleasedX,
      final int caretMouseReleasedY) {
    if (persistantModel == null) {
      // Must be a placeholder object without label content text.
      return;
    }

    final CUndoHistory undoHistory = getUndoHistory();

    if (undoHistory != null) {
      final CUndoStateData undoData =
          new CUndoStateData(labelContent, persistantModel, editableObject, changedText,
              isAboveLineComment, isBehindLineComment, isLabelComment, caretStartX,
              caretMousePressedX, caretMousePressedY, caretEndX, caretMouseReleasedX,
              caretMouseReleasedY);

      undoHistory.addState(undoData);
    }
  }

  public void redo() {
    final CUndoHistory undoHistory = getUndoHistory();

    if (undoHistory != null) {
      undoHistory.redo();
    }
  }

  public void setLabelContent(final ZyLabelContent labelContent) {
    m_labelContent = labelContent;
  }

  public void undo() {
    final CUndoHistory undoHistory = getUndoHistory();

    if (undoHistory != null) {
      undoHistory.undo();
    }
  }
}
