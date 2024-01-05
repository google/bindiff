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

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

public class ZyRegenerateableNodeRealizer implements IZyRegenerateableRealizer {
  private final IZyNodeRealizer realizer;

  public ZyRegenerateableNodeRealizer(final IZyNodeRealizer realizer) {
    this.realizer = Preconditions.checkNotNull(realizer, "Error: Node realizer can't be null.");
  }

  @Override
  public void regenerate() {
    realizer.regenerate();
  }

  @Override
  public void repaint() {
    realizer.repaint();
  }
}
