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

package com.google.security.zynamics.bindiff.graph.sorters;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultSorter {

  // RESULT (Sort by Primary):

  // null 1 j1
  // null 2 j2
  // i1 A 4 j4 <- first iteration store index (fallback point)
  // i2 B null
  // null 5 j5
  // i3 C 6 j6
  // i4 D 3 j3
  // null 7

  private static Map<Pair<IAddress, IAddress>, Object> getAddrPairToObjectMap(
      final Collection<Object> resultObjects) {
    final Map<Pair<IAddress, IAddress>, Object> addrPairToObj = new HashMap<>();

    for (final Object obj : resultObjects) {
      IAddress primaryAddr = null;
      IAddress secondaryAddr = null;

      // nodes
      if (obj instanceof SuperDiffNode) {
        // sync mode only
        final SingleViewNode primaryNode = ((SuperDiffNode) obj).getPrimaryRawNode();
        if (primaryNode != null) {
          primaryAddr = primaryNode.getAddress();
        }

        final SingleViewNode secondaryNode = ((SuperDiffNode) obj).getSecondaryRawNode();
        if (secondaryNode != null) {
          secondaryAddr = secondaryNode.getAddress();
        }
      } else if (obj instanceof CombinedDiffNode) {
        // sync and async mode
        final SingleViewNode primaryNode = ((CombinedDiffNode) obj).getPrimaryRawNode();
        if (primaryNode != null) {
          primaryAddr = primaryNode.getAddress();
        }

        final SingleViewNode secondaryNode = ((CombinedDiffNode) obj).getSecondaryRawNode();
        if (secondaryNode != null) {
          secondaryAddr = secondaryNode.getAddress();
        }
      } else if (obj instanceof SingleDiffNode) {
        // async mode only
        final SingleDiffNode singleNode = (SingleDiffNode) obj;

        if (singleNode.getSide() == ESide.PRIMARY) {
          primaryAddr = singleNode.getRawNode().getAddress();
        } else {
          secondaryAddr = singleNode.getRawNode().getAddress();
        }
      }

      // edges (edge labels)
      else if (obj instanceof SuperDiffEdge) {
        // sync mode only
        final SuperDiffEdge superEdge = (SuperDiffEdge) obj;

        final SingleViewNode primaryNode = superEdge.getSource().getPrimaryRawNode();
        if (primaryNode != null) {
          primaryAddr = primaryNode.getAddress();
        }

        final SingleViewNode secondaryNode = superEdge.getSource().getSecondaryRawNode();
        if (secondaryNode != null) {
          secondaryAddr = secondaryNode.getAddress();
        }
      } else if (obj instanceof CombinedDiffEdge) {
        // sync and async mode
        final CombinedDiffEdge combinedEdge = (CombinedDiffEdge) obj;

        final SingleViewNode primaryNode = combinedEdge.getSource().getPrimaryRawNode();
        if (primaryNode != null) {
          primaryAddr = primaryNode.getAddress();
        }

        final SingleViewNode secondaryNode = combinedEdge.getSource().getSecondaryRawNode();
        if (secondaryNode != null) {
          secondaryAddr = secondaryNode.getAddress();
        }

      } else if (obj instanceof SingleDiffEdge) {
        // async mode only
        final SingleDiffEdge singleEdge = (SingleDiffEdge) obj;

        if (singleEdge.getSide() == ESide.PRIMARY) {
          primaryAddr = singleEdge.getSource().getRawNode().getAddress();
        } else {
          secondaryAddr = singleEdge.getSource().getRawNode().getAddress();
        }
      }

      final Pair<IAddress, IAddress> addrPair = new Pair<>(primaryAddr, secondaryAddr);

      addrPairToObj.put(addrPair, obj);
    }

    return addrPairToObj;
  }

  public static List<Object> getSortedList(
      final Collection<Object> resultObjectList, final ESide sortBy) {
    // to fill up
    final List<Object> sortedObjects = new ArrayList<>();

    // get addr pair to object map
    final Map<Pair<IAddress, IAddress>, Object> addrPairToObj;
    addrPairToObj = getAddrPairToObjectMap(resultObjectList);

    // get sorted addr pairs
    final List<Pair<IAddress, IAddress>> sortedAddrPairs;
    sortedAddrPairs = AddressPairSorter.getSortedList(addrPairToObj.keySet(), sortBy);

    // fill up sorted object
    for (final Pair<IAddress, IAddress> addrPair : sortedAddrPairs) {
      final Object obj = addrPairToObj.get(addrPair);
      sortedObjects.add(obj);
    }

    return sortedObjects;
  }
}
