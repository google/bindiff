package com.google.security.zynamics.bindiff.gui.components.treesearchfield;

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

    getEditor().getEditorComponent().addKeyListener(listener);
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
