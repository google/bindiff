package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes;

import com.google.security.zynamics.bindiff.enums.ESortByCriterion;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.graph.filter.IGraphNodeMultiFilterListener;
import com.google.security.zynamics.bindiff.graph.filter.enums.EVisibilityFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.ITreeNodeSearcherListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ITreeNodeSorterListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;

public abstract class AbstractBaseTreeNode extends AbstractTreeNode {
  private InternalTreeNodeSearcherListener searchListener = new InternalTreeNodeSearcherListener();
  private InternalGraphFilterListener filterListener = new InternalGraphFilterListener();
  private InternalTreeNodeSorterListener sorterListener = new InternalTreeNodeSorterListener();
  private InternalGraphVisibilityListener visibilityListener =
      new InternalGraphVisibilityListener();

  public AbstractBaseTreeNode(final AbstractRootTreeNode rootNode) {
    super(rootNode);

    getSearcher().addListener(searchListener);
    getFilter().addListener(filterListener);
    getSorter().addListener(sorterListener);

    // Note: Single graph does not notify visibility changed events
    getGraph()
        .getCombinedGraph()
        .getIntermediateListeners()
        .addIntermediateListener(visibilityListener);
  }

  @Override
  protected void delete() {
    getSearcher().removeListener(searchListener);
    getFilter().removeListener(filterListener);
    getSorter().removeListener(sorterListener);

    // Note: Single graph does not notify visibility changed events
    getGraph()
        .getCombinedGraph()
        .getIntermediateListeners()
        .removeIntermediateListener(visibilityListener);

    filterListener = null;
    searchListener = null;
    sorterListener = null;
    visibilityListener = null;

    super.delete();
  }

  protected abstract void updateTreeNodes(final boolean updateSearch);

  private class InternalGraphFilterListener implements IGraphNodeMultiFilterListener {
    @Override
    public void filterChanged(final GraphNodeMultiFilter filter) {
      updateTreeNodes(false);
    }
  }

  private class InternalGraphVisibilityListener implements IZyGraphVisibilityListener {
    private void handleNotificationAndUpdateTreeNodes() {
      boolean update = getFilter().getVisibilityFilterValue() != EVisibilityFilter.NONE;
      if (!update) {
        for (final Pair<ESortByCriterion, ESortOrder> criterion : getSorter()) {
          update = criterion.first() == ESortByCriterion.VISIBILITY;
          if (update) {
            break;
          }
        }
      }

      if (update) {
        updateTreeNodes(false);
      }
    }

    @Override
    public void nodeDeleted() {
      handleNotificationAndUpdateTreeNodes();
    }

    @Override
    public void visibilityChanged() {
      handleNotificationAndUpdateTreeNodes();
    }
  }

  private class InternalTreeNodeSearcherListener implements ITreeNodeSearcherListener {
    @Override
    public void searchStringChanged(final TreeNodeSearcher searcher) {
      updateTreeNodes(true);
    }
  }

  private class InternalTreeNodeSorterListener implements ITreeNodeSorterListener {
    @Override
    public void sortingChanged(final TreeNodeMultiSorter sorter) {
      updateTreeNodes(false);
    }
  }
}
