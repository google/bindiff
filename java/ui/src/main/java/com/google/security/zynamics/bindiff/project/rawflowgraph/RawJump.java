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

package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.security.zynamics.bindiff.enums.EJumpType;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawJump extends SingleViewEdge<RawBasicBlock> {
  final EJumpType jumpType;

  public RawJump(final RawBasicBlock source, final RawBasicBlock target, final EJumpType jumpType) {
    super(source, target);

    this.jumpType = jumpType;
  }

  public EJumpType getJumpType() {
    return jumpType;
  }

  @Override
  public RawBasicBlock getSource() {
    return super.getSource();
  }

  public IAddress getSourceBasicblockAddress() {
    return getSource().getAddress();
  }

  @Override
  public RawBasicBlock getTarget() {
    return super.getTarget();
  }

  public IAddress getTargetBasicblockAddress() {
    return getTarget().getAddress();
  }
}
