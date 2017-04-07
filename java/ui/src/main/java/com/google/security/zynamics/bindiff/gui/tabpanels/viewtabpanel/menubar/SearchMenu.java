package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.JumpToAddressAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SearchAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class SearchMenu extends JMenu {
  private JMenuItem jumpToPrimaryAddress;
  private JMenuItem jumpToSecondaryAddress;

  private JMenuItem search;

  public SearchMenu(final ViewTabPanelFunctions controller) {
    super("Search");
    setMnemonic('A');

    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    Preconditions.checkNotNull(controller);

    jumpToPrimaryAddress =
        GuiUtils.buildMenuItem(
            "Jump to Primary Address",
            'P',
            KeyEvent.VK_J,
            CTRL_MASK,
            new JumpToAddressAction(controller, ESide.PRIMARY));

    jumpToSecondaryAddress =
        GuiUtils.buildMenuItem(
            "Jump to Secondary Address",
            'S',
            KeyEvent.VK_J,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new JumpToAddressAction(controller, ESide.SECONDARY));

    search =
        GuiUtils.buildMenuItem(
            "Search", 'S', KeyEvent.VK_F, CTRL_MASK, new SearchAction(controller));

    add(search);

    add(new JSeparator());

    add(jumpToPrimaryAddress);
    add(jumpToSecondaryAddress);
  }

  public void dispose() {
    jumpToPrimaryAddress = null;
    jumpToSecondaryAddress = null;
    search = null;
  }
}
