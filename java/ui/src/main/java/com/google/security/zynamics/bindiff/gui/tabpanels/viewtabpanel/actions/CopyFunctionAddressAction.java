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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class CopyFunctionAddressAction extends AbstractAction {
  private final SingleDiffNode node;

  public CopyFunctionAddressAction(final CombinedDiffNode node, final ESide side) {
    super(
        side == ESide.PRIMARY
            ? "Copy Primary Function Address"
            : "Copy Secondary Function Address");

    checkNotNull(node);
    checkNotNull(side);

    this.node = side == ESide.PRIMARY ? node.getPrimaryDiffNode() : node.getSecondaryDiffNode();
    checkNotNull(this.node);
  }

  public CopyFunctionAddressAction(final SingleDiffNode node) {
    super("Copy Function Address");

    this.node = checkNotNull(node);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    ClipboardHelpers.copyToClipboard(((RawFunction) node.getRawNode()).getAddress().toHexString());
  }
}
