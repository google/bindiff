// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyRegenerateableRealizer;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CAbstractKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CBackspaceKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CCharKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CCopyKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CCursorKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CCutKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CDelKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CEndKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CHomeKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CInsertKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CPasteKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CRedoKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CReturnKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CSelectAllKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CTabKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.CUndoKeyBehavior;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyCaret;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class CDefaultLabelEventHandler extends KeyAdapter {
  private final AbstractZyGraph<?, ?> m_graph;

  private final Map<Integer, CAbstractKeyBehavior> m_ctrlKeyBehaviourMap =
      new HashMap<Integer, CAbstractKeyBehavior>();
  private final Map<Integer, CAbstractKeyBehavior> m_keyBehaviourMap =
      new HashMap<Integer, CAbstractKeyBehavior>();

  private final CUndoManager m_undoManager = new CUndoManager();

  private ZyLabelContent m_activeLabelContent;

  private IZyRegenerateableRealizer m_activeRealizer;

  private final InternalFocusListener m_focusListener = new InternalFocusListener();

  private final ListenerProvider<ILabelEditableContentListener> m_editModeListener =
      new ListenerProvider<ILabelEditableContentListener>();

  public CDefaultLabelEventHandler(final AbstractZyGraph<?, ?> graph) {
    m_graph = graph;

    m_graph.addViewFocusListener(m_focusListener);

    init();
  }

  private String getContentSnippet(final int yPos) {
    String snippet = "";

    int firstLineIndex = getActiveLabelContent().getFirstLineIndexOfModelAt(yPos);
    int lastLineIndex = getActiveLabelContent().getLastLineIndexOfModelAt(yPos);

    for (int lineIndex = firstLineIndex; lineIndex <= lastLineIndex; ++lineIndex) {
      snippet += getActiveLabelContent().getLineContent(lineIndex).getText();
    }

    if (firstLineIndex > 0) {
      lastLineIndex = firstLineIndex - 1;
      firstLineIndex = getActiveLabelContent().getFirstLineIndexOfModelAt(lastLineIndex);

      String content = "";
      for (int lineIndex = firstLineIndex; lineIndex <= lastLineIndex; ++lineIndex) {
        content += getActiveLabelContent().getLineContent(lineIndex).getText();
      }

      snippet = content + snippet;
    }

    lastLineIndex = getActiveLabelContent().getLastLineIndexOfModelAt(yPos);
    if ((lastLineIndex + 1) < (getActiveLabelContent().getLineCount() - 1)) {
      firstLineIndex = lastLineIndex + 1;
      lastLineIndex = getActiveLabelContent().getLastLineIndexOfModelAt(firstLineIndex);

      for (int lineIndex = firstLineIndex; lineIndex <= lastLineIndex; ++lineIndex) {
        snippet += getActiveLabelContent().getLineContent(lineIndex).getText();
      }
    }

    return snippet;
  }

  private void init() {
    m_keyBehaviourMap.put(KeyEvent.VK_UP, new CCursorKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_DOWN, new CCursorKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_LEFT, new CCursorKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_RIGHT, new CCursorKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_TAB, new CTabKeyBehavior(m_undoManager)); // jump to next/prev
                                                                                // editable object
    m_keyBehaviourMap.put(KeyEvent.VK_HOME, new CHomeKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_END, new CEndKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_INSERT, new CInsertKeyBehavior(m_undoManager)); // copy //
                                                                                      // Ctrl-INS //
                                                                                      // paste //
                                                                                      // Shift-INS
    m_keyBehaviourMap.put(KeyEvent.VK_DELETE, new CDelKeyBehavior(m_undoManager)); // cut //
                                                                                   // SHIFT-Del
    m_keyBehaviourMap.put(KeyEvent.VK_BACK_SPACE, new CBackspaceKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(KeyEvent.VK_ENTER, new CReturnKeyBehavior(m_undoManager));
    m_keyBehaviourMap.put(null, new CCharKeyBehavior(m_undoManager));
    m_ctrlKeyBehaviourMap.put(KeyEvent.VK_A, new CSelectAllKeyBehavior(m_undoManager)); // select
                                                                                        // all //
                                                                                        // Ctrl-A
    m_ctrlKeyBehaviourMap.put(KeyEvent.VK_X, new CCutKeyBehavior(m_undoManager)); // cut // Ctrl-X
    m_ctrlKeyBehaviourMap.put(KeyEvent.VK_C, new CCopyKeyBehavior(m_undoManager)); // copy // Ctrl-C
    m_ctrlKeyBehaviourMap.put(KeyEvent.VK_V, new CPasteKeyBehavior(m_undoManager)); // paste //
                                                                                    // Ctrl-V
    m_ctrlKeyBehaviourMap.put(KeyEvent.VK_Y, new CRedoKeyBehavior(m_undoManager)); // redo // ctrl-Y
    m_ctrlKeyBehaviourMap.put(KeyEvent.VK_Z, new CUndoKeyBehavior(m_undoManager)); // undo // ctrl-Z
  }

  protected AbstractZyGraph<?, ?> getGraph() {
    return m_graph;
  }

  public void activateLabelContent(final ZyLabelContent labelContent,
      final IZyRegenerateableRealizer activeRealizer) {
    if ((labelContent == null) || (activeRealizer == null)) {
      return; // should not happen
    }
    unregisterListener();

    if ((getActiveLabelContent() != null) && (labelContent != getActiveLabelContent())) {
      if (getActiveLabelContent().isSelectable()) {
        getActiveLabelContent().showCaret(false);

        m_graph.updateViews();
      }
    }

    setActiveLabelContent(labelContent);
    m_activeRealizer = activeRealizer;
    getActiveLabelContent().showCaret(getActiveLabelContent().isSelectable());

    m_undoManager.setLabelContent(getActiveLabelContent());

    registerListener();
  }

  public void addEditModeListener(final ILabelEditableContentListener listener) {
    m_editModeListener.addListener(listener);
  }

  public void addKeyBehaviour(final Integer keyCode, final CAbstractKeyBehavior behaviour,
      final boolean ctrl) {
    if (ctrl) {
      m_ctrlKeyBehaviourMap.put(keyCode, behaviour);
    } else {
      m_keyBehaviourMap.put(keyCode, behaviour);
    }
  }

  public void deactivateLabelContent() {
    unregisterListener();

    if ((getActiveLabelContent() != null) && getActiveLabelContent().isSelectable()) {
      getActiveLabelContent().showCaret(false);

      setActiveLabelContent(null);
      m_activeRealizer = null;
    }

    m_graph.updateViews();

    m_undoManager.setLabelContent(getActiveLabelContent());

    registerListener();
  }

  public void dispose() {
    m_graph.removeViewFocusListener(m_focusListener);
  }

  public void handleMouseDraggedEvent(final double labelParentX, final double labelParentY,
      final double mouseX, final double mouseY, final double zoomFactor) {
    final ZyCaret caret = getActiveLabelContent().getCaret();

    final int oldlr = caret.getYmouseReleased();
    final int oldpr = caret.getXmouseReleased();

    caret.setCaretEnd(labelParentX, labelParentY, mouseX, mouseY, zoomFactor);

    if ((caret.getYmouseReleased() != oldlr) || (caret.getXmouseReleased() != oldpr)) {
      m_activeRealizer.repaint();
    }
  }

  public void handleMousePressedEvent(final double labelParentX, final double labelParentY,
      final double mouseX, final double mouseY, final double zoomFactor) {
    final ZyCaret caret = getActiveLabelContent().getCaret();

    caret.setCaretStart(labelParentX, labelParentY, mouseX, mouseY, zoomFactor);
    caret.setCaretEnd(labelParentX, labelParentY, mouseX, mouseY, zoomFactor);

    m_activeRealizer.repaint();
  }

  public void handleMouseReleasedEvent(final double labelParentX,
      final double labelParentY,
      final double mouseX,
      final double mouseY,
      final double zoomFactor,
      final int clickCount) {
    final ZyCaret caret = getActiveLabelContent().getCaret();

    switch (clickCount) {
      case 1:
        caret.setCaretEnd(labelParentX, labelParentY, mouseX, mouseY, zoomFactor);
        break;
      case 2:
        caret.selectWord(labelParentX, labelParentY, mouseX, mouseY, zoomFactor);
        break;
      case 3:
        caret.selectLine(labelParentY, mouseY);
        break;
      case 4:
        caret.selectAll();
    }

    m_activeRealizer.repaint();
  }

  public boolean hasEmptySelection() {
    return getActiveLabelContent().getSelectedText().equals("");
  }

  public boolean isActive() {
    return getActiveLabelContent() != null;
  }

  public boolean isActiveLabel(final ZyLabelContent labelContent) {
    return (labelContent != null) && (labelContent == getActiveLabelContent());
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    if (!isActive()) {
      return;
    }

    if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
      deactivateLabelContent();

      return;
    }

    if ((event.getKeyCode() == KeyEvent.VK_TAB) && (event.getModifiers() != 0)
        && (event.getModifiers() != InputEvent.SHIFT_DOWN_MASK)) {
      return;
    }

    if (!getActiveLabelContent().isSelectable()) {
      return;
    }

    CAbstractKeyBehavior behaviour = null;

    final char keyText = KeyEvent.getKeyText(event.getKeyCode()).charAt(0);

    if (event.isControlDown() && (keyText >= 'A') && (keyText <= 'Z')) {
      behaviour = m_ctrlKeyBehaviourMap.get(event.getKeyCode());
    }

    if (behaviour == null) {
      behaviour = m_keyBehaviourMap.get(event.getKeyCode());

      if ((behaviour == null) && (event.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
          && !event.isControlDown() && getActiveLabelContent().isEditable()) {
        behaviour = m_keyBehaviourMap.get(null);
      }
    }

    if (behaviour != null) {
      int y = -1;
      String oldContent = "";

      if (getActiveLabelContent().isEditable()) {
        y = getActiveLabelContent().getCaret().getYmouseReleased();
        oldContent = getContentSnippet(y);
      }

      behaviour.keyPressed(getActiveLabelContent(), event);

      if (y > -1) {
        final String newContent = getContentSnippet(y);

        if (!oldContent.equals(newContent) && !oldContent.isEmpty()) {
          for (final ILabelEditableContentListener listener : m_editModeListener) {
            listener.editableContentChanged(getActiveLabelContent());
          }
        }
      }

      m_activeRealizer.regenerate();

      m_activeRealizer.repaint();

      m_graph.updateViews();
    }

    event.consume();
  }

  public void registerListener() {
    if (isActive()) {
      m_graph.addViewCanvasKeyListener(this);
    }
  }

  public void removeEditModeListener(final ILabelEditableContentListener listener) {
    m_editModeListener.removeListener(listener);
  }

  public void unregisterListener() {
    if (isActive()) {
      m_graph.removeViewCanvasKeyListener(this);
    }
  }

  private ZyLabelContent getActiveLabelContent() {
    return m_activeLabelContent;
  }

  private void setActiveLabelContent(ZyLabelContent m_activeLabelContent) {
    this.m_activeLabelContent = m_activeLabelContent;
  }

  private class InternalFocusListener implements FocusListener {
    @Override
    public void focusGained(final FocusEvent e) {}

    @Override
    public void focusLost(final FocusEvent e) {
      deactivateLabelContent();
    }
  }
}
