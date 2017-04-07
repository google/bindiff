package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.InitialCallGraphSettingsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.InitialFlowGraphSettingsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.MainSettingsAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class SettingsMenu extends JMenu {
  private final JMenuItem mainSettings;

  private final JMenuItem initialCallgraphSettings;

  private final JMenuItem initialFlowgraphSettings;

  public SettingsMenu(final WorkspaceTabPanelFunctions controller) {
    super("Settings");
    setMnemonic('S');

    // TODO(cblichmann): Remove shortcuts for settings, it should not be
    // necessary to access settings often.
    mainSettings =
        GuiUtils.buildMenuItem(
            "Main Settings...", 'M', KeyEvent.VK_F2, new MainSettingsAction(controller));

    initialCallgraphSettings =
        GuiUtils.buildMenuItem(
            "Initial Call Graph Settings...",
            'C',
            KeyEvent.VK_F2,
            InputEvent.SHIFT_DOWN_MASK,
            new InitialCallGraphSettingsAction(controller));

    initialFlowgraphSettings =
        GuiUtils.buildMenuItem(
            "Initial Flow Graph Settings...",
            'F',
            KeyEvent.VK_F2,
            InputEvent.CTRL_DOWN_MASK,
            new InitialFlowGraphSettingsAction(controller));

    add(mainSettings);

    add(new JSeparator());

    add(initialCallgraphSettings);
    add(initialFlowgraphSettings);
  }
}
