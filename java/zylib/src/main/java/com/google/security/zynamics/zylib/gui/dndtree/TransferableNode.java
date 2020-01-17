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
