// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.graph.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.filter.enums.EMatchStateFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESelectionFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESideFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.EVisibilityFilter;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.util.ArrayList;
import java.util.List;

public class GraphNodeMultiFilter {
  private final ListenerProvider<IGraphNodeMultiFilterListener> listeners =
      new ListenerProvider<>();

  private final Diff diff;
  private EMatchStateFilter matchStateFilter;
  private ESelectionFilter selectionFilter;
  private EVisibilityFilter visibilityFilter;
  private ESideFilter sideFilter;
  private IAddress startRangeAddr;
  private IAddress endRangeAddr;
  private ESideFilter addrRangeSide;
  private boolean notify = false;

  private final RawFlowGraph priFlowGraph;
  private final RawFlowGraph secFlowGraph;

  public GraphNodeMultiFilter(
      final Diff diff,
      final RawFlowGraph priFlowGraph,
      final RawFlowGraph secFlowGraph,
      final IAddress startAddr,
      final IAddress endAddr,
      final EMatchStateFilter matchStateFilter,
      final ESelectionFilter selectionFilter,
      final EVisibilityFilter visibilityFilter,
      final ESideFilter sideFilter) {
    this.diff = checkNotNull(diff);
    this.startRangeAddr = checkNotNull(startAddr);
    this.endRangeAddr = checkNotNull(endAddr);
    this.matchStateFilter = checkNotNull(matchStateFilter);
    this.selectionFilter = checkNotNull(selectionFilter);
    this.visibilityFilter = checkNotNull(visibilityFilter);
    this.sideFilter = checkNotNull(sideFilter);

    this.priFlowGraph = priFlowGraph;
    this.secFlowGraph = secFlowGraph;
  }

  private boolean filterAddressRange(final CombinedViewNode node) {
    final IAddress priAddress = node.getAddress(ESide.PRIMARY);
    final IAddress secAddress = node.getAddress(ESide.SECONDARY);

    boolean filter = false;

    final String startRange = startRangeAddr.toHexString();
    final String endRange = endRangeAddr.toHexString();

    if (priAddress != null) {
      final String addr = priAddress.toHexString();

      filter = addr.compareTo(startRange) >= 0 && addr.compareTo(endRange) <= 0;
    }

    if (secAddress != null && !filter) {
      final String addr = secAddress.toHexString();

      filter = addr.compareTo(startRange) >= 0 && addr.compareTo(endRange) <= 0;
    }

    return filter;
  }

  private boolean filterAddressRange(final SingleViewNode node) {
    final String addr = node.getAddress().toHexString();
    final String startRange = startRangeAddr.toHexString();
    final String endRange = endRangeAddr.toHexString();

    return addr.compareTo(startRange) >= 0 && addr.compareTo(endRange) <= 0;
  }

  private boolean filterMatchState(final CombinedViewNode node) {
    if (matchStateFilter == EMatchStateFilter.NONE) {
      return true;
    }

    if (node.getMatchState() == EMatchState.MATCHED) {
      if (matchStateFilter == EMatchStateFilter.MATCHED) {
        return true;
      }

      if (node instanceof RawCombinedBasicBlock) {

        final RawBasicBlock priBasicBlock =
            ((RawCombinedBasicBlock) node).getRawNode(ESide.PRIMARY);
        final RawBasicBlock secBasicBlock =
            ((RawCombinedBasicBlock) node).getRawNode(ESide.SECONDARY);

        final boolean isIdentical =
            MatchesGetter.isIdenticalBasicBlock(diff, priBasicBlock, secBasicBlock);

        if (matchStateFilter == EMatchStateFilter.MATCHED_IDENTICAL && isIdentical) {
          return true;
        } else if (matchStateFilter == EMatchStateFilter.MATCHED_INSTRUCTION_CHANGES
            && !isIdentical) {
          return true;
        }
      } else if (node instanceof RawCombinedFunction) {
        final RawFunction function = ((RawCombinedFunction) node).getRawNode(ESide.PRIMARY);

        final boolean isIdentical = function != null && function.isIdenticalMatch();
        final boolean isInstructionOnlyChanged =
            function != null && function.isChangedInstructionsOnlyMatch();
        final boolean isSturcturalChanged = function != null && function.isChangedStructuralMatch();

        if (matchStateFilter == EMatchStateFilter.MATCHED_IDENTICAL && isIdentical) {
          return true;
        } else if (matchStateFilter == EMatchStateFilter.MATCHED_INSTRUCTION_CHANGES
            && isInstructionOnlyChanged) {
          return true;
        } else if (matchStateFilter == EMatchStateFilter.MATCHED_STRUTURAL_CHANGES
            && isSturcturalChanged) {
          return true;
        }
      }
    } else if (node.getMatchState() != EMatchState.MATCHED
        && matchStateFilter == EMatchStateFilter.UNMATCHED) {
      return true;
    }

    return false;
  }

