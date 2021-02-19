// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.types.common.ICancelableCommand;
import com.google.security.zynamics.zylib.types.common.ICommand;

public class CUnlimitedProgressDialog extends JDialog implements IProgressDescription {
  private static final long serialVersionUID = 1009536934858788904L;

  private final ICommand m_command;

  private final InternalWindowListener m_windowListener = new InternalWindowListener();

  private final CProgressPanel m_progressPanel;

  private Exception m_exception;

  private final boolean m_isCancelable;

  private CUnlimitedProgressDialog(final Window parent, final String title,
      final String description, final ICommand command, final boolean isCancelable) {
    super(parent, title, ModalityType.DOCUMENT_MODAL);

    Preconditions.checkNotNull(command, "Error: Comand can't be null.");

    m_isCancelable = isCancelable;

    addWindowListener(m_windowListener);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    m_command = command;

    m_progressPanel = createProgressPanel(description, isCancelable, m_windowListener);
    m_progressPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    m_progressPanel.start();

    // Hack, fixes a strange bug, where label width and height gets screwed up
    setSubDescription("Please wait...");

    add(m_progressPanel);

    setSize(400, getPreferredSize().height);
    setMinimumSize(new Dimension(400, getPreferredSize().height));
    setMaximumSize(new Dimension(Math.max(400, getPreferredSize().width), getPreferredSize().height));

    pack();

    setSubDescription("Please wait...");

    if (parent != null) {
      GuiHelper.centerChildToParent(parent, this, true);
    } else {
      GuiHelper.centerOnScreen(this);
    }
  }

  public CUnlimitedProgressDialog(final Window parent, final String title,
      final String description, final ICancelableCommand command) {
    this(parent, title, description, command, true);
  }

  public CUnlimitedProgressDialog(final Window parent, final String title,
      final String description, final ICommand command) {
    this(parent, title, description, command, false);
  }

  private static CProgressPanel createProgressPanel(final String description,
      final boolean isCancelable, final InternalWindowListener windowListener) {
    if (isCancelable) {
      return new CProgressPanel(description, true, true, windowListener);
    }

    return new CProgressPanel(description, true, true, true, false);
  }

  private void setException(final Exception e) {
    // stores only the first exception that occurs
    if (m_exception == null) {
      m_exception = e;
    }
  }

  public Exception getException() {
    return m_exception;
  }

  @Override
  public synchronized void setDescription(final String description) {
    m_progressPanel.setText(description);
  }

  @Override
  public synchronized void setSubDescription(final String subDescription) {
    m_progressPanel.setSubText(subDescription);
  }

  // TODO(cblichmann): Refactor into a static showDialog() method that throws
  // a wrapped ProgressDialogException, so that we can have
  // proper exception handling without having to catch
  // exceptions of type Exception.
  @Override
  public void setVisible(final boolean value) {
    if (value && !isVisible()) {
      m_exception = null;

      final CountDownLatch countDownLatch = new CountDownLatch(2);

      final Thread mainWorkerThread = new Thread(new InternalCommandThread(countDownLatch));
      mainWorkerThread.start();

      super.setVisible(value);

      try {
        countDownLatch.await();
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else if (!value && isVisible()) {
      super.setVisible(false);
    }
  }

  public boolean wasCanceled() {
    return m_isCancelable && ((ICancelableCommand) m_command).wasCanceled();
  }

  private class InternalCommandThread implements Runnable {
    final private CountDownLatch m_countDownLatch;

    public InternalCommandThread(final CountDownLatch countDownLatch) {
      m_countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
      try {
        m_command.execute();
      } catch (final Exception e) {
        setException(e);
      }

      try {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            CUnlimitedProgressDialog.super.dispose();
            CUnlimitedProgressDialog.super.setVisible(false);

            m_countDownLatch.countDown();
          }
        });
      } catch (final Exception e) {
        setException(e);
      } finally {
        m_countDownLatch.countDown();
      }
    }
  }

  private class InternalWindowListener extends WindowAdapter implements ActionListener {
    private void cancel() {
      try {
        if (m_isCancelable) {
          setDescription("Canceling...");

          ((ICancelableCommand) m_command).cancel();
        }
      } catch (final Exception e) {
        setException(e);
      }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      cancel();
    }

    @Override
    public void windowClosing(final WindowEvent event) {
      cancel();
    }
  }
}
