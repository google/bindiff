// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESortByCriterion;
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
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TreeNodeMultiSorter implements Iterable<Pair<ESortByCriterion, ESortOrder>> {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static final int MAX_DEPTH = 5;

  private final ListenerProvider<ITreeNodeSorterListener> listeners = new ListenerProvider<>();

  private final List<Pair<ESortByCriterion, ESortOrder>> criteria = new ArrayList<>();

  public TreeNodeMultiSorter() {
    criteria.add(new Pair<>(ESortByCriterion.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterion.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterion.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterion.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterion.NONE, ESortOrder.ASCENDING));
    criteria.add(new Pair<>(ESortByCriterion.NONE, ESortOrder.ASCENDING));
  }

  public void addListener(final ITreeNodeSorterListener listener) {
    listeners.addListener(listener);
  }

  public List<Comparator<ISortableTreeNode>> getCombinedBasicBlockTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterion, ESortOrder> criterion : criteria) {
      switch (criterion.first()) {
        case ADDRESS:
          comparatorList.add(new CombinedTreeNodeAdressComparator(criterion.second()));
          break;
        case MATCH_STATE:
          comparatorList.add(new BasicBlockTreeNodeMatchStateComparator(criterion.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterion.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterion.second()));
          break;
        case SIDE:
          comparatorList.add(new CombinedTreeNodeSideComparator(criterion.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  public List<Comparator<ISortableTreeNode>> getCombinedFunctionTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterion, ESortOrder> criterion : criteria) {
      switch (criterion.first()) {
        case ADDRESS:
          comparatorList.add(new CombinedTreeNodeAdressComparator(criterion.second()));
          break;
        case MATCH_STATE:
          comparatorList.add(new FunctionTreeNodeMatchStateComparator(criterion.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterion.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterion.second()));
          break;
        case SIDE:
          comparatorList.add(new CombinedTreeNodeSideComparator(criterion.second()));
          break;
        case FUNCTION_TYPE:
          comparatorList.add(new FunctionTreeNodeTypeComparator(criterion.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  public List<Comparator<ISortableTreeNode>> getSingleBasicBlockTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterion, ESortOrder> criterion : criteria) {
      switch (criterion.first()) {
        case ADDRESS:
          comparatorList.add(new SingleTreeNodeAdressComparator(criterion.second()));
          break;
        case MATCH_STATE:
          comparatorList.add(new BasicBlockTreeNodeMatchStateComparator(criterion.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterion.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterion.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  public List<Comparator<ISortableTreeNode>> getSingleFunctionTreeNodeComparatorList() {
    final List<Comparator<ISortableTreeNode>> comparatorList = new ArrayList<>();

    for (final Pair<ESortByCriterion, ESortOrder> criterion : criteria) {
      switch (criterion.first()) {
        case ADDRESS:
          comparatorList.add(new CombinedTreeNodeAdressComparator(criterion.second()));
          break;
        case MATCH_STATE:
          comparatorList.add(new FunctionTreeNodeMatchStateComparator(criterion.second()));
          break;
        case VISIBILITY:
          comparatorList.add(new TreeNodeVisibilityComparator(criterion.second()));
          break;
        case SELECTION:
          comparatorList.add(new TreeNodeSelectionComparator(criterion.second()));
          break;
        case FUNCTION_TYPE:
          comparatorList.add(new FunctionTreeNodeTypeComparator(criterion.second()));
          break;
        case FUNCTION_NAME:
          comparatorList.add(new FunctionTreeNodeNameComparator(criterion.second()));
          break;
        default:
      }
    }

    Collections.reverse(comparatorList);

    return comparatorList;
  }

  @Override
  public Iterator<Pair<ESortByCriterion, ESortOrder>> iterator() {
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

  public void setCriterion(
      final ESortByCriterion sortBy,
      final ESortOrder order,
      final int criterionDepth,
      final boolean notify) {
    if (criterionDepth < 0 || criterionDepth > MAX_DEPTH) {
      logger.atSevere().log("Criterion depth is out of range");

      return;
    }

    if (criteria.get(criterionDepth).first() != sortBy
        || criteria.get(criterionDepth).second() != order) {
      criteria.remove(criterionDepth);

      criteria.add(criterionDepth, new Pair<>(sortBy, order));

      if (notify) {
        notifyListeners();
      }
    }
  }
}