  private boolean filterMatchState(
      final RawBasicBlock basicBlock,
      final RawFlowGraph priFlowGraph,
      final RawFlowGraph secFlowGraph) {
    if (matchStateFilter == EMatchStateFilter.NONE) {
      return true;
    }

    if (basicBlock.getMatchState() == EMatchState.MATCHED) {
      if (matchStateFilter == EMatchStateFilter.MATCHED) {
        return true;
      }

      final ESide side = basicBlock.getSide();

      RawBasicBlock priBasicBlock = basicBlock;
      RawBasicBlock secBasicBlock = basicBlock;

      final BasicBlockMatchData basicBlockMatch =
          MatchesGetter.getBasicBlockMatch(diff, basicBlock);
      if (side == ESide.PRIMARY) {
        secBasicBlock = secFlowGraph.getBasicBlock(basicBlockMatch.getIAddress(ESide.SECONDARY));
      } else {
        priBasicBlock = priFlowGraph.getBasicBlock(basicBlockMatch.getIAddress(ESide.PRIMARY));
      }

      final boolean isIdentical =
          MatchesGetter.isIdenticalBasicBlock(diff, priBasicBlock, secBasicBlock);

      if (matchStateFilter == EMatchStateFilter.MATCHED_IDENTICAL && isIdentical) {
        return true;
      } else if (matchStateFilter == EMatchStateFilter.MATCHED_INSTRUCTION_CHANGES
          && !isIdentical) {
        return true;
      }
    } else if (basicBlock.getMatchState() != EMatchState.MATCHED
        && matchStateFilter == EMatchStateFilter.UNMATCHED) {
      return true;
    }

    return false;
  }

  private boolean filterMatchState(final RawFunction function) {
    if (matchStateFilter == EMatchStateFilter.NONE) {
      return true;
    }

    if (function.getMatchState() == EMatchState.MATCHED) {
      if (matchStateFilter == EMatchStateFilter.MATCHED) {
        return true;
      }

      final boolean isIdentical = function.isIdenticalMatch();
      final boolean isInstructionOnlyChanged = function.isChangedInstructionsOnlyMatch();
      final boolean isStructuralChanged = function.isChangedStructuralMatch();

      if (matchStateFilter == EMatchStateFilter.MATCHED_IDENTICAL && isIdentical) {
        return true;
      } else if (matchStateFilter == EMatchStateFilter.MATCHED_INSTRUCTION_CHANGES
          && isInstructionOnlyChanged) {
        return true;
      } else if (matchStateFilter == EMatchStateFilter.MATCHED_STRUTURAL_CHANGES
          && isStructuralChanged) {
        return true;
      }
    } else if (function.getMatchState() != EMatchState.MATCHED
        && matchStateFilter == EMatchStateFilter.UNMATCHED) {
      return true;
    }

    return false;
  }

  private boolean filterSelection(final CombinedViewNode node) {
    if (selectionFilter == ESelectionFilter.NONE) {
      return true;
    }

    if (node.isSelected() && selectionFilter == ESelectionFilter.SELECTED) {
      return true;
    }

    if (!node.isSelected() && selectionFilter == ESelectionFilter.UNSELECTED) {
      return true;
    }

    return false;
  }

  private boolean filterSelection(final SingleViewNode node) {
    if (selectionFilter == ESelectionFilter.NONE) {
      return true;
    }

    if (node.isSelected() && selectionFilter == ESelectionFilter.SELECTED) {
      return true;
    }

    if (!node.isSelected() && selectionFilter == ESelectionFilter.UNSELECTED) {
      return true;
    }

    return false;
  }

  private boolean filterSide(final CombinedViewNode node) {
    final IAddress priAddr = node.getAddress(ESide.PRIMARY);
    final IAddress secAddr = node.getAddress(ESide.SECONDARY);

    if (sideFilter == ESideFilter.NONE) {
      return true;
    }

    if (priAddr != null && sideFilter == ESideFilter.PRIMARY) {
      return true;
    }
    if (secAddr != null && sideFilter == ESideFilter.SECONDARY) {
      return true;
    }

    return false;
  }

  private boolean filterSide(final SingleViewNode node) {
    if (sideFilter == ESideFilter.NONE) {
      return true;
    }

    if (node.getSide() == ESide.PRIMARY && sideFilter == ESideFilter.PRIMARY) {
      return true;
    }

    if (node.getSide() == ESide.SECONDARY && sideFilter == ESideFilter.SECONDARY) {
      return true;
    }

    return false;
  }

  private boolean filterVisibility(final CombinedViewNode node) {
    if (visibilityFilter == EVisibilityFilter.NONE) {
      return true;
    }

    if (node.isVisible() && visibilityFilter == EVisibilityFilter.VISIBLE) {
      return true;
    }

    if (!node.isVisible() && visibilityFilter == EVisibilityFilter.INVISIBLE) {
      return true;
    }

    return false;
  }

  private boolean filterVisibility(final SingleViewNode node) {
    if (visibilityFilter == EVisibilityFilter.NONE) {
      return true;
    }

    if (node.isVisible() && visibilityFilter == EVisibilityFilter.VISIBLE) {
      return true;
    }

    if (!node.isVisible() && visibilityFilter == EVisibilityFilter.INVISIBLE) {
      return true;
    }

    return false;
  }

