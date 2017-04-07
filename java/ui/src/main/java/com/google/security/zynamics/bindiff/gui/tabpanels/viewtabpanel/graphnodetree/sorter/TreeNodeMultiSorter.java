package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter;

import com.google.security.zynamics.bindiff.enums.ESortByCriterium;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.BasicBlockTreeNodeMatchStateComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.CombinedTreeNodeAdressComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.CombinedTreeNodeSideComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.FunctionTreeNodeMatchStateComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.FunctionTreeNodeNameComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.FunctionTreeNodeTypeComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.SingleTreeNodeAdressComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.TreeNodeSelectionComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators.TreeNodeVisibilityComparator;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TreeNodeMultiSorter implements Iterable<Pair<ESortByCriterium, ESortOrder>> {
  public static final int MAX_DEPTH = 5;

  private final ListenerProvider<ITreeNodeSorterListener> listeners = new ListenerProvider<>();

  private final List<Pair<ESortByCriterium, ESortOrder>> criteria = new ArrayList<>();

  public TreeNodeMultiSorter() {
    criteria.add(new Pair<>(ESortByCriterium.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterium.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterium.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterium.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterium.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterium.NONE, ESortOrder.ASCENDING));
  }

  public void addListener(final ITreeNodeSorterListener listener) {
    listeners.addListener(listener);
  }

  public List<Comparator<ISortableTreeNode>> getCombinedBasicblockTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterium, ESortOrder> criterium : criteria) {
      switch (criterium.first()) {
        case ADDRESS:
          comparatorList.add(new CombinedTreeNodeAdressComparator(criterium.second()));
          break;
        case MATCHSTATE:
          comparatorList.add(new BasicBlockTreeNodeMatchStateComparator(criterium.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterium.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterium.second()));
          break;
        case SIDE:
          comparatorList.add(new CombinedTreeNodeSideComparator(criterium.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  public List<Comparator<ISortableTreeNode>> getCombinedFunctionTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterium, ESortOrder> criterium : criteria) {
      switch (criterium.first()) {
        case ADDRESS:
          comparatorList.add(new CombinedTreeNodeAdressComparator(criterium.second()));
          break;
        case MATCHSTATE:
          comparatorList.add(new FunctionTreeNodeMatchStateComparator(criterium.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterium.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterium.second()));
          break;
        case SIDE:
          comparatorList.add(new CombinedTreeNodeSideComparator(criterium.second()));
          break;
        case FUNCTIONTYPE:
          comparatorList.add(new FunctionTreeNodeTypeComparator(criterium.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  public List<Comparator<ISortableTreeNode>> getSingleBasicblockTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterium, ESortOrder> criterium : criteria) {
      switch (criterium.first()) {
        case ADDRESS:
          comparatorList.add(new SingleTreeNodeAdressComparator(criterium.second()));
          break;
        case MATCHSTATE:
          comparatorList.add(new BasicBlockTreeNodeMatchStateComparator(criterium.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterium.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterium.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  public List<Comparator<ISortableTreeNode>> getSingleFunctionTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterium, ESortOrder> criterium : criteria) {
      switch (criterium.first()) {
        case ADDRESS:
          comparatorList.add(new CombinedTreeNodeAdressComparator(criterium.second()));
          break;
        case MATCHSTATE:
          comparatorList.add(new FunctionTreeNodeMatchStateComparator(criterium.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterium.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterium.second()));
          break;
        case FUNCTIONTYPE:
          comparatorList.add(new FunctionTreeNodeTypeComparator(criterium.second()));
          break;
        case FUNCTIONNAME:
          comparatorList.add(new FunctionTreeNodeNameComparator(criterium.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  @Override
  public Iterator<Pair<ESortByCriterium, ESortOrder>> iterator() {
    return criteria.iterator();
  }

  public void notifyListeners() {
    for (final ITreeNodeSorterListener listener : listeners) {
      listener.sortingChanged(this);
    }
  }

  public void removeListener(final ITreeNodeSorterListener listener) {
    listeners.removeListener(listener);
  }

  public void setCriterium(
      final ESortByCriterium sortBy,
      final ESortOrder order,
      final int criteriumDepth,
      final boolean notify) {
    if (criteriumDepth < 0 || criteriumDepth > MAX_DEPTH) {
      Logger.logSevere("Severe: Criterium depth is out of range.");

      return;
    }

    if (criteria.get(criteriumDepth).first() != sortBy
        || criteria.get(criteriumDepth).second() != order) {
      criteria.remove(criteriumDepth);

      criteria.add(criteriumDepth, new Pair<>(sortBy, order));

      if (notify) {
        notifyListeners();
      }
    }
  }
}
