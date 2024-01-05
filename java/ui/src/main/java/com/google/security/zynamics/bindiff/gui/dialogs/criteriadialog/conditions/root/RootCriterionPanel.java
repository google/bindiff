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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.root;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators.AbstractOperatorPanel;

public class RootCriterionPanel extends AbstractOperatorPanel {
  @Override
  public String getBorderTitle() {
    return "Root Node";
  }

  @Override
  public String getInvalidInfoString() {
    return "Root node needs exactly one child condition or operator.";
  }

  @Override
  public String getValidInfoString() {
    return "Root node is valid.";
  }
}
