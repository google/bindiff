// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.gui.components.closeablebuttontab;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class TabLabel extends JLabel {
  private final JTabbedPane pane;

  private final TabButtonComponent buttonComponent;

  public TabLabel(final JTabbedPane tabbedPane, final TabButtonComponent buttonComponent) {
    checkNotNull(tabbedPane);
    checkNotNull(buttonComponent);

    pane = tabbedPane;
    this.buttonComponent = buttonComponent;
  }

  @Override
  public String getText() {
    if (buttonComponent != null) {
      final int i = pane.indexOfTabComponent(buttonComponent);

      if (i != -1) {
        return pane.getTitleAt(i);
      }
    }

    return null;
  }

  @Override
  public void setText(final String text) {
    if (buttonComponent != null) {
      final int i = pane.indexOfTabComponent(buttonComponent);

      if (i != -1) {
        pane.setTitleAt(i, text);
      }
    }
  }
}
