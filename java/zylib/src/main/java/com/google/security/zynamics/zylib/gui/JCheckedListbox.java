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

package com.google.security.zynamics.zylib.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.LineBorder;

/**
 * List class that can be used to have lists with checkable entries.
 *
 * <p>Note: Does not (yet) work with lists the grow or shrink.
 */
public class JCheckedListbox<T> extends JList<T> {
  /** Keeps track of what list items are m_selected. */
  private final boolean[] m_selected;

  private boolean m_selectCompleteLine = true;

  public JCheckedListbox(final ListModel<T> model) {
    super(model);

    m_selected = new boolean[model.getSize()];

    setCellRenderer(new CheckedListboxRenderer<T>());

    addMouseListener(new InternalListener());
  }

  public JCheckedListbox(final T[] listData) {
    super(listData);

    m_selected = new boolean[listData.length];

    setCellRenderer(new CheckedListboxRenderer<T>());

    addMouseListener(new InternalListener());
  }

  public JCheckedListbox(final Vector<T> listData) {
    super(listData);

    m_selected = new boolean[listData.size()];

    setCellRenderer(new CheckedListboxRenderer<T>());

    addMouseListener(new InternalListener());
  }

  public JCheckedListbox(final Vector<T> listData, final boolean selectCompleteLine) {
    super(listData);

    this.m_selectCompleteLine = selectCompleteLine;

    m_selected = new boolean[listData.size()];

    setCellRenderer(new CheckedListboxRenderer<T>());

    addMouseListener(new InternalListener());
  }

  public boolean isChecked(final int index) {
    return m_selected[index];
  }

  public void setChecked(final int index, final boolean isSelected) {
    m_selected[index] = isSelected;

    updateUI();

    fireSelectionValueChanged(index, index, false);
  }

  @Override
  public void updateUI() {
    super.updateUI();
  }

  private class CheckedListboxRenderer<T> extends JPanel implements ListCellRenderer<T> {
    private static final long serialVersionUID = 7446207257969469739L;

    private final JCheckBox checkBox = new JCheckBox();

    private final LineBorder SELECTED_BORDER = new LineBorder(Color.DARK_GRAY);
    private final LineBorder UNSELECTED_BORDER = new LineBorder(Color.WHITE);

    private final Color SELECTED_COLOR = new Color(0xbdcfe7);
    private final Color UNSELECTED_COLOR = Color.WHITE;

    public CheckedListboxRenderer() {
      super(new BorderLayout());
      setBorder(UNSELECTED_BORDER);
      setBackground(UNSELECTED_COLOR);
      checkBox.setBackground(UNSELECTED_COLOR);
    }

    @Override
    public Component getListCellRendererComponent(
        final JList<? extends T> list,
        final T value,
        final int index,
        final boolean iss,
        final boolean chf) {
      checkBox.setEnabled(JCheckedListbox.this.isEnabled());

      if (m_selectCompleteLine) {
        setBorder(iss ? SELECTED_BORDER : UNSELECTED_BORDER);
        setBackground(iss ? SELECTED_COLOR : UNSELECTED_COLOR);
        checkBox.setBackground(iss ? SELECTED_COLOR : UNSELECTED_COLOR);
      }

      checkBox.setText(value.toString());
      checkBox.setSelected(m_selected[index]);

      checkBox.updateUI();

      add(checkBox, BorderLayout.WEST);

      return this;
    }
  }

  private class InternalListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if (!isEnabled()) {
        return;
      }

      // TODO: Somehow find a better way to check
      // whether the checkbox was hit.
      if (event.getPoint().x > 20) {
        return;
      }

      final int row = JCheckedListbox.this.locationToIndex(event.getPoint());

      m_selected[row] = !m_selected[row];

      updateUI();

      fireSelectionValueChanged(row, row, false);
    }
  }
}