  private void setAddressRangeFilter(final IAddress startAddr, final IAddress endAddr) {
    if (!startRangeAddr.equals(startAddr) || !endRangeAddr.equals(endAddr)) {
      startRangeAddr = startAddr;
      endRangeAddr = endAddr;
      notify = true;
    }
  }

  private void setMatchStateFilter(final EMatchStateFilter filter) {
    if (matchStateFilter != filter) {
      matchStateFilter = filter;
      notify = true;
    }
  }

  private void setSelectionFilter(final ESelectionFilter filter) {
    if (selectionFilter != filter) {
      selectionFilter = filter;
      notify = true;
    }
  }

  private void setSideFilter(final ESideFilter filter) {
    if (filter != sideFilter) {
      sideFilter = filter;
      notify = true;
    }
  }

  private void setVisibilityFilter(final EVisibilityFilter filter) {
    if (visibilityFilter != filter) {
      visibilityFilter = filter;

      notify = true;
    }
  }

  public void addListener(final IGraphNodeMultiFilterListener listener) {
    listeners.addListener(listener);
  }

  public void clearSettings(final boolean notify) {
    matchStateFilter = EMatchStateFilter.NONE;
    selectionFilter = ESelectionFilter.NONE;
    visibilityFilter = EVisibilityFilter.NONE;
    sideFilter = ESideFilter.NONE;

    startRangeAddr = new CAddress(0);
    endRangeAddr = new CAddress(0xFFFFFFFF);

    if (notify) {
      notifyListeners();
    }
  }

  public boolean filterRawBasicBlock(final RawBasicBlock basicBlock) {
    return basicBlock != null
        && filterAddressRange(basicBlock)
        && filterMatchState(basicBlock, priFlowGraph, secFlowGraph)
        && filterSelection(basicBlock)
        && filterVisibility(basicBlock)
        && filterSide(basicBlock);
  }

  public boolean filterRawCombinedBasicBlock(final RawCombinedBasicBlock combinedBasicBlock) {
    return combinedBasicBlock != null
        && filterAddressRange(combinedBasicBlock)
        && filterMatchState(combinedBasicBlock)
        && filterSelection(combinedBasicBlock)
        && filterVisibility(combinedBasicBlock)
        && filterSide(combinedBasicBlock);
  }

  public List<RawCombinedBasicBlock> filterRawCombinedBasicBlocks(
      final List<RawCombinedBasicBlock> basicBlocks) {
    checkNotNull(addrRangeSide);

    final List<RawCombinedBasicBlock> filteredList = new ArrayList<>();

    for (final RawCombinedBasicBlock basicBlock : basicBlocks) {
      if (filterRawCombinedBasicBlock(basicBlock)) {
        filteredList.add(basicBlock);
      }
    }

    return filteredList;
  }

  public boolean filterRawCombinedFunction(final RawCombinedFunction combinedFunction) {
    return combinedFunction != null
        && filterAddressRange(combinedFunction)
        && filterMatchState(combinedFunction)
        && filterSelection(combinedFunction)
        && filterVisibility(combinedFunction)
        && filterSide(combinedFunction);
  }

  public List<RawCombinedFunction> filterRawCombinedFunctions(
      final List<RawCombinedFunction> combinedFunctions) {
    checkNotNull(addrRangeSide);

    final List<RawCombinedFunction> filteredList = new ArrayList<>();

    for (final RawCombinedFunction function : combinedFunctions) {
      if (filterRawCombinedFunction(function)) {
        filteredList.add(function);
      }
    }

    return filteredList;
  }

  public boolean filterRawFunction(final RawFunction function) {
    return function != null
        && filterAddressRange(function)
        && filterMatchState(function)
        && filterSelection(function)
        && filterVisibility(function)
        && filterSide(function);
  }

  public List<RawFunction> filterRawFunctions(final List<RawFunction> functions) {
    final List<RawFunction> filteredList = new ArrayList<>();

    for (final RawFunction function : functions) {
      if (filterRawFunction(function)) {
        filteredList.add(function);
      }
    }

    return filteredList;
  }

  public ESelectionFilter getSelectionFilterValue() {
    return selectionFilter;
  }

  public EVisibilityFilter getVisibilityFilterValue() {
    return visibilityFilter;
  }

  public void notifyListeners() {
    if (notify) {
      for (final IGraphNodeMultiFilterListener listener : listeners) {
        listener.filterChanged(this);
      }

      notify = false;
    }
  }

  public void removeListener(final IGraphNodeMultiFilterListener listener) {
    listeners.removeListener(listener);
  }

  public void setFilter(
      final IAddress startAddr,
      final IAddress endAddr,
      final EMatchStateFilter matchStateFilter,
      final ESelectionFilter selectionFilter,
      final EVisibilityFilter visibilityFilter,
      final ESideFilter sideFilter,
      final boolean notify) {
    setAddressRangeFilter(startAddr, endAddr);
    setMatchStateFilter(matchStateFilter);
    setSelectionFilter(selectionFilter);
    setVisibilityFilter(visibilityFilter);
    setSideFilter(sideFilter);

    if (notify) {
      notifyListeners();
    }
  }
}
