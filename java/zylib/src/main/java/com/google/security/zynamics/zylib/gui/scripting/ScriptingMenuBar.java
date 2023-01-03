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

package com.google.security.zynamics.zylib.gui.scripting;

import com.google.security.zynamics.zylib.resources.Constants;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

/** Default menu bar used in the scripting dialog. */
public abstract class ScriptingMenuBar extends JMenuBar {
  private static final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  private final CutAction cutAction = new CutAction();

  private final CopyAction copyAction = new CopyAction();

  private final PasteAction pasteAction = new PasteAction();

  private final ExecuteAgainAction executeAgainAction = new ExecuteAgainAction();

  private File lastExecutedFile = null;

  protected ScriptingMenuBar() {
    final JMenu fileMenu = new JMenu(Constants.MENU_FILE);

    fileMenu.addSeparator();

    fileMenu.add(new ExecuteAction());
    fileMenu.add(executeAgainAction);

    fileMenu.addSeparator();

    fileMenu.add(new NewConsoleTabAction());
    fileMenu.add(new CloseTabAction());

    add(fileMenu);

    final JMenu editMenu = new JMenu(Constants.MENU_EDIT);

    editMenu.addSeparator();
    editMenu.add(cutAction);
    editMenu.add(copyAction);
    editMenu.add(pasteAction);

    add(editMenu);
  }

  protected abstract void closeTabMenuClicked();

  protected abstract void copyMenuClicked();

  protected abstract void cutMenuClicked();

  protected abstract void executeAgainMenuClicked();

  protected abstract void executeMenuClicked();

  protected File getLastExecutedScriptFile() {
    return lastExecutedFile;
  }

  protected abstract void newConsoleTabMenuClicked();

  protected abstract void pasteMenuClicked();

  protected void setLastExecutedScriptFile(final File file) {
    lastExecutedFile = file;

    executeAgainAction.setFile(file);
  }

  public void updateGui(final boolean hasOptions) {
    cutAction.setEnabled(hasOptions);
    copyAction.setEnabled(hasOptions);
    pasteAction.setEnabled(hasOptions);
  }

  private class CloseTabAction extends AbstractAction {
    public CloseTabAction() {
      super(Constants.MENU_CLOSE_ACTIVE_TAB);

      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, CTRL_MASK));
      putValue(SMALL_ICON, new ImageIcon(Constants.class.getResource("folder_delete.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      closeTabMenuClicked();
    }
  }

  private class CopyAction extends AbstractAction {
    public CopyAction() {
      super(Constants.MENU_EDIT_COPY);

      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_MASK));
      putValue(SMALL_ICON, new ImageIcon(Constants.class.getResource("page_white_copy.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      copyMenuClicked();
    }
  }

  private class CutAction extends AbstractAction {
    public CutAction() {
      super(Constants.MENU_EDIT_CUT);

      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, CTRL_MASK));
      putValue(SMALL_ICON, new ImageIcon(Constants.class.getResource("cut.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      cutMenuClicked();
    }
  }

  private class ExecuteAction extends AbstractAction {
    public ExecuteAction() {
      super(Constants.MENU_EXECUTE_SCRIPT);

      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, CTRL_MASK));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      executeMenuClicked();
    }
  }

  private class ExecuteAgainAction extends AbstractAction {
    public ExecuteAgainAction() {
      super(String.format(Constants.MENU_EXECUTE_AGAIN_SCRIPT, "-"));

      putValue(
          ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, CTRL_MASK | KeyEvent.SHIFT_MASK));

      setFile(null);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      executeAgainMenuClicked();
    }

    public void setFile(final File file) {
      if (file != null) {
        putValue(
            AbstractAction.NAME,
            String.format(Constants.MENU_EXECUTE_AGAIN_SCRIPT, file.getAbsolutePath()));
      } else {
        putValue(AbstractAction.NAME, String.format(Constants.MENU_EXECUTE_AGAIN_SCRIPT, "-"));
      }

      setEnabled(file != null);
    }
  }

  private class NewConsoleTabAction extends AbstractAction {
    public NewConsoleTabAction() {
      super(Constants.MENU_SCRIPTING_CONSOLE);

      putValue(
          SMALL_ICON, new ImageIcon(Constants.class.getResource("application_xp_terminal.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      newConsoleTabMenuClicked();
    }
  }

  private class PasteAction extends AbstractAction {
    public PasteAction() {
      super(Constants.MENU_EDIT_PASTE);

      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, CTRL_MASK));
      putValue(SMALL_ICON, new ImageIcon(Constants.class.getResource("page_white_paste.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      pasteMenuClicked();
    }
  }
}
