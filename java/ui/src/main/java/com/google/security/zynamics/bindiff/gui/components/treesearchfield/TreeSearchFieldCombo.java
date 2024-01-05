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

package com.google.security.zynamics.bindiff.gui.components.treesearchfield;

import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.comboboxes.memorybox.JMemoryBox;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;

public class TreeSearchFieldCombo extends JMemoryBox {
  private static final int SEARCH_STRING_HISTORY_MAX = 25;

  private final ListenerProvider<ITreeSearchFieldListener> listeners = new ListenerProvider<>();

  private final InternalKeyListener listener = new InternalKeyListener();

  private String lastSearchString = "";

  public TreeSearchFieldCombo() {
    super(SEARCH_STRING_HISTORY_MAX);

    final JTextField textField = (JTextField) getEditor().getEditorComponent();
    TextComponentUtils.addDefaultEditorActions(textField);
    textField.addKeyListener(listener);
  }

  private String getText() {
    return ((JTextField) getEditor().getEditorComponent()).getText();
  }

  public void addListener(final ITreeSearchFieldListener listener) {
    listeners.addListener(listener);
  }

  public void clear() {
    setSelectedItem("");

    lastSearchString = "";
  }

  public void dispose() {
    getEditor().getEditorComponent().removeKeyListener(listener);
  }

  public void removeListener(final ITreeSearchFieldListener listener) {
    listeners.removeListener(listener);
  }

  private class InternalKeyListener implements KeyListener {
    @Override
    public void keyPressed(final KeyEvent event) {}

    @Override
    public void keyReleased(final KeyEvent event) {}

    @Override
    public void keyTyped(final KeyEvent event) {
      if (event.getKeyChar() == '\n') {
        if (!getText().equals(lastSearchString)) {
          if (!getText().equals("")) {
            add(getText());
          }

          for (final ITreeSearchFieldListener listener : listeners) {
            listener.searchChanged(getText());
          }

          lastSearchString = getText();
        }
      }
    }
  }
}
