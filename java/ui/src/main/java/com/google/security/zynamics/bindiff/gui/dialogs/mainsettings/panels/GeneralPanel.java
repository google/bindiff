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

package com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.bindiff.utils.SystemUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.FileChooser.FileChooserPanel;
import com.google.security.zynamics.zylib.io.DirectoryChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class GeneralPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 2;

  private FileChooserPanel idaDirectoryPanel;
  private FileChooserPanel workspaceDirectoryPanel;

  public GeneralPanel() {
    super(new BorderLayout());
    init();
  }

  private void init() {
    setBorder(new LineBorder(Color.GRAY));

    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    final InternalDirectoryListener idaDirectoryListener =
        new InternalDirectoryListener("Choose IDA Installation Directory");
    idaDirectoryPanel =
        new FileChooserPanel(
            settings.getIdaDirectory(), idaDirectoryListener, "...", 0, ROW_HEIGHT, 0);
    idaDirectoryListener.setPanel(idaDirectoryPanel);

    String workspaceDir = settings.getWorkspaceDirectory();
    if ("".equals(workspaceDir)) {
      workspaceDir = SystemUtils.getCurrentUserPersonalFolder();
    }

    final InternalDirectoryListener workspaceDirectoryListener =
        new InternalDirectoryListener("Choose Workspace Directory");
    workspaceDirectoryPanel =
        new FileChooserPanel(workspaceDir, workspaceDirectoryListener, "...", 0, ROW_HEIGHT, 0);
    workspaceDirectoryListener.setPanel(workspaceDirectoryPanel);

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));
    panel.setBorder(new TitledBorder("General settings"));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "IDA directory:", LABEL_WIDTH, idaDirectoryPanel, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Workspaces directory:", LABEL_WIDTH, workspaceDirectoryPanel, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public String getIDADirectory() {
    return idaDirectoryPanel.getText();
  }

  public String getWorkspaceDirectory() {
    return workspaceDirectoryPanel.getText();
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    idaDirectoryPanel.setText(settings.getIdaDirectory());

    String workspaceDir = settings.getWorkspaceDirectory();
    if ("".equals(workspaceDir)) {
      workspaceDir = SystemUtils.getCurrentUserPersonalFolder();
    }
    workspaceDirectoryPanel.setText(workspaceDir);
  }

  private class InternalDirectoryListener implements ActionListener {
    FileChooserPanel panel;
    final String title;

    public InternalDirectoryListener(final String title) {
      this.title = title;
    }

    public void setPanel(final FileChooserPanel panel) {
      this.panel = panel;
    }

    private String selectDirectory(final Container container) {
      final DirectoryChooser chooser = new DirectoryChooser(title);
      chooser.setCurrentDirectory(new File(panel.getText()));

      if (chooser.showOpenDialog(container) == JFileChooser.APPROVE_OPTION) {
        final File file = chooser.getSelectedFile();
        if (!file.exists()) {
          CMessageBox.showError(container, "Directory does not exist.");
          return null;
        }
        if (!file.canExecute()) {
          CMessageBox.showError(container, "Directory is not executable.");
          return null;
        }
        return file.getAbsolutePath();
      }

      return null;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final String path = selectDirectory(getParent());
      if (path != null) {
        panel.setText(path);
      }
    }
  }
}
