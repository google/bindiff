package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class CopyBasicBlockAddressAction extends AbstractAction {
  private final SingleDiffNode node;

  public CopyBasicBlockAddressAction(final CombinedDiffNode node, final ESide side) {
    super(
        side == ESide.PRIMARY
            ? "Copy Primary Basic Block Address"
            : "Copy Secondary Basic Block Address");

    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(side);

    this.node = side == ESide.PRIMARY ? node.getPrimaryDiffNode() : node.getSecondaryDiffNode();
    Preconditions.checkNotNull(this.node);
  }

  public CopyBasicBlockAddressAction(final SingleDiffNode node) {
    super("Copy Basic Block Address");

    this.node = Preconditions.checkNotNull(node);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final RawBasicBlock rawNode = (RawBasicBlock) node.getRawNode();
    ClipboardHelpers.copyToClipboard(rawNode.getAddress().toHexString());
  }
}
