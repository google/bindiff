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

package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawCombinedBasicBlock extends CombinedViewNode {
  private final BasicBlockMatchData basicblockMatch;

  private final RawBasicBlock primaryBasicblock;

  private final RawBasicBlock secondaryBasicblock;

  private final IAddress primaryFunctionAddr;

  private final IAddress secondaryFunctionAddr;

  public RawCombinedBasicBlock(
      final RawBasicBlock primaryBasicblock,
      final RawBasicBlock secondaryBasicblock,
      final BasicBlockMatchData basicblockMatch,
      final IAddress primaryFunctionAddr,
      final IAddress secondaryFunctionAddr) {
    super();

    if (primaryBasicblock == null && secondaryBasicblock == null) {
      throw new IllegalArgumentException(
          "Primary basic block and secondary basic block cannot both be null.");
    }
    if (primaryFunctionAddr == null && secondaryFunctionAddr == null) {
      throw new IllegalArgumentException(
          "Primary and secondary function address cannot both be null.");
    }
    this.primaryBasicblock = primaryBasicblock;
    this.secondaryBasicblock = secondaryBasicblock;
    this.basicblockMatch = basicblockMatch;
    this.primaryFunctionAddr = primaryFunctionAddr;
    this.secondaryFunctionAddr = secondaryFunctionAddr;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    if (getRawNode(side) == null) {
      return null;
    }

    return getRawNode(side).getAddress();
  }

  public BasicBlockMatchData getBasicblockMatch() {
    return basicblockMatch;
  }

  public IAddress getPrimaryFunctionAddress() {
    return primaryFunctionAddr;
  }

  @Override
  public RawBasicBlock getRawNode(final ESide side) {
    if (side == ESide.PRIMARY) {
      return primaryBasicblock;
    }

    return secondaryBasicblock;
  }

  public IAddress getSecondaryFunctionAddress() {
    return secondaryFunctionAddr;
  }
}
