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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriterionTreeNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CriterionExecutor {
  private CriterionExecutor() {}

  public static Set<ZyGraphNode<? extends CViewNode<?>>> execute(
      final CriterionTree tree,
      final BinDiffGraph<? extends ZyGraphNode<? extends CViewNode<?>>, ?> graph) {
    return new HashSet<>(CollectionHelpers.filter(graph.getNodes(), new CriterionFilter(tree)));
  }

  private static class CriterionFilter
      implements ICollectionFilter<ZyGraphNode<? extends CViewNode<?>>> {
    private final CriterionTree tree;

    public CriterionFilter(final CriterionTree tree) {
      this.tree = tree;
    }

    private boolean qualifies(
        final ICriterionTreeNode node, final ZyGraphNode<? extends CViewNode<?>> item) {
      final List<ICriterionTreeNode> children = node.getChildren();

      if (node.equals(tree.getRoot())) {
        if (children.size() != 1) {
          throw new IllegalStateException("Root node has more or less than one child criterion.");
        }

        return qualifies(children.get(0), item);
      }

      if (node.getCriterion().getType() == CriterionType.AND) {
        if (children.size() < 2) {
          throw new IllegalStateException("AND operator has less than two child criteria.");
        }

        for (final ICriterionTreeNode child : node.getChildren()) {
          if (!qualifies(child, item)) {
            return false;
          }
        }

        return true;
      }

      if (node.getCriterion().getType() == CriterionType.OR) {
        if (children.size() < 2) {
          throw new IllegalStateException("AND operator has less than two child criteria.");
        }

        for (final ICriterionTreeNode child : node.getChildren()) {
          if (qualifies(child, item)) {
            return true;
          }
        }

        return false;
      }

      if (node.getCriterion().getType() == CriterionType.NOT) {
        if (children.size() != 1) {
          throw new IllegalStateException(
              "NOT operator has more or less than one child criterion.");
        }

        return !qualifies(children.get(0), item);
      }

      if (node instanceof CriterionTreeNode) {
        final CriterionTreeNode cnode = (CriterionTreeNode) node;
        return cnode.getCriterion().matches(item);
      }

      throw new IllegalStateException("Unknown criterion.");
    }

    @Override
    public boolean qualifies(final ZyGraphNode<? extends CViewNode<?>> item) {
      return qualifies(tree.getRoot(), item);
    }
  }
}
