package com.google.security.zynamics.bindiff.gui.components.viewsearchfield;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.comboboxes.memorybox.JMemoryBox;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextField;

/** Editable combo box used for searching in UI tables. */
public class TableTextSearchComboBox extends JMemoryBox {
  private static final Color BACKGROUND_COLOR_FAIL = Colors.GRAY224;
  private static final Color BACKGROUND_COLOR_SUCCESS = Color.WHITE;

  private static final int SEARCH_STRING_HISTORY_MAX = 30;

  private final ListenerProvider<IViewSearchFieldListener> listeners = new ListenerProvider<>();

  private final AbstractTable table;

  private final List<Pair<Integer, ESide>> affectedColumnIndices;

  private boolean isRegEx;
  private boolean isCaseSensitive;

  private boolean primarySideSearch;
  private boolean secondarySideSearch;

  private boolean temporaryTableUse;

  public TableTextSearchComboBox(
      final AbstractTable table, final List<Pair<Integer, ESide>> affectedColumnIndices) {
    super(SEARCH_STRING_HISTORY_MAX);

    this.table = Preconditions.checkNotNull(table);
    this.affectedColumnIndices = Preconditions.checkNotNull(affectedColumnIndices);

    final JTextField textField = (JTextField) getEditor().getEditorComponent();
    TextComponentUtils.addDefaultEditorActions(textField);
    textField.addKeyListener(new InternalKeyListener());
  }

  private String getText() {
    return ((JTextField) getEditor().getEditorComponent()).getText();
  }

  private void search(final boolean selectResultsOnly) {
    final List<Integer> result = search(getText());

    if (result.size() == 0) {
      getEditor().getEditorComponent().setBackground(BACKGROUND_COLOR_FAIL);
    } else {
      for (final IViewSearchFieldListener listener : listeners) {
        listener.searched(result, selectResultsOnly);
      }

      getEditor().getEditorComponent().setBackground(BACKGROUND_COLOR_SUCCESS);
    }
  }

  private List<Integer> search(final String searchString) {
    if (!"".equals(searchString)) {
      add(searchString);
    }

    final List<Integer> resultRowIndices = new ArrayList<>();

    if (!temporaryTableUse) {
      reset();
    }

    final int rows = table.getRowCount();

    boolean found;

    boolean primarySearch = primarySideSearch;
    boolean secondarySearch = secondarySideSearch;

    if (!primarySearch && !secondarySearch) {
      primarySearch = true;
      secondarySearch = true;
    }

    for (int row = 0; row < rows; ++row) {
      StringBuilder rowText = new StringBuilder();
      for (final Pair<Integer, ESide> column : affectedColumnIndices) {
        if (column.second() == ESide.PRIMARY && primarySearch) {
          rowText.append(table.getValueAt(row, column.first()));
        } else if (column.second() == ESide.SECONDARY && secondarySearch) {
          rowText.append(table.getValueAt(row, column.first()));
        }
      }

      if (isRegEx) {
        Pattern pattern;

        if (isCaseSensitive) {
          pattern = Pattern.compile(searchString);
        } else {
          pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
        }

        final Matcher matcher = pattern.matcher(rowText.toString());

        found = matcher.find(0);
      } else if (isCaseSensitive) {
        found = rowText.toString().contains(searchString);
      } else {
        found = rowText.toString().toLowerCase().contains(searchString.toLowerCase());
      }

      if (found) {
        resultRowIndices.add(row);
      }
    }

    return resultRowIndices;
  }

  public void addListener(final IViewSearchFieldListener searchFieldListener) {
    listeners.addListener(searchFieldListener);
  }

  public boolean isCaseSensitive() {
    return isCaseSensitive;
  }

  public boolean isPrimarySideSearch() {
    return primarySideSearch;
  }

  public boolean isRegEx() {
    return isRegEx;
  }

  public boolean isSecondarySideSearch() {
    return secondarySideSearch;
  }

  public boolean isTemporaryTableUse() {
    return temporaryTableUse;
  }

  public void removeListener(final IViewSearchFieldListener searchFieldListener) {
    listeners.removeListener(searchFieldListener);
  }

  public void reset() {
    for (final IViewSearchFieldListener listener : listeners) {
      listener.reset();
    }
  }

  public void setSearchOptions(
      final boolean isRegEx,
      final boolean isCaseSensitive,
      final boolean primarySideSearch,
      final boolean secondarySideSearch,
      final boolean tempTableUse) {
    this.isRegEx = isRegEx;
    this.isCaseSensitive = isCaseSensitive;

    this.primarySideSearch = primarySideSearch;
    this.secondarySideSearch = secondarySideSearch;

    this.temporaryTableUse = tempTableUse;

    search(getText());
  }

  public void updateResults() {
    if (!"".equals(getText())) {
      search(false);
    }
  }

  private class InternalKeyListener extends KeyAdapter {
    @Override
    public void keyTyped(final KeyEvent event) {
      if (event.getKeyChar() == '\n') {
        if ("".equals(getText())) {
          for (final IViewSearchFieldListener listener : listeners) {
            listener.reset();
          }
          getEditor().getEditorComponent().setBackground(BACKGROUND_COLOR_SUCCESS);
        } else {
          search(event.getModifiersEx() == InputEvent.CTRL_DOWN_MASK);
        }
      }
    }
  }
}
