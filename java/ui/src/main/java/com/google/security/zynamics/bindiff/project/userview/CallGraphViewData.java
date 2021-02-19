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

package com.google.security.zynamics.bindiff.project.userview;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/** Stores metadata for call graph views. */
public class CallGraphViewData extends ViewData {
  private final RawCallGraph primaryRawGraph;
  private final RawCallGraph secondaryRawGraph;

  private final String priImageName;
  private final String secImageName;

  public CallGraphViewData(
      final RawCallGraph primaryRawCallGraph,
      final RawCallGraph secondaryRawCallGraph,
      final GraphsContainer graphs,
      final String viewName,
      final String priImageName,
      final String secImageName,
      final EViewType viewType) {
    super(graphs, viewName, viewType);
    this.primaryRawGraph = checkNotNull(primaryRawCallGraph);
    this.secondaryRawGraph = secondaryRawCallGraph;
    this.priImageName = priImageName;
    this.secImageName = secImageName;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    return null;
  }

  public String getImageName(final ESide side) {
    return side == ESide.PRIMARY ? priImageName : secImageName;
  }

  @Override
  public RawCallGraph getRawGraph(final ESide side) {
    return side == ESide.PRIMARY ? primaryRawGraph : secondaryRawGraph;
  }

  @Override
  public boolean isCallGraphView() {
    return true;
  }

  @Override
  public boolean isFlowGraphView() {
    return false;
  }
}
