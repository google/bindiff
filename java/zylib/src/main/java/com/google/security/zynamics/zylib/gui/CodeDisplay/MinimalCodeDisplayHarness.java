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

package com.google.security.zynamics.zylib.gui.CodeDisplay;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Simple harness for testing GUI code.
 */
public final class MinimalCodeDisplayHarness {

  /**
   * Build and display minimal GUI.
   */
  public static void main(String... args) {
    MinimalCodeDisplayHarness app = new MinimalCodeDisplayHarness();
    app.buildAndDisplayGui();
  }

  private void buildAndDisplayGui() {
    JFrame frame = new JFrame("Test Frame");
    buildContent(frame);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  private void buildContent(JFrame frame) {
    JPanel panel = new JPanel(new BorderLayout());

    CodeDisplayModelExample example = new CodeDisplayModelExample();

    panel.add(new CodeDisplay(example), BorderLayout.CENTER);

    JButton ok = new JButton("OK");
    ok.addActionListener(new ShowDialog(frame));
    panel.add(ok, BorderLayout.SOUTH);
    frame.getContentPane().add(panel);
  }

  private static final class ShowDialog implements ActionListener {
    /** Defining the dialog's owner JFrame is highly recommended. */
    ShowDialog(JFrame frame) {
      innerFrame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      JOptionPane.showMessageDialog(innerFrame, "This is a dialog");
    }

    private JFrame innerFrame;
  }
}
