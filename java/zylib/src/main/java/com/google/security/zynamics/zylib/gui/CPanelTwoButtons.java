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

package com.google.security.zynamics.zylib.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

// TODO(jannewger): this should be refactored so client code can pass in two
// listeners or in a way that this panel handles APPLY keyboard shortcuts as well.

/**
 * Creates a panel with two buttons and registers the given action listener.
 */
public class CPanelTwoButtons extends JPanel {
  private static final long serialVersionUID = -7197559018087702078L;

  private final String m_FirstButtonName;
  private final String m_SecondButtonName;

  private final JButton m_FirstButton;
  private final JButton m_SecondButton;

  private final ActionListener m_listener;

  public CPanelTwoButtons(final ActionListener listener, final String firstButtonName,
      final String secondButtonName) {
    super();
    m_listener = listener;
    m_FirstButtonName = firstButtonName;
    m_SecondButtonName = secondButtonName;
    setLayout(new BorderLayout());
    m_FirstButton = new JButton(m_FirstButtonName);
    m_SecondButton = new JButton(m_SecondButtonName);
    m_FirstButton.addActionListener(listener);
    m_SecondButton.addActionListener(listener);

    if (m_FirstButton.getPreferredSize().width > m_SecondButton.getPreferredSize().width) {
      m_SecondButton.setPreferredSize(m_FirstButton.getPreferredSize());
    } else {
      m_FirstButton.setPreferredSize(m_SecondButton.getPreferredSize());
    }

    final JPanel p = new JPanel(new GridLayout(1, 2));
    final JPanel p_ok = new JPanel();
    final JPanel p_cancel = new JPanel();

    p_ok.setBorder(new EmptyBorder(5, 5, 5, 5));
    p_cancel.setBorder(new EmptyBorder(5, 5, 5, 5));
    p_ok.add(m_FirstButton);
    p_cancel.add(m_SecondButton);
    p.add(p_ok);
    p.add(p_cancel);

    add(p, BorderLayout.EAST);
  }

  public JButton getFirstButton() {
    return m_FirstButton;
  }

  public void removeListener() {
    m_FirstButton.removeActionListener(m_listener);
    m_SecondButton.removeActionListener(m_listener);
  }
}
