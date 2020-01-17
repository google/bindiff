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

package com.google.security.zynamics.bindiff.gui.tabpanels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.components.MaterialChromeTabbedPaneUI;
import com.google.security.zynamics.bindiff.gui.components.closeablebuttontab.ICloseTabButtonListener;
import com.google.security.zynamics.bindiff.gui.components.closeablebuttontab.TabButtonComponent;
import com.google.security.zynamics.bindiff.gui.dialogs.searchresultsdialog.SearchResultsDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.detachedviewstabpanel.FunctionDiffViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.ViewToolbarPanel;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListenerAdapter;
import com.google.security.zynamics.bindiff.project.diff.IDiffListener;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class TabPanelManager implements Iterable<TabPanel> {
  private static final Color WORKSPACE_TAB_COLOR = Color.WHITE;
  private static final Color SINGLE_FUNCTION_DIFF_VIEWS_TAB_COLOR = Color.WHITE;

  private static final ColorSlot[] colorSlots = {
    ColorSlot.make(Colors.DIFF_A_VIEW_TABS_COLOR),
    ColorSlot.make(Colors.DIFF_B_VIEW_TABS_COLOR),
    ColorSlot.make(Colors.DIFF_C_VIEW_TABS_COLOR),
    ColorSlot.make(Colors.DIFF_D_VIEW_TABS_COLOR)
  };

  private final InternalCloseTabButtonListener closeTabListener =
      new InternalCloseTabButtonListener();

  private final InternalTabListener tablistener = new InternalTabListener();

  private final MainWindow window;

  private final JTabbedPane tabbedPane = new JTabbedPane();

  private final List<TabPanel> tabPanels = new ArrayList<>();

  private final Workspace workspace;

  public TabPanelManager(final MainWindow window, final Workspace workspace) {
    this.window = Preconditions.checkNotNull(window);
    this.workspace = Preconditions.checkNotNull(workspace);

    tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
    tabbedPane.setFocusable(false);
    tabbedPane.setUI(new MaterialChromeTabbedPaneUI());

    addListener();
  }

  private void addListener() {
    tabbedPane.addChangeListener(tablistener);
  }

  private Color getTabColor(final TabPanel tab) {
    if (tab instanceof FunctionDiffViewTabPanel) {
      return SINGLE_FUNCTION_DIFF_VIEWS_TAB_COLOR;
    }

    if (tab instanceof ViewTabPanel) {
      final Diff diff = ((ViewTabPanel) tab).getDiff();

      for (final ColorSlot slot : colorSlots) {
        if (slot.diff == diff) {
          return slot.color;
        }
      }
    }

    return Colors.GRAY240;
  }

  private void registerTabColor(final TabPanel tab) {
    if (tab instanceof ViewTabPanel) {
      final Diff diff = ((ViewTabPanel) tab).getDiff();

      if (diff.isFunctionDiff()) {
        return;
      }

      for (final ColorSlot slot : colorSlots) {
        if (slot.diff == diff) {
          return;
        }
      }

      for (final ColorSlot slot : colorSlots) {
        if (slot.diff == null) {
          final IDiffListener diffListener = new InternalDiffListener();
          diff.addListener(diffListener);
          slot.diff = diff;
          slot.diffListener = diffListener;

          return;
        }
      }
    }
  }

  private void unregisterTabColor(final Diff diff) {
    for (final ViewTabPanel panel :
        window.getController().getTabPanelManager().getViewTabPanels()) {
      if (panel.getDiff() == diff) {
        return;
      }
    }

    for (final ColorSlot slot : colorSlots) {
      if (slot.diff == diff) {
        slot.diff.removeListener(slot.diffListener);
        slot.diff = null;
        slot.diffListener = null;
      }
    }
  }

  public void addTab(final TabPanel tab) {
    tabbedPane.addTab(tab.getTitle().toString(), tab);

    final int pos = tabbedPane.getTabCount() - 1;

    final TabButtonComponent tabButtonComponent;
    Color tabColor = Color.WHITE;

    if (tab instanceof WorkspaceTabPanel) {
      tabButtonComponent = new TabButtonComponent(tabbedPane, tab, tab.getIcon(), false);
      tabColor = WORKSPACE_TAB_COLOR;
    } else {
      registerTabColor(tab);

      tabColor = getTabColor(tab);

      tabButtonComponent = new TabButtonComponent(tabbedPane, tab, tab.getIcon(), true);
      tabButtonComponent.addListener(closeTabListener);
    }

    tabbedPane.setTabComponentAt(pos, tabButtonComponent);
    tabbedPane.setBackgroundAt(pos, tabColor);

    tabPanels.add(tab);
  }

  public JTabbedPane getTabbedPane() {
    return tabbedPane;
  }

  public ViewTabPanel getTabPanel(
      final IAddress priFunctionAddr, final IAddress secFunctionAddr, final Diff diff) {
    for (final TabPanel panel : tabPanels) {
      if (panel instanceof ViewTabPanel) {
        final ViewData view = ((ViewTabPanel) panel).getView();

        if (view.getGraphs().getDiff() != diff) {
          continue;
        }
        final IAddress priAddr = view.getAddress(ESide.PRIMARY);
        final IAddress secAddr = view.getAddress(ESide.SECONDARY);

        if (view.isCallgraphView()
            && priAddr == null
            && secAddr == null
            && priFunctionAddr == null
            && secFunctionAddr == null) {
          return (ViewTabPanel) panel;
        } else if (view.isFlowgraphView()) {
          boolean priIsEqual = priAddr == null && priFunctionAddr == null;

          if (priAddr != null && priFunctionAddr != null) {
            priIsEqual = priAddr.equals(priFunctionAddr);
          }

          boolean secIsEqual = secAddr == null && secFunctionAddr == null;

          if (secAddr != null && secFunctionAddr != null) {
            secIsEqual = secAddr.equals(secFunctionAddr);
          }

          if (priIsEqual && secIsEqual) {
            return (ViewTabPanel) panel;
          }
        }
      }
    }

    return null;
  }

  public List<ViewTabPanel> getViewTabPanels() {
    final List<ViewTabPanel> viewTabPanels = new ArrayList<>();

    for (final TabPanel panel : tabPanels) {
      if (panel instanceof ViewTabPanel) {
        viewTabPanels.add((ViewTabPanel) panel);
      }
    }

    return viewTabPanels;
  }

  public List<ViewTabPanel> getViewTabPanels(final boolean isFunctionDiff) {
    final List<ViewTabPanel> viewTabPanels = new ArrayList<>();

    for (final TabPanel panel : tabPanels) {
      if (panel instanceof ViewTabPanel
          && ((ViewTabPanel) panel).getDiff().isFunctionDiff() == isFunctionDiff) {
        viewTabPanels.add((ViewTabPanel) panel);
      }
    }

    return viewTabPanels;
  }

  public WorkspaceTabPanel getWorkspaceTabPanel() {
    return (WorkspaceTabPanel) tabbedPane.getComponentAt(0);
  }

  @Override
  public Iterator<TabPanel> iterator() {
    return tabPanels.iterator();
  }

  public void removeListener() {
    tabbedPane.addChangeListener(tablistener);
  }

  public void removeTab(final TabPanel tab) {
    tabPanels.remove(tab);

    final int index = tabbedPane.indexOfComponent(tab);

    tabbedPane.remove(index);
  }

  public void selectTab(final TabPanel tabPanel) {
    tabbedPane.setSelectedComponent(tabPanel);
  }

  public void selectTabPanel(
      final IAddress priFunctionAddr, final IAddress secFunctionAddr, final Diff diff) {
    final ViewTabPanel panel = getTabPanel(priFunctionAddr, secFunctionAddr, diff);
    if (panel != null) {
      selectTab(panel);
    }
  }

  public void udpateSelectedTabIcon() {
    final int index = tabbedPane.getSelectedIndex();
    final TabButtonComponent component = (TabButtonComponent) tabbedPane.getTabComponentAt(index);
    if (component != null) {
      component.setIcon(tabPanels.get(index).getIcon());
      component.updateUI();
    }
  }

  public void updateSelectedTabTitel(final String tabTitle) {
    final int index = tabbedPane.getSelectedIndex();
    final TabButtonComponent component = (TabButtonComponent) tabbedPane.getTabComponentAt(index);
    if (component != null) {
      component.setTitle(tabTitle);
      component.updateUI();
    }
  }

  private class InternalCloseTabButtonListener implements ICloseTabButtonListener {
    @Override
    public boolean closing(final TabButtonComponent tabButtonComponent) {
      final ViewTabPanel tabPanel = (ViewTabPanel) tabButtonComponent.getTabPanel();

      final ViewData viewData = tabPanel.getView();
      if (tabPanel.getController().hasChanged()) {
        tabPanel.getTabPanelManager().selectTab(tabPanel);

        final int answer =
            CMessageBox.showYesNoCancelQuestion(
                window, String.format("'%s' has been modified. Save?", viewData.getViewName()));
        if (answer == JOptionPane.CANCEL_OPTION) {
          return false;
        }

        tabPanel.getController().closeView(answer == JOptionPane.YES_OPTION);
      } else {
        tabPanel.getController().closeView(false);
      }

      return true;
    }
  }

  private static final class ColorSlot {
    protected final Color color;

    protected Diff diff;
    protected IDiffListener diffListener;

    private ColorSlot(final Color color) {
      this.color = Preconditions.checkNotNull(color);
      this.diff = null;
      this.diffListener = null;
    }

    protected static ColorSlot make(final Color color) {
      return new ColorSlot(color);
    }
  }

  private class InternalDiffListener extends DiffListenerAdapter {
    @Override
    public void closedView(final Diff diff) {
      unregisterTabColor(diff);
    }
  }

  private class InternalTabListener implements ChangeListener {
    @Override
    public void stateChanged(final ChangeEvent changeEvent) {
      final TabPanel tabPanel = (TabPanel) tabbedPane.getSelectedComponent();

      window.updateTitle(workspace, tabPanel);

      window.setJMenuBar(tabPanel.getMenuBar());

      for (final TabPanel tab : tabPanels) {
        if (tab instanceof ViewTabPanel) {
          if (tabPanel == tab) {
            final ViewToolbarPanel toolbar = ((ViewTabPanel) tab).getToolbar();
            final SearchResultsDialog dlg = toolbar.getSearchResultsDialog();
            dlg.setVisible(dlg.getReshowDialog());
          } else {
            final ViewToolbarPanel toolbar = ((ViewTabPanel) tab).getToolbar();
            final SearchResultsDialog dlg = toolbar.getSearchResultsDialog();
            final boolean reshow = dlg.isVisible() || dlg.getReshowDialog();
            dlg.setVisible(false);
            dlg.setReshowDialog(reshow);
          }
        }
      }
    }
  }
}
