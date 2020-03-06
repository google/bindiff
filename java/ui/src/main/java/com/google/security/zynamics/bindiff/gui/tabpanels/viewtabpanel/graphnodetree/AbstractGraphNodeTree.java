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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractRootTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

public abstract class AbstractGraphNodeTree extends JTree {
  private InternalMouseListener mouseListener = new InternalMouseListener();

  private InternalGraphSelectionListener graphSelectionListener =
      new InternalGraphSelectionListener();

  private InternalGraphVisibilityListener graphVisibilityListener =
      new InternalGraphVisibilityListener();

  private boolean updateFrozen = false;

  private void handleMouseEvent(final MouseEvent event) {
    final AbstractTreeNode selectedNode =
        (AbstractTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.handleMouseEvent(event);
  }

  public void addListeners() {
    getGraph().getIntermediateListeners().addIntermediateListener(graphSelectionListener);
    getGraph().getIntermediateListeners().addIntermediateListener(graphVisibilityListener);

    addMouseListener(mouseListener);
  }

  public void dispose() {
    getGraph().getIntermediateListeners().removeIntermediateListener(graphSelectionListener);
    getGraph().getIntermediateListeners().removeIntermediateListener(graphVisibilityListener);

    removeMouseListener(mouseListener);

    graphSelectionListener = null;
    graphVisibilityListener = null;
    mouseListener = null;

    getRootNode().dispose();
  }

  public abstract BinDiffGraph<?, ?> getGraph();

  @Override
  public DefaultTreeModel getModel() {
    return (DefaultTreeModel) super.getModel();
  }

  public AbstractRootTreeNode getRootNode() {
    return (AbstractRootTreeNode) getModel().getRoot();
  }

  public void setUpdateFrozen(final boolean freeze) {
    updateFrozen = freeze;
  }

  @Override
  public void updateUI() {
    if (!updateFrozen) {
      super.updateUI();
    }
  }

  private class InternalGraphSelectionListener implements IZyGraphSelectionListener {
    @Override
    public void selectionChanged() {
      AbstractGraphNodeTree.this.updateUI();
    }
  }

  private class InternalGraphVisibilityListener implements IZyGraphVisibilityListener {
    @Override
    public void nodeDeleted() {
      AbstractGraphNodeTree.this.updateUI();
    }

    @Override
    public void visibilityChanged() {
      AbstractGraphNodeTree.this.updateUI();
    }
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      handleMouseEvent(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      handleMouseEvent(event);
    }
  }
}
