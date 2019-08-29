package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
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

    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(side);

    this.node = side == ESide.PRIMARY ? node.getPrimaryDiffNode() : node.getSecondaryDiffNode();
    Preconditions.checkNotNull(this.node);
  }

  public CopyFunctionAddressAction(final SingleDiffNode node) {
    super("Copy Function Address");

    this.node = Preconditions.checkNotNull(node);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    ClipboardHelpers.copyToClipboard(((RawFunction) node.getRawNode()).getAddress().toHexString());
  }
}
