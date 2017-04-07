// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.resources.Constants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class CProgressPanel extends JPanel {
  private static final long serialVersionUID = 8176524035621381995L;

  private final JLabel m_label = new JLabel();

  private final JProgressBar m_progressBar = new JProgressBar();

  private String m_description;

  private final ActionListener m_listener = new InternalActionListener();

  private final ActionListener m_externalCancelButtonListener;

  private final Timer m_timer = new Timer(1000, m_listener);

  private int m_seconds = 0;

  private final boolean m_showSeconds;

  public CProgressPanel(final String description, final boolean indeterminate,
      final boolean showCancelButton) {
    this(description, indeterminate, true, showCancelButton);
  }

  public CProgressPanel(final String description, final boolean indeterminate,
      final boolean showSeconds, final ActionListener cancelButtonListener) {
    m_externalCancelButtonListener =
        Preconditions.checkNotNull(cancelButtonListener,
            "Error: Cancel button listener can't be null.");

    m_description = description;
    m_showSeconds = showSeconds;

    createPanel(indeterminate, showSeconds, true, true);
  }

  public CProgressPanel(final String description, final boolean indeterminate,
      final boolean showSeconds, final boolean showCancelButton) {
    m_description = description;
    m_showSeconds = showSeconds;

    m_externalCancelButtonListener = null;

    createPanel(indeterminate, showSeconds, showCancelButton, false);
  }

  public CProgressPanel(final String description, final boolean indeterminate,
      final boolean showSeconds, final boolean border, final boolean showCancelButton) {
    m_description = description;
    m_showSeconds = showSeconds;

    m_externalCancelButtonListener = null;

    createPanel(indeterminate, showSeconds, showCancelButton, border);
  }

  private static String convertTextToHtml(final String text) {
    return "<html>" + text.replaceAll("\n", "<br>") + "</html>";
  }

  private void createPanel(final boolean indeterminate, final boolean showSeconds,
      final boolean showCancelButton, final boolean addBorder) {
    setLayout(new BorderLayout());


    final JPanel pPb = new JPanel(new BorderLayout());
    pPb.setBorder(new TitledBorder(""));

    if (m_description == null) {
      m_label.setVisible(false);
    } else {
      m_label.setText(convertTextToHtml(m_description));
    }

    pPb.add(m_label, BorderLayout.NORTH);

    m_progressBar.setIndeterminate(indeterminate);
    m_progressBar.setStringPainted(true);

    final JPanel borderPanel = new JPanel(new BorderLayout());

    if (addBorder) {
      borderPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.GRAY),
          new EmptyBorder(1, 1, 1, 1)));
    }

    if (showCancelButton) {
      final JPanel buttonPanel = new JPanel(new BorderLayout());

      final JButton cancelButton = new JButton(new CancelAction());
      cancelButton.setFocusable(false);

      final JPanel paddingPanel = new JPanel(new BorderLayout());
      paddingPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
      paddingPanel.setMinimumSize(new Dimension(1, 0));

      buttonPanel.add(paddingPanel, BorderLayout.WEST);
      buttonPanel.add(cancelButton, BorderLayout.EAST);

      borderPanel.add(buttonPanel, BorderLayout.EAST);
    }

    borderPanel.add(m_progressBar, BorderLayout.CENTER);
    pPb.add(borderPanel, BorderLayout.CENTER);

    if (indeterminate && showSeconds) {
      updateSecondsText();

      m_timer.setRepeats(true);
    }

    add(pPb, BorderLayout.NORTH);
  }

  private void updateSecondsText() {
    m_progressBar.setString(String.format("%d seconds", ++m_seconds));
  }

  protected void closeRequested() {
  }

  public String fitTextToLabel(String text) {
    final FontMetrics metrics = m_label.getFontMetrics(m_label.getFont());
    final double labelWidth = getWidth() - 50;

    boolean fits = false;
    do {
      final double textWidth = metrics.stringWidth(text);

      if (labelWidth > textWidth) {
        fits = true;
      } else {
        if (text.length() > 4) {
          text = text.substring(0, text.length() - 4) + "...";
        } else {
          break;
        }
      }

    } while (!fits && (text.length() > 4));

    return text;
  }

  public void next() {
    m_progressBar.setValue(m_progressBar.getValue() + 1);
  }

  public void reset() {
    m_progressBar.setValue(0);
  }

  public void setMaximum(final int maximum) {
    m_progressBar.setMaximum(maximum);
  }

  public void setProgressText(final String displayString) {
    m_progressBar.setString(displayString);
  }

  public void setSubText(final String subDescription) {
    String text = fitTextToLabel(m_description) + "\n";
    text += fitTextToLabel(subDescription);

    m_label.setText(convertTextToHtml(text));

    new SwingInvoker() {
      @Override
      public void operation() {
        m_label.updateUI();
      }
    }.invokeLater();
  }

  public void setText(final String description) {
    m_description = description;

    String text = convertTextToHtml(description);
    text = fitTextToLabel(text);

    m_label.setText(text);

    new SwingInvoker() {
      @Override
      public void operation() {
        m_label.updateUI();
      }
    }.invokeLater();
  }

  public void setValue(final int value) {
    m_progressBar.setValue(value);
  }

  public void start() {
    if (m_progressBar.isIndeterminate() && m_showSeconds) {
      m_seconds = 0;
      updateSecondsText();
      m_timer.start();
    }
  }

  public void stop() {
    if (m_progressBar.isIndeterminate() && m_showSeconds) {
      m_timer.stop();
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = 3809222494243730570L;

    public CancelAction() {
      super("", new ImageIcon(Constants.class.getResource("cancel.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      if (m_externalCancelButtonListener != null) {
        m_externalCancelButtonListener.actionPerformed(e);
      }

      closeRequested();
    }
  }

  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      updateSecondsText();
    }
  }
}
