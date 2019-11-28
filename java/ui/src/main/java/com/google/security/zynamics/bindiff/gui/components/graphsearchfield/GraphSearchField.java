package com.google.security.zynamics.bindiff.gui.components.graphsearchfield;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.searchers.GraphAddressSearcher;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSeacherFunctions;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcher;
import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CHexFormatter;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.comboboxes.memorybox.JMemoryBox;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class GraphSearchField extends JPanel {
  private static final int SEARCH_STRING_HISTORY_MAX = 50;

  private static final Icon ICON_CLEAR = ImageUtils.getImageIcon("data/buttonicons/clear.png");
  private static final Icon ICON_CLEAR_GRAY =
      ImageUtils.getImageIcon("data/buttonicons/clear-gray.png");

  private static final Icon ICON_NORMAL_SEARCH =
      ImageUtils.getImageIcon("data/buttonicons/normal-search.png");
  private static final Icon ICON_JUMP_TO_PRIMARY_ADDRESS =
      ImageUtils.getImageIcon("data/buttonicons/jump-primary-address.png");
  private static final Icon ICON_JUMP_TO_SECONDARY_ADDRESS =
      ImageUtils.getImageIcon("data/buttonicons/jump-secondary-address.png");

  private static final Icon ICON_NORMAL_SEARCH_FIELD =
      ImageUtils.getImageIcon("data/buttonicons/normal-searchfield.png");
  private static final Icon ICON_JUMP_TO_PRIMARY_ADDRESS_FIELD =
      ImageUtils.getImageIcon("data/buttonicons/jump-primary-addressfield.png");
  private static final Icon ICON_JUMP_TO_SECONDARY_ADDRESS_FIELD =
      ImageUtils.getImageIcon("data/buttonicons/jump-secondary-addressfield.png");

  private static final Color BACKGROUND_COLOR_FAIL = Colors.GRAY224;
  private static final Color BACKGROUND_COLOR_SUCCESS = Color.WHITE;

  private static final int ICON_SPACE = 38;
  private static final Insets BORDER_INSETS = new Insets(2, ICON_SPACE, 2, 0);

  private final ListenerProvider<IGraphSearchFieldListener> listeners = new ListenerProvider<>();
  private final MouseListener mouseListener;

  private final ViewTabPanelFunctions controller;
  private final GraphsContainer graphs;

  private final JMemoryBox searchCombo = new JMemoryBox(SEARCH_STRING_HISTORY_MAX);
  private final JMemoryBox priJumpCombo = new JMemoryBox(SEARCH_STRING_HISTORY_MAX);
  private final JMemoryBox secJumpCombo = new JMemoryBox(SEARCH_STRING_HISTORY_MAX);

  private final JTextField searchField =
      TextComponentUtils.addDefaultEditorActions(new JTextField());
  private final JFormattedTextField priHexField =
      TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CHexFormatter(16)));
  private final JFormattedTextField secHexField =
      TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CHexFormatter(16)));

  private final JButton clearSearchResultsButton;

  private final CPopupChooserAction popupChooserAction = new CPopupChooserAction();

  private Icon activeIcon = ICON_NORMAL_SEARCH_FIELD;

  public GraphSearchField(
      final ViewTabPanelFunctions controller, final JButton clearSearchResultsButton) {
    super(new BorderLayout());

    this.clearSearchResultsButton = clearSearchResultsButton;
    this.clearSearchResultsButton.setIcon(ICON_CLEAR_GRAY);

    this.controller = Preconditions.checkNotNull(controller);

    graphs = controller.getGraphs();

    setEditors();

    final CEditorBorder border = new CEditorBorder();
    searchField.setBorder(border);
    priHexField.setBorder(border);
    secHexField.setBorder(border);

    searchField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "search");
    searchField
        .getInputMap()
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "search");
    searchField
        .getInputMap()
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "search");
    searchField
        .getInputMap()
        .put(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
            "search");
    final CSearchAction searchAction = new CSearchAction();
    searchField.getActionMap().put("search", searchAction);

    priHexField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "zoomToAddress");
    priHexField.getActionMap().put("zoomToAddress", new CZoomToAddressAction());

    secHexField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "zoomToAddress");
    secHexField.getActionMap().put("zoomToAddress", new CZoomToAddressAction());

    mouseListener = new InternalMouseListener();
    searchField.addMouseListener(mouseListener);
    priHexField.addMouseListener(mouseListener);
    secHexField.addMouseListener(mouseListener);

    add(searchCombo, BorderLayout.CENTER);
  }

  private JPopupMenu createPopup() {
    final JPopupMenu popup = new JPopupMenu();

    popup.add(
        GuiUtils.buildMenuItem("Search Node Contents", ICON_NORMAL_SEARCH, popupChooserAction));

    popup.add(new JSeparator());

    popup.add(
        GuiUtils.buildMenuItem(
            "Jump to Primary Address", ICON_JUMP_TO_PRIMARY_ADDRESS, popupChooserAction));
    popup.add(
        GuiUtils.buildMenuItem(
            "Jump to Secondary Address", ICON_JUMP_TO_SECONDARY_ADDRESS, popupChooserAction));

    return popup;
  }

  private void setEditors() {
    searchCombo.setEditor(
        new BasicComboBoxEditor() {
          @Override
          protected JTextField createEditorComponent() {
            return searchField;
          }
        });

    priJumpCombo.setEditor(
        new BasicComboBoxEditor() {
          @Override
          protected JTextField createEditorComponent() {
            return priHexField;
          }
        });

    secJumpCombo.setEditor(
        new BasicComboBoxEditor() {
          @Override
          protected JTextField createEditorComponent() {
            return secHexField;
          }
        });
  }

  private void updateSearchBox(final Icon newIcon) {
    if (newIcon == ICON_NORMAL_SEARCH && activeIcon == ICON_NORMAL_SEARCH_FIELD) {
      searchField.getCaret().setVisible(true);
      searchField.grabFocus();
      searchCombo.updateUI();

      return;
    }
    if (newIcon == ICON_JUMP_TO_PRIMARY_ADDRESS
        && activeIcon == ICON_JUMP_TO_PRIMARY_ADDRESS_FIELD) {
      priHexField.getCaret().setVisible(true);
      priHexField.grabFocus();
      priJumpCombo.updateUI();

      return;
    }
    if (newIcon == ICON_JUMP_TO_SECONDARY_ADDRESS
        && activeIcon == ICON_JUMP_TO_SECONDARY_ADDRESS_FIELD) {
      secHexField.getCaret().setVisible(true);
      secHexField.grabFocus();
      secJumpCombo.updateUI();

      return;
    }

    if (activeIcon == ICON_NORMAL_SEARCH_FIELD) {
      remove(searchCombo);
    } else if (activeIcon == ICON_JUMP_TO_PRIMARY_ADDRESS_FIELD) {
      remove(priJumpCombo);
    } else if (activeIcon == ICON_JUMP_TO_SECONDARY_ADDRESS_FIELD) {
      remove(secJumpCombo);
    }

    if (newIcon == ICON_NORMAL_SEARCH) {
      activeIcon = ICON_NORMAL_SEARCH_FIELD;
    } else if (newIcon == ICON_JUMP_TO_PRIMARY_ADDRESS) {
      activeIcon = ICON_JUMP_TO_PRIMARY_ADDRESS_FIELD;
    } else if (newIcon == ICON_JUMP_TO_SECONDARY_ADDRESS) {
      activeIcon = ICON_JUMP_TO_SECONDARY_ADDRESS_FIELD;
    }

    if (activeIcon == ICON_NORMAL_SEARCH_FIELD) {
      add(searchCombo);

      searchField.getCaret().setVisible(true);
      searchField.grabFocus();
      searchCombo.updateUI();
    } else if (activeIcon == ICON_JUMP_TO_PRIMARY_ADDRESS_FIELD) {
      add(priJumpCombo);

      priHexField.getCaret().setVisible(true);
      priHexField.grabFocus();
      priJumpCombo.updateUI();
    } else if (activeIcon == ICON_JUMP_TO_SECONDARY_ADDRESS_FIELD) {
      add(secJumpCombo);

      secHexField.getCaret().setVisible(true);
      secHexField.grabFocus();
      secJumpCombo.updateUI();
    }
  }

  public void addListener(final IGraphSearchFieldListener searchFieldListener) {
    listeners.addListener(searchFieldListener);
  }

  public void dispose() {
    searchField.removeMouseListener(mouseListener);
    priHexField.removeMouseListener(mouseListener);
    secHexField.removeMouseListener(mouseListener);
  }

  public GraphsContainer getGraphs() {
    return graphs;
  }

  public void jumpToIndex(final int selectedResult, final ESide side) {
    BinDiffGraph<?, ?> graph;
    if (side == ESide.PRIMARY) {
      graph = graphs.getPrimaryGraph();
    } else {
      graph = graphs.getSecondaryGraph();
    }

    final GraphSearcher graphSearcher = graph.getGraphSearcher();

    Object obj = graphSearcher.getSubObjectResults().get(selectedResult).getObject();

    if (graph.getSettings().getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      if (obj instanceof ZyGraphNode<?>) {
        obj = ((SingleDiffNode) obj).getCombinedDiffNode();
      } else if (obj instanceof ZyGraphEdge<?, ?, ?>) {
        obj = ((SingleDiffEdge) obj).getCombinedDiffEdge();
      }

      graph = graphs.getCombinedGraph();
    }

    GraphSeacherFunctions.jumpToResultObject(graph, obj, true);
  }

  public void notifySearchFieldListener() {
    for (final IGraphSearchFieldListener listener : listeners) {
      listener.searched();
    }
  }

  public void setCaretIntoJumpToAddressField(final ESide side) {
    updateSearchBox(
        side == ESide.PRIMARY ? ICON_JUMP_TO_PRIMARY_ADDRESS : ICON_JUMP_TO_SECONDARY_ADDRESS);
  }

  public void setCaretIntoSearchField() {
    if (searchField.hasFocus() && searchField.getCaret().isVisible()) {
      return;
    }

    updateSearchBox(ICON_NORMAL_SEARCH);
  }

  private final class CEditorBorder extends AbstractBorder {
    @Override
    public Insets getBorderInsets(final Component c) {
      return BORDER_INSETS;
    }

    @Override
    public void paintBorder(
        final Component c, final Graphics g, final int x, final int y, final int w, final int h) {
      g.translate(x, y);

      activeIcon.paintIcon(c, g, x + 4, y + 5);

      g.setColor(MetalLookAndFeel.getControlDarkShadow());
      g.drawRect(0, 0, w, h - 1);
      g.setColor(MetalLookAndFeel.getControlShadow());
      g.drawRect(1, 1, w - 2, h - 3);

      g.translate(-x, -y);
    }
  }

  private final class CPopupChooserAction extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final JMenuItem item = (JMenuItem) event.getSource();
      updateSearchBox(item.getIcon());
    }
  }

  private final class CSearchAction extends AbstractAction {
    private void centerNextSearchHit(final boolean cycleBackwards, final boolean zoomToResult) {
      final String text = searchField.getText();

      if (GraphSeacherFunctions.getHasChanged(graphs, text)) {
        if (!text.equals("")) {
          iterateObjectResults(cycleBackwards, zoomToResult);
        } else {
          GraphSeacherFunctions.clearResults(graphs);

          for (final IGraphSearchFieldListener listener : listeners) {
            listener.cleaned();
          }
        }

        graphs.updateViews();
      } else {
        iterateObjectResults(cycleBackwards, zoomToResult);
      }
    }

    private void iterateObjectResults(final boolean cycleBackwards, final boolean zoomToResult) {
      if (!GraphSeacherFunctions.isEmpty(graphs)) {
        GraphSeacherFunctions.iterateObjectResults(graphs, cycleBackwards, zoomToResult);

        graphs.updateViews();
      }
    }

    private void search(final boolean zoomToResult) {
      final String searchString = searchField.getText();

      if (GraphSeacherFunctions.getHasChanged(graphs, searchString)) {
        if (!"".equals(searchString)) {
          searchCombo.add(searchString);
          searchField.setCaretPosition(searchField.getText().length());
        }

        try {
          for (final IGraphSearchFieldListener listener : listeners) {
            listener.cleaned();
          }

          GraphSeacherFunctions.search(graphs, searchString);

          if (GraphSeacherFunctions.isEmpty(graphs)) {
            searchCombo.getEditor().getEditorComponent().setBackground(BACKGROUND_COLOR_FAIL);
            clearSearchResultsButton.setIcon(ICON_CLEAR_GRAY);
          } else {
            searchCombo.getEditor().getEditorComponent().setBackground(BACKGROUND_COLOR_SUCCESS);
            clearSearchResultsButton.setIcon(ICON_CLEAR);
          }

          GraphSeacherFunctions.highlightSubObjectResults(graphs);

          GraphSeacherFunctions.jumpToFirstResultObject(graphs.getFocusedGraph(), zoomToResult);

          notifySearchFieldListener();

          graphs.updateViews();
        } catch (final PatternSyntaxException exception) {
          CMessageBox.showInformation(
              controller.getMainWindow(),
              String.format("Invalid Regular Expression '%s'", searchString));
        }
      }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource() == searchField && searchField.getText() != null) {
        if (searchField.getText().isEmpty()) {
          GraphSeacherFunctions.clearResults(graphs);
          searchField.setBackground(BACKGROUND_COLOR_SUCCESS);
          clearSearchResultsButton.setIcon(ICON_CLEAR_GRAY);

          for (final IGraphSearchFieldListener listener : listeners) {
            listener.cleaned();
          }

          return;
        }

        final boolean cycleBackwards =
            event.getModifiers() == ActionEvent.CTRL_MASK
                || event.getModifiers() == (ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        final boolean zoomToResult =
            event.getModifiers() == ActionEvent.SHIFT_MASK
                || event.getModifiers() == (ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        if (GraphSeacherFunctions.getHasChanged(graphs, searchField.getText())) {
          search(zoomToResult);
        } else {
          centerNextSearchHit(cycleBackwards, zoomToResult);
        }
      }
    }
  }

  private final class CZoomToAddressAction extends AbstractAction {
    private boolean jumpToAddress(final IAddress addr, final ESide side) {
      final String searchString =
          side == ESide.PRIMARY ? priHexField.getText() : secHexField.getText();

      if (!searchString.isEmpty()) {
        if (side == ESide.PRIMARY) {
          priJumpCombo.add(searchString);
          priHexField.setCaretPosition(searchString.length());
        } else {
          secJumpCombo.add(searchString);
          secHexField.setCaretPosition(searchString.length());
        }
      }

      BinDiffGraph<?, ?> graph = graphs.getCombinedGraph();

      if (controller.getGraphSettings().getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
        graph = side == ESide.PRIMARY ? graphs.getPrimaryGraph() : graphs.getSecondaryGraph();
      }

      if (graph instanceof SingleGraph) {
        final SingleDiffNode node = GraphAddressSearcher.searchAddress((SingleGraph) graph, addr);

        if (node != null) {
          GraphZoomer.zoomToNode(graph, node);
        }

        return node != null;
      } else if (graph instanceof CombinedGraph) {
        final CombinedDiffNode node =
            GraphAddressSearcher.searchAddress((CombinedGraph) graph, side, addr);

        if (node != null) {
          GraphZoomer.zoomToNode(graph, node);
        }

        return node != null;
      }

      return true;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource() == priHexField && priHexField.getText() != null) {
        if (priHexField.getText().isEmpty()) {
          priHexField.setBackground(BACKGROUND_COLOR_SUCCESS);
          return;
        }

        final IAddress address = new CAddress(priHexField.getText(), 16);
        priHexField.setBackground(
            jumpToAddress(address, ESide.PRIMARY)
                ? BACKGROUND_COLOR_SUCCESS
                : BACKGROUND_COLOR_FAIL);

        // this ensure that the caret stay at the end of the search address
        // (jumps otherwise to position 0 after return was pressed and jump address fails)
        priHexField.setFocusable(false);
        priHexField.setFocusable(true);
        priHexField.grabFocus();

        priHexField.updateUI();
      }

      if (event.getSource() == secHexField && secHexField.getText() != null) {
        if (secHexField.getText().isEmpty()) {
          secHexField.setBackground(BACKGROUND_COLOR_SUCCESS);
          return;
        }

        final IAddress address = new CAddress(secHexField.getText(), 16);
        secHexField.setBackground(
            jumpToAddress(address, ESide.SECONDARY)
                ? BACKGROUND_COLOR_SUCCESS
                : BACKGROUND_COLOR_FAIL);

        // this ensure that the caret stay at the end of the search address
        // (jumps otherwise to position 0 after return was pressed and jumps address fails)
        secHexField.setFocusable(false);
        secHexField.setFocusable(true);
        secHexField.grabFocus();

        secHexField.updateUI();
      }
    }
  }

  private final class InternalMouseListener extends MouseAdapter {
    private final JPopupMenu popupMenu = createPopup();

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.getX() >= 0 && event.getX() <= ICON_SPACE && !popupMenu.isVisible()) {
        popupMenu.show(GraphSearchField.this, getX() - 1, getY() + getHeight() - 2);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (popupMenu.isVisible() && event.getX() > ICON_SPACE) {
        if (event.getSource() instanceof JComponent
            && event.getY() >= 0
            && event.getY() <= ((JComponent) event.getSource()).getHeight()) {
          popupMenu.setVisible(false);
        }
      }
    }
  }
}
