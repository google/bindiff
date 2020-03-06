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

package com.google.security.zynamics.bindiff.graph.searchers;

import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Comparator;

public class SearchResultComparator implements Comparator<SearchResult> {
  @Override
  public int compare(final SearchResult sr1, final SearchResult sr2) {
    final Object obj1 = sr1.getObject();
    final Object obj2 = sr2.getObject();

    if (obj1 == obj2) {
      if (sr1.getLine() > sr2.getLine()) {
        return 1;
      } else if (sr1.getLine() < sr2.getLine()) {
        return -1;
      }

      return 0;
    }

    IAddress addr1 = null;
    IAddress addr2 = null;

    if (obj1 instanceof SingleDiffNode) {
      final SingleViewNode viewNode = ((SingleDiffNode) obj1).getRawNode();
      addr1 = viewNode.getAddress();
    } else if (obj1 instanceof SingleDiffEdge) {
      final SingleViewEdge<?> viewEdge = ((SingleDiffEdge) obj1).getRawEdge();
      final SingleViewNode sourceNode = viewEdge.getSource();
      addr1 = sourceNode.getAddress();
    }

    if (obj2 instanceof SingleDiffNode) {
      final SingleViewNode viewNode = ((SingleDiffNode) obj2).getRawNode();
      addr2 = viewNode.getAddress();
    } else if (obj2 instanceof SingleDiffEdge) {
      final SingleViewEdge<?> viewEdge = ((SingleDiffEdge) obj2).getRawEdge();
      final SingleViewNode sourceNode = viewEdge.getSource();
      addr2 = sourceNode.getAddress();
    }

    if (addr1 == null || addr2 == null) {
      throw new IllegalStateException("Address cannot be null");
    }

    return addr1.compareTo(addr2);
  }
}
