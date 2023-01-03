// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectionSnapshot {
  private final ListenerProvider<ISnapshotListener> listeners = new ListenerProvider<>();

  private final List<ZyGraphNode<?>> selectedNodes = new ArrayList<>();

  public SelectionSnapshot(final Collection<ZyGraphNode<?>> selectedNodes) {
    this.selectedNodes.addAll(selectedNodes);
  }

  public void add(final ZyGraphNode<?> diffNodeToAdd) {
    selectedNodes.add(diffNodeToAdd);
    for (final ISnapshotListener listener : listeners) {
      listener.addedNode(diffNodeToAdd);
    }
  }

  public void addListener(final ISnapshotListener snapshotListener) {
    listeners.addListener(snapshotListener);
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof SelectionSnapshot)) {
      return false;
    }
    return ((SelectionSnapshot) rhs) != null
        && ((SelectionSnapshot) rhs).selectedNodes.equals(selectedNodes);
  }

  public Collection<CombinedDiffNode> getCombinedGraphSelection() {
    final Collection<CombinedDiffNode> selectedNodes = new ArrayList<>();

    for (final ZyGraphNode<?> node : this.selectedNodes) {
      if (node instanceof CombinedDiffNode) {
        selectedNodes.add((CombinedDiffNode) node);
      }
    }

    return selectedNodes;
  }

  public String getDescription() {
    String title = "No Selection";

    if (selectedNodes.size() == 1) {
      String address = "";
      final ZyGraphNode<?> node = selectedNodes.get(0);
      if (node instanceof SingleDiffNode) {
        address = ((SingleDiffNode) node).getRawNode().getAddress().toHexString();
      } else if (node instanceof CombinedDiffNode) {
        final CombinedDiffNode combinedNode = (CombinedDiffNode) node;
        final SingleViewNode priNode = combinedNode.getPrimaryRawNode();
        final SingleViewNode secNode = combinedNode.getSecondaryRawNode();

        address = priNode == null ? "missing" : priNode.getAddress().toHexString();
        address += " - ";
        address += secNode == null ? "missing" : secNode.getAddress().toHexString();
      }

      title = String.format("Single Selection (%s)", address);
    } else if (selectedNodes.size() > 1) {
      title = "Group Selection";
    }

    return title;
  }

  public int getNumberOfSelectedNodes() {
    return selectedNodes.size();
  }

  public Collection<ZyGraphNode<?>> getSelection() {
    return selectedNodes;
  }

  public Collection<SingleDiffNode> getSingleGraphSelection() {
    final Collection<SingleDiffNode> selectedNodes = new ArrayList<>();

    for (final ZyGraphNode<?> node : this.selectedNodes) {
      if (node instanceof SingleDiffNode) {
        selectedNodes.add((SingleDiffNode) node);
      }
    }

    return selectedNodes;
  }

  @Override
  public int hashCode() {
    return selectedNodes.hashCode();
  }

  public void modicationFinished() {
    for (final ISnapshotListener listener : listeners) {
      listener.finished();
    }
  }

  public void remove(final ZyGraphNode<?> diffNodeToRemove) {
    selectedNodes.remove(diffNodeToRemove);
    for (final ISnapshotListener listener : listeners) {
      listener.removedNode(diffNodeToRemove);
    }
  }

  public void removeListener(final ISnapshotListener snapshotListener) {
    listeners.removeListener(snapshotListener);
  }
}
