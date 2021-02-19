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

import java.util.ArrayList;
import java.util.List;

public class CUndoHistory {
  private final List<CUndoStateData> m_undoStates = new ArrayList<CUndoStateData>();

  private int m_statePointer = 0;

  public void addState(final CUndoStateData undoData) {
    if (m_statePointer < (m_undoStates.size() - 1)) {
      final int offset = m_statePointer > 0 ? -1 : 0;

      final List<CUndoStateData> statesToRemove = new ArrayList<CUndoStateData>();

      for (int index = m_statePointer - offset; index < m_undoStates.size(); ++index) {
        statesToRemove.add(m_undoStates.get(index));
      }

      for (final CUndoStateData state : statesToRemove) {
        m_undoStates.remove(state);
      }
    }

    if (!m_undoStates.isEmpty()) {
      final CUndoStateData prevData = m_undoStates.get(m_undoStates.size() - 1);

      if (prevData.equals(undoData)) {
        m_undoStates.remove(m_undoStates.size() - 1);
      }
    }

    m_undoStates.add(undoData);

    m_statePointer = m_undoStates.size() - 1;
  }

  public boolean isEmpty() {
    return m_undoStates.isEmpty();
  }

  public void redo() {
    if (m_statePointer < (m_undoStates.size() - 1)) {
      ++m_statePointer;

      m_undoStates.get(m_statePointer).restore();
    }
  }

  public void undo() {
    if (!m_undoStates.isEmpty() && (m_statePointer > 0)) {
      --m_statePointer;

      m_undoStates.get(m_statePointer).restore();
    }
  }
}
