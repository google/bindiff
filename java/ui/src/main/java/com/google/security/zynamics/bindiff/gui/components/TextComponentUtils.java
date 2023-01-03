// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.gui.components;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;

/** Utility methods for Swing text fields. */
public class TextComponentUtils {
  private TextComponentUtils() {}

  /** Contains implementations for default editor actions */
  public static class EditorActions {
    private final UndoManager undoManager;

    private final TextAction undo =
        new TextAction("undo") {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (undoManager != null && undoManager.canUndo()) {
              undoManager.undo();
            }
          }
        };

    private final TextAction redo =
        new TextAction("redo") {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (undoManager != null && undoManager.canRedo()) {
              undoManager.redo();
            }
          }
        };

    private final TextAction cut = new DefaultEditorKit.CutAction();
    private final TextAction copy = new DefaultEditorKit.CopyAction();
    private final TextAction paste = new DefaultEditorKit.PasteAction();

    private final TextAction delete =
        new TextAction("delete") {
          @Override
          public void actionPerformed(ActionEvent e) {
            getTextComponent(e).replaceSelection("");
          }
        };

    private final TextAction selectAll =
        new TextAction(DefaultEditorKit.selectAllAction) {
          @Override
          public void actionPerformed(ActionEvent e) {
            getTextComponent(e).selectAll();
          }
        };

    public EditorActions(UndoManager undoManager) {
      this.undoManager = undoManager;
    }

    public TextAction getUndo() {
      return undo;
    }

    public TextAction getRedo() {
      return redo;
    }

    public TextAction getCut() {
      return cut;
    }

    public TextAction getCopy() {
      return copy;
    }

    public TextAction getPaste() {
      return paste;
    }

    public TextAction getDelete() {
      return delete;
    }

    public TextAction getSelectAll() {
      return selectAll;
    }
  }

  /** A popup menu with default actions for undo and clipboard operations. */
  public static class DefaultEditorPopupMenu extends JPopupMenu {
    public DefaultEditorPopupMenu(EditorActions actions) {
      JMenuItem item;

      item = new JMenuItem(actions.getUndo());
      item.setText("Undo");
      item.setMnemonic('u');
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
      add(item);

      item = new JMenuItem(actions.getRedo());
      item.setText("Redo");
      item.setMnemonic('r');
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
      add(item);

      add(new JSeparator());

      item = new JMenuItem(actions.getCut());
      item.setText("Cut");
      item.setMnemonic('t');
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
      add(item);

      item = new JMenuItem(actions.getCopy());
      item.setText("Copy");
      item.setMnemonic('c');
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
      add(item);

      item = new JMenuItem(actions.getPaste());
      item.setText("Paste");
      item.setMnemonic('p');
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
      add(item);

      item = new JMenuItem(actions.getDelete());
      item.setText("Delete");
      item.setMnemonic('d');
      add(item);

      add(new JSeparator());

      item = new JMenuItem(actions.getSelectAll());
      item.setText("Select all");
      item.setMnemonic('a');
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
      add(item);
    }
  }

  /** Adds default editor actions to Swing text fields. */
  public static <T extends JTextComponent> T addDefaultEditorActions(final T textComponent) {
    final UndoManager undoManager = new UndoManager();
    textComponent.getDocument().addUndoableEditListener(undoManager);

    final EditorActions actions = new EditorActions(undoManager);
    textComponent.setComponentPopupMenu(new DefaultEditorPopupMenu(actions));
    actions.getDelete().setEnabled(false);

    textComponent.addCaretListener(
        e -> {
          final boolean haveSelection = e.getDot() != e.getMark();
          final boolean haveClipboardData =
              Toolkit.getDefaultToolkit()
                  .getSystemClipboard()
                  .isDataFlavorAvailable(DataFlavor.stringFlavor);
          actions.getUndo().setEnabled(undoManager.canUndo());
          actions.getRedo().setEnabled(undoManager.canRedo());
          actions.getCut().setEnabled(haveSelection);
          actions.getCopy().setEnabled(haveSelection);
          actions.getPaste().setEnabled(haveClipboardData);
          actions.getDelete().setEnabled(haveSelection);
          actions.getSelectAll().setEnabled(textComponent.getText().length() > 0);
        });
    textComponent.setSelectionStart(0); // Fires the caret listener
    return textComponent;
  }
}
