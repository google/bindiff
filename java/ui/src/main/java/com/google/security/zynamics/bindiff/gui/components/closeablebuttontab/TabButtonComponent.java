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

package com.google.security.zynamics.bindiff.gui.components.closeablebuttontab;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseAllViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CloseViewAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

public class TabButtonComponent extends JPanel {
  private final InternalMouseListener mouseListener = new InternalMouseListener();

  private final JTabbedPane pane;

  private final TabPanel tabPanel;

  private final TabButton closeButton;

  private final TabLabel label;

  public TabButtonComponent(
      final JTabbedPane pane, final TabPanel tabPanel, final Icon icon, final boolean enableClose) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));

    this.pane = Preconditions.checkNotNull(pane);
    this.tabPanel = Preconditions.checkNotNull(tabPanel);

    closeButton = new TabButton(pane, this, enableClose);

    label = new TabLabel(pane, this);
    label.setFont(GuiHelper.getDefaultFont().deriveFont(GuiHelper.getDefaultFont().getSize() + 2.0f));
    if (icon != null) {
      label.setIcon(icon);
    }
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));

    add(label);

    if (enableClose) {
      add(closeButton);
    }

    setOpaque(false);

    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

    addMouseListener(mouseListener);
  }

  public void addListener(final ICloseTabButtonListener listener) {
    closeButton.addListener(listener);
  }

  public TabPanel getTabPanel() {
    return tabPanel;
  }

  public void removeListener(final ICloseTabButtonListener listener) {
    closeButton.removeListener(listener);
  }

  public void setIcon(final Icon icon) {
    label.setIcon(icon);
    label.updateUI();
  }

  public void setTitle(final String title) {
    label.setText(title);
    label.updateUI();
  }

  private class InternalMouseListener extends MouseAdapter {
    private JPopupMenu popup = null;

    private void createPopupmenu() {
      if (popup == null) {
        popup = new JPopupMenu();
        popup.add(
            GuiUtils.buildMenuItem("Close View", new CloseViewAction((ViewTabPanel) tabPanel)));

        final WorkspaceTabPanelFunctions controller =
            ((WorkspaceTabPanel) pane.getComponentAt(0)).getController();
        popup.add(
            GuiUtils.buildMenuItem(
                "Close Others", new CloseAllViewsAction(controller, (ViewTabPanel) tabPanel)));
        popup.add(GuiUtils.buildMenuItem("Close All", new CloseAllViewsAction(controller)));
      }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
      final int index = pane.indexOfTabComponent(TabButtonComponent.this);
      pane.setSelectedIndex(index);

      if (index != 0 && e.isPopupTrigger()) {
        createPopupmenu();
        popup.show(TabButtonComponent.this, e.getX(), e.getY());
      }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
      final int index = pane.indexOfTabComponent(TabButtonComponent.this);
      if (index != 0 && e.isPopupTrigger()) {
        createPopupmenu();
        popup.show(TabButtonComponent.this, e.getX(), e.getY());
      }

      if (e.getButton() == MouseEvent.BUTTON2 && e.getClickCount() == 1) {
        final CloseViewAction action = new CloseViewAction((ViewTabPanel) tabPanel);
        action.execute();
      }
    }
  }
}
