// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.tables;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;

public class CActionSearch extends AbstractAction {
  private static final long serialVersionUID = -2773135367074178821L;
  private final CTableSearcher m_tableSearcher;

  public CActionSearch(final CTableSearcher tableSearcher) {
    super("Search");

    m_tableSearcher =
        Preconditions.checkNotNull(tableSearcher, "Internal Error: Table Searcher can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_tableSearcher.search();
  }
}
