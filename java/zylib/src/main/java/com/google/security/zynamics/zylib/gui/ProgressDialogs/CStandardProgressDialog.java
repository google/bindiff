// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;


public class CStandardProgressDialog extends JDialog {
  private static final long serialVersionUID = 7140381762236285546L;

  private boolean m_finished = false;

  private final IStandardProgressModel m_progressModel;

  private final InternalListener m_internalListener = new InternalListener();

  // private final String m_description;

  private final CProgressPanel m_progressPanel;

  public CStandardProgressDialog(final Window parent, final String title, final String description,
      final IStandardProgressModel progressModel) {
    super(parent, title, ModalityType.DOCUMENT_MODAL);

    // m_description = description;

    m_progressModel = progressModel;

    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    addWindowListener(new InternalWindowListener());

    progressModel.addProgressListener(m_internalListener);

    m_progressPanel = new CProgressPanel(description, false, false);
    m_progressPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    add(m_progressPanel);

    pack();

    if (parent != null) {
      GuiHelper.centerChildToParent(parent, this, true);
    } else {
      GuiHelper.centerOnScreen(this);
    }
  }

  // private static String convertTextToHtml(final String text)
  // {
  // return "<html>" + text.replaceAll("\n", "<br>") + "</html>";
  // }

  public static void show(final Window parent, final String title, final String description,
      final CStandardHelperThread thread) {
    final CStandardProgressDialog dlg =
        new CStandardProgressDialog(parent, title, description, thread);

    thread.start();

    dlg.setVisible(true);
  }

  public void setDescription(final String description) {
    m_progressPanel.setText(description);
  }

  public void setSubDescription(final String subDescription) {
    m_progressPanel.setSubText(subDescription);

    if (getParent() != null) {
      GuiHelper.centerChildToParent(getParent(), this, true);
    } else {
      GuiHelper.centerOnScreen(this);
    }
  }

  @Override
  public void setVisible(final boolean visible) {
    if (!m_finished) {
      try {
        super.setVisible(visible);
      } catch (final Exception exception) {
        // Workaround for a weird crash issue
      }
    }
  }

  private class InternalListener implements IStandardProgressListener {
    @Override
    public void changedDescription(final String description) {
      setSubDescription(description);
    }

    @Override
    public void changedMaximum(final int maximum) {
      m_progressPanel.setMaximum(maximum);
    }

    @Override
    public void finished() {
      m_finished = true;

      m_progressModel.removeProgressListener(this);

      dispose();
    }

    @Override
    public void next() {
      m_progressPanel.next();
    }

    @Override
    public void reset() {
      m_progressPanel.reset();
    }
  }

  private class InternalWindowListener extends WindowAdapter {
    @Override
    public void windowClosed(final WindowEvent arg0) {
    }

    @Override
    public void windowClosing(final WindowEvent arg0) {
      m_progressModel.closeRequested();
    }
  }
}
