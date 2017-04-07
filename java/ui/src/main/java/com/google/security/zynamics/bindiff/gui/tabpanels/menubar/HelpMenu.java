package com.google.security.zynamics.bindiff.gui.tabpanels.menubar;

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelFunctions;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class HelpMenu extends JMenu {
  public HelpMenu(final TabPanelFunctions controller) {
    super("Help");
    setMnemonic('H');

    final JMenuItem helpMenuItem =
        GuiUtils.buildMenuItem(
            "Help Contents",
            'H',
            KeyEvent.VK_F1,
            0,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.showHelp();
              }
            });

    final JMenuItem reportABugMenuItem =
        GuiUtils.buildMenuItem(
            "Report a Bug",
            'R',
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.reportABug();
              }
            });

    final JMenuItem checkForUpdatesMenuItem =
        GuiUtils.buildMenuItem(
            "Check for Updates...",
            'U',
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.checkForUpdates();
              }
            });

    final JMenuItem aboutMenuItem =
        GuiUtils.buildMenuItem(
            "About",
            'A',
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.showAboutDialog();
              }
            });

    add(helpMenuItem);
    add(new JSeparator());
    add(reportABugMenuItem);
    add(checkForUpdatesMenuItem);
    add(new JSeparator());
    add(aboutMenuItem);
  }
}
