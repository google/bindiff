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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public abstract class AbstractTreeTransferHandler implements DragGestureListener,
    DragSourceListener, DropTargetListener {

  private static DefaultMutableTreeNode draggedNode;
  private static BufferedImage image = null; // buff image
  private final DNDTree tree;
  private final DragSource dragSource; // dragsource
  private final DropTarget dropTarget; // droptarget
  private DefaultMutableTreeNode draggedNodeParent;
  private final Rectangle rect2D = new Rectangle();
  private final boolean drawImage;

  protected AbstractTreeTransferHandler(final DNDTree tree, final int action, final boolean drawIcon) {
    this.tree = tree;
    drawImage = drawIcon;
    dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(tree, action, this);
    dropTarget = new DropTarget(tree, action, this);
  }

  private final void clearImage() {
    tree.paintImmediately(rect2D.getBounds());
  }

  private final void paintImage(final Point pt) {
    if (image != null) {
      tree.paintImmediately(rect2D.getBounds());
      rect2D.setRect((int) pt.getX(), (int) pt.getY(), image.getWidth(), image.getHeight());
      tree.getGraphics().drawImage(image, (int) pt.getX(), (int) pt.getY(), tree);
    }
  }

  protected abstract boolean canPerformAction(DNDTree tree, DataFlavor flavor,
      Transferable tranferable, int action, Point pt);

  protected abstract boolean executeDrop(DNDTree tree, Transferable transferable,
      DefaultMutableTreeNode newParentNode, int action);

  public abstract boolean canPerformAction(DNDTree target, DefaultMutableTreeNode draggedNode,
      int action, Point location);

  /* Methods for DragSourceListener */
  @Override
  public void dragDropEnd(final DragSourceDropEvent dsde) {
    if (dsde.getDropSuccess() && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)
        && (draggedNodeParent != null)) {
      ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(draggedNodeParent);
    }
  }

  /* Methods for DropTargetListener */
  @Override
  public final void dragEnter(final DragSourceDragEvent dsde) {
    final int action = dsde.getDropAction();
    if (action == DnDConstants.ACTION_COPY) {
      dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
    } else {
      if (action == DnDConstants.ACTION_MOVE) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      } else {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      }
    }
  }

  @Override
  public final void dragEnter(final DropTargetDragEvent dtde) {
    final Point pt = dtde.getLocation();
    final int action = dtde.getDropAction();
    if (drawImage) {
      paintImage(pt);
    }

    final Transferable transferable = dtde.getTransferable();

    if (!transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR)) {
      if (canPerformAction(tree, dtde.getCurrentDataFlavorsAsList().get(0), dtde.getTransferable(),
          action, pt)) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    } else {
      if (canPerformAction(tree, draggedNode, action, pt)) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    }
  }

  @Override
  public final void dragExit(final DragSourceEvent dse) {
    dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  @Override
  public final void dragExit(final DropTargetEvent dte) {
    if (drawImage) {
      clearImage();
    }
  }

  /* Methods for DragGestureListener */
  @Override
  public final void dragGestureRecognized(final DragGestureEvent dge) {
    // final TreePath path = tree.getSelectionPath();
    final TreePath path = tree.getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);

    if (path != null) {
      draggedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
      draggedNodeParent = (DefaultMutableTreeNode) draggedNode.getParent();
      if (drawImage) {
        final Rectangle pathBounds = tree.getPathBounds(path); // getpathbounds of selectionpath
        final JComponent lbl =
            (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode,
                false, tree.isExpanded(path),
                ((DefaultTreeModel) tree.getModel()).isLeaf(path.getLastPathComponent()), 0, false);// returning
                                                                                                    // the
                                                                                                    // label
        lbl.setBounds(pathBounds);// setting bounds to lbl
        image =
            new BufferedImage(lbl.getWidth(), lbl.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);// buffered image reference passing
                                                                // the label's ht and width
        final Graphics2D graphics = image.createGraphics();// creating the graphics for buffered
                                                           // image
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Sets
                                                                                          // the
                                                                                          // Composite
                                                                                          // for the
                                                                                          // Graphics2D
                                                                                          // context
        lbl.setOpaque(false);
        lbl.paint(graphics); // painting the graphics to label
        graphics.dispose();
      }
      dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0),
          new TransferableNode(draggedNode), this);
    }
  }

  @Override
  public final void dragOver(final DragSourceDragEvent dsde) {
    final int action = dsde.getDropAction();
    if (action == DnDConstants.ACTION_COPY) {
      dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
    } else {
      if (action == DnDConstants.ACTION_MOVE) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      } else {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      }
    }
  }

  @Override
  public final void dragOver(final DropTargetDragEvent dtde) {
    final Point pt = dtde.getLocation();
    final int action = dtde.getDropAction();
    tree.autoscroll(pt);
    if (drawImage) {
      paintImage(pt);
    }

    final Transferable transferable = dtde.getTransferable();

    if (!transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR)) {
      if (canPerformAction(tree, dtde.getCurrentDataFlavorsAsList().get(0), dtde.getTransferable(),
          action, pt)) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    } else {
      if (canPerformAction(tree, draggedNode, action, pt)) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    }
  }

  @Override
  public final void drop(final DropTargetDropEvent dtde) {
    if (drawImage) {
      clearImage();
    }
    final int action = dtde.getDropAction();
    final Transferable transferable = dtde.getTransferable();
    final Point pt = dtde.getLocation();
    if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR)
        && canPerformAction(tree, draggedNode, action, pt)) {
      boolean gotData = false;
      DefaultMutableTreeNode node = null;
      try {
        node = (DefaultMutableTreeNode) transferable.getTransferData(TransferableNode.NODE_FLAVOR);
        gotData = true;
      } catch (final IOException e) {
        // TODO: This should be properly logged
        System.out.println(e);
      } catch (final UnsupportedFlavorException e) {
        // TODO: This should be properly logged
        System.out.println(e);
      }
      if (gotData) {
        final TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
        final DefaultMutableTreeNode newParentNode =
            (DefaultMutableTreeNode) pathTarget.getLastPathComponent();

        if (executeDrop(tree, node, newParentNode, action)) {
          dtde.acceptDrop(action);
          dtde.dropComplete(true);
          return;
        }
      }
    } else if (canPerformAction(tree, dtde.getCurrentDataFlavors()[0], dtde.getTransferable(),
        action, pt)) {
      final TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
      final DefaultMutableTreeNode newParentNode =
          (DefaultMutableTreeNode) pathTarget.getLastPathComponent();

      if (executeDrop(tree, dtde.getTransferable(), newParentNode, action)) {
        dtde.acceptDrop(action);
        dtde.dropComplete(true);
        return;
      }
    }

    dtde.rejectDrop();
    dtde.dropComplete(false);
  }

  @Override
  public final void dropActionChanged(final DragSourceDragEvent dsde) {
    final int action = dsde.getDropAction();
    if (action == DnDConstants.ACTION_COPY) {
      dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
    } else {
      if (action == DnDConstants.ACTION_MOVE) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      } else {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
      }
    }
  }

  @Override
  public final void dropActionChanged(final DropTargetDragEvent dtde) {
    final Point pt = dtde.getLocation();
    final int action = dtde.getDropAction();
    if (drawImage) {
      paintImage(pt);
    }
    if (draggedNode == null) {
      if (canPerformAction(tree, dtde.getCurrentDataFlavorsAsList().get(0), dtde.getTransferable(),
          action, pt)) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    } else {
      if (canPerformAction(tree, draggedNode, action, pt)) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    }
  }

  public abstract boolean executeDrop(DNDTree tree, DefaultMutableTreeNode draggedNode,
      DefaultMutableTreeNode newParentNode, int action);

  public DropTarget getDropTarget() {
    return dropTarget;
  }
}
