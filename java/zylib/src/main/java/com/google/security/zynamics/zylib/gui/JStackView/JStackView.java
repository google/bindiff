// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JStackView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;

/**
 * This class can be used to display the stack of a target process.
 */
public final class JStackView extends JPanel {
  private static final long serialVersionUID = -7850318708757157383L;

  /**
   * Scrollbar that is used to scroll through the dataset.
   */
  private final JScrollBar m_scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1);

  /**
   * Default internal listener that is used to handle various events.
   */
  private final InternalListener m_listener = new InternalListener();

  private final IStackModel m_model;

  private final JStackPanel m_panel;

  private final JScrollBar m_bottomScrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);

  /**
   * Creates a new stack view.
   * 
   * @param model The model that provides the data displayed in the view.
   */
  public JStackView(final IStackModel model) {
    super(new BorderLayout());

    Preconditions.checkNotNull(model, "Error: Model argument can not be null");

    m_model = model;

    m_panel = new JStackPanel(model);

    add(m_panel);

    // Necessary to receive input
    setFocusable(true);

    initScrollbar();

    setPreferredSize(new Dimension(400, 400));

    m_model.addListener(m_listener);
    addComponentListener(m_listener);
    addMouseWheelListener(new InternalMouseListener());
  }

  /**
   * Creates and initializes the scroll bar that is used to scroll through the data.
   */
  private void initScrollbar() {
    m_scrollbar.addAdjustmentListener(m_listener);
    m_bottomScrollbar.addAdjustmentListener(m_listener);

    add(m_scrollbar, BorderLayout.EAST);

    add(m_bottomScrollbar, BorderLayout.SOUTH);
  }

  private void setCurrentPosition(final long newPosition) {
    final int newFirstLine = (int) ((newPosition - m_model.getStartAddress()) / 4); // Avoid
                                                                                    // notifying
                                                                                    // twice

    m_scrollbar.setValue(newFirstLine);

    m_panel.repaint();
  }

  private void setHorizontalScrollBarMaximum() {
    final int totalWidth = m_panel.getOffsetViewWidth() + 10 + 80;

    final int realWidth = getWidth();

    if ((realWidth >= totalWidth) || (m_panel.getCharWidth() == 0)) {
      m_bottomScrollbar.setValue(0);
      m_bottomScrollbar.setEnabled(false);
    } else {
      m_bottomScrollbar.setMaximum(((totalWidth - realWidth) / m_panel.getCharWidth()) + 1);
      m_bottomScrollbar.setEnabled(true);
    }
  }

  /**
   * Updates the maximum scroll range of the scroll bar depending on the number of bytes in the
   * current data set.
   */
  private void setScrollBarMaximum() {
    final int visibleRows = m_panel.getNumberOfVisibleRows();

    final int totalRows = m_model.getNumberOfEntries();
    int scrollRange = (2 + totalRows) - visibleRows;

    if (scrollRange < 0) {
      scrollRange = 0;
      m_scrollbar.setValue(0);
      m_scrollbar.setEnabled(false);
    } else {
      m_scrollbar.setEnabled(true);
    }

    m_scrollbar.setMaximum(scrollRange);
  }

  public String getValueAt(final Point point) {
    return m_panel.getValueAt(point);
  }

  /**
   * Scrolls to the given offset.
   * 
   * @param offset The offset to scroll to.
   */
  public void gotoOffset(final long offset) {
    setCurrentPosition(offset);
  }

  public void setDefinitionStatus(final DefinitionStatus status) {
    m_panel.setDefinitionStatus(status);
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);

    m_panel.repaint();
  }

  private class InternalListener implements AdjustmentListener, ComponentListener,
      IStackModelListener {
    @Override
    public void adjustmentValueChanged(final AdjustmentEvent event) {
      if (event.getSource() == m_scrollbar) {
        m_panel.setFirstRow(event.getValue());
      } else {
        m_panel.setFirstColumn(event.getValue());
      }

      m_panel.repaint();
    }

    @Override
    public void componentHidden(final ComponentEvent event) {
      // Do nothing
    }

    @Override
    public void componentMoved(final ComponentEvent event) {
      // Do nothing
    }

    @Override
    public void componentResized(final ComponentEvent event) {
      setScrollBarMaximum();
      setHorizontalScrollBarMaximum();
    }

    @Override
    public void componentShown(final ComponentEvent event) {
      // Do nothing
    }

    @Override
    public void dataChanged() {
      setScrollBarMaximum();
      setHorizontalScrollBarMaximum();

      m_panel.repaint();
    }
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
      // Mouse wheel support for scrolling

      if (!isEnabled()) {
        return;
      }

      final int notches = e.getWheelRotation();
      m_scrollbar.setValue(m_scrollbar.getValue() + notches);
    }
  }
}
