// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.dndtree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

public class TransferableNode implements Transferable {
  public static final DataFlavor NODE_FLAVOR = new DataFlavor(
      DataFlavor.javaJVMLocalObjectMimeType, "Node");
  private final DefaultMutableTreeNode node;
  private final DataFlavor[] flavors = {NODE_FLAVOR};

  public TransferableNode(final DefaultMutableTreeNode nd) {
    node = nd;
  }

  @Override
  public synchronized Object getTransferData(final DataFlavor flavor)
      throws UnsupportedFlavorException {
    if (flavor == NODE_FLAVOR) {
      return node;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return Arrays.asList(flavors).contains(flavor);
  }
}
