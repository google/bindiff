// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.nodecolor;

import com.google.security.zynamics.bindiff.graph.AbstractGraphsContainer;
import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;
import com.google.security.zynamics.zylib.gui.ColorPanel.IColorPanelListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ColorCriterionPanel extends JPanel {
  private final ColorPanel selectedColorPanel;
  private final ColorCriterion colorCriterion;
  private final List<ColorPanel> colorPanels = new ArrayList<>();
  private final InternalColorPanelListener colorPanelListener = new InternalColorPanelListener();

  public ColorCriterionPanel(final ColorCriterion colorCriterion) {
    super(new BorderLayout());

    this.colorCriterion = colorCriterion;

    selectedColorPanel = new ColorPanel(null, false);
    selectedColorPanel.addListener(colorPanelListener);
    selectedColorPanel.addMouseListener(colorPanelListener);
  }

  private void createPanel(final List<Color> colors) {
    removeAll();

    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Color Condition"));

    final JPanel selectedColorPanel = new JPanel(new BorderLayout());
    selectedColorPanel.setBorder(new EmptyBorder(0, 5, 3, 5));
    selectedColorPanel.add(selectedColorPanel);

    final JPanel colorGrid = new JPanel(new GridLayout(1 + colors.size() / 4, 4));
    colorGrid.setBorder(new TitledBorder(""));

    for (final Color c : colors) {
      final JPanel colorPanel = new JPanel(new BorderLayout());
      colorPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

      final ColorPanel cp = new ColorPanel(c, false);
      colorPanel.add(cp, BorderLayout.CENTER);

      colorPanels.add(cp);

      cp.addListener(colorPanelListener);
      cp.addMouseListener(colorPanelListener);

      colorGrid.add(colorPanel, BorderLayout.NORTH);
    }

    this.selectedColorPanel.setColor(colors.size() > 0 ? colors.get(0) : Color.WHITE);

    mainPanel.add(selectedColorPanel, BorderLayout.NORTH);

    final JPanel gridContainer = new JPanel(new BorderLayout());
    gridContainer.add(colorGrid, BorderLayout.NORTH);
    gridContainer.setBorder(new EmptyBorder(3, 5, 0, 5));

    mainPanel.add(gridContainer, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);
  }

  private void getColors(
      final Set<Color> colors, final AbstractZyGraph<? extends ZyGraphNode<?>, ?> graph) {
    for (final ZyGraphNode<?> node : graph.getNodes()) {
      colors.add(node.getRealizer().getFillColor());
    }
  }

  public void delete() {
    selectedColorPanel.removeListener(colorPanelListener);
    selectedColorPanel.removeMouseListener(colorPanelListener);

    for (final ColorPanel cp : colorPanels) {
      cp.removeListener(colorPanelListener);
      cp.removeMouseListener(colorPanelListener);
    }
  }

  public Color getColor() {
    return selectedColorPanel.getColor();
  }

  public void updateColors(final AbstractGraphsContainer graphs) {
    final Set<Color> colors = new HashSet<>();

    getColors(colors, graphs.getPrimaryGraph());
    getColors(colors, graphs.getSecondaryGraph());
    getColors(colors, graphs.getCombinedGraph());

    createPanel(new ArrayList<>(colors));
  }

  private class InternalColorPanelListener extends MouseAdapter implements IColorPanelListener {
    @Override
    public void changedColor(final ColorPanel panel) {
      colorCriterion.update();
    }

    @Override
    public void mousePressed(final MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
        final ColorPanel panel = (ColorPanel) e.getSource();

        final Color color = panel.getColor();

        if (color != null) {
          selectedColorPanel.setColor(color);
        }
      }
    }
  }
}
