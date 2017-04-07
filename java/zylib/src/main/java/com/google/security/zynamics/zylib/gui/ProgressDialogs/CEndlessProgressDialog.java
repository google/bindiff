// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;

// TODO: Merge with CUnlimitedProgressDialog again?
public class CEndlessProgressDialog extends JDialog {
  private boolean m_finished = false;

  private final IEndlessProgressModel m_progressModel;

  private final InternalListener m_internalListener = new InternalListener();

  private final CProgressPanel m_progressPanel;

  public CEndlessProgressDialog(final Window parent, final String title, final String description,
      final IEndlessProgressModel progressModel) {
    super(parent, title, ModalityType.DOCUMENT_MODAL);

    m_progressModel = progressModel;

    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    addWindowListener(new InternalWindowListener());

    progressModel.addProgressListener(m_internalListener);

    m_progressPanel = new CProgressPanel(description, true, false);
    m_progressPanel.start();
    m_progressPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

    // Hack, fixes a strange bug, where label width and height gets screwed up
    setSubDescription("Please wait...");

    getContentPane().add(m_progressPanel);
    pack();

    setSubDescription("Please wait...");

    if (parent != null) {
      GuiHelper.centerChildToParent(parent, this, true);
    } else {
      GuiHelper.centerOnScreen(this);
    }
  }

  public static CEndlessProgressDialog show(final Window parent, final String title,
      final String description, final CEndlessHelperThread thread) {
    final CEndlessProgressDialog dlg =
        new CEndlessProgressDialog(parent, title, description, thread);

    thread.start();
    dlg.setVisible(true);

    return dlg;
  }

  public void setDescription(final String description) {
    m_progressPanel.setText(description);
  }

  public void setSubDescription(final String subDescription) {
    m_progressPanel.setSubText(subDescription);
  }

  @Override
  public void setVisible(final boolean visible) {
    if (!m_finished) {
      try {
        super.setVisible(visible);
      } catch (final Exception e) {
        // FIXME: Fix this "workaround", never catch all exceptions!
        // Workaround for a weird crash issue
      }
    }
  }

  private class InternalListener implements IEndlessProgressListener {
    @Override
    public void changedDescription(final String description) {
      setSubDescription(description);
    }

    @Override
    public void changedGeneralDescription(final String description) {
      setDescription(description);
    }

    @Override
    public void finished() {
      m_finished = true;

      m_progressModel.removeProgressListener(this);

      dispose();
    }
  }

  private class InternalWindowListener extends WindowAdapter {
    @Override
    public void windowClosed(final WindowEvent arg0) {
      m_progressPanel.stop();
    }

    @Override
    public void windowClosing(final WindowEvent arg0) {
      m_progressModel.closeRequested();
    }
  }
}
