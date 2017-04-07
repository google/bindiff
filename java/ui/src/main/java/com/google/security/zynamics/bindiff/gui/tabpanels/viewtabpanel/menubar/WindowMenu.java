package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ResetDefaultPerspectiveAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleGraphsPerspectiveAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.TogglePrimaryPerspectiveAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleSecondaryPerspectiveAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class WindowMenu extends JMenu {
  private JMenuItem togglePrimaryPerspective;
  private JMenuItem toggleSecondaryPerspective;
  private JMenuItem toogleGraphsPerspective;
  private JMenuItem resetDefaultPerspective;

  public WindowMenu(final ViewTabPanelFunctions controller) {
    super("Window");
    setMnemonic('W');

    Preconditions.checkNotNull(controller);

    // TODO(cblichmann): Key won't work on OS X
    togglePrimaryPerspective =
        GuiUtils.buildMenuItem(
            "Show/Hide Primary",
            'P',
            KeyEvent.VK_F9,
            0,
            new TogglePrimaryPerspectiveAction(controller));

    toggleSecondaryPerspective =
        GuiUtils.buildMenuItem(
            "Show/Hide Secondary",
            'S',
            KeyEvent.VK_F10,
            0,
            new ToggleSecondaryPerspectiveAction(controller));

    toogleGraphsPerspective =
        GuiUtils.buildMenuItem(
            "Show/Hide Overviews",
            'G',
            KeyEvent.VK_F11,
            0,
            new ToggleGraphsPerspectiveAction(controller));

    resetDefaultPerspective =
        GuiUtils.buildMenuItem(
            "Reset Window Layout",
            'R',
            KeyEvent.VK_F12,
            0,
            new ResetDefaultPerspectiveAction(controller));

    add(togglePrimaryPerspective);
    add(toggleSecondaryPerspective);
    add(toogleGraphsPerspective);

    add(new JSeparator());

    add(resetDefaultPerspective);
  }

  public void dispose() {
    togglePrimaryPerspective = null;
    toggleSecondaryPerspective = null;
    toogleGraphsPerspective = null;
    resetDefaultPerspective = null;
  }
}
