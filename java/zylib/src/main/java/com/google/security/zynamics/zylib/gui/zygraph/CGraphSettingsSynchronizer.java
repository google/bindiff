// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettingsListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;

public class CGraphSettingsSynchronizer {
  // TODO: Rename or displace functionality. The only job of this class is to enable and disable the
  // magnifying glass

  private final AbstractZyGraphSettings m_settings;
  private final ZyEditMode<?, ?> m_editMode;

  private final InternalSettingsListener m_settingsListener = new InternalSettingsListener();

  public CGraphSettingsSynchronizer(
      final ZyEditMode<?, ?> editMode, final AbstractZyGraphSettings settings) {
    m_editMode = editMode;
    m_settings = settings;

    m_settings.getDisplaySettings().addListener(m_settingsListener);
  }

  public void dispose() {
    m_settings.getDisplaySettings().removeListener(m_settingsListener);
  }

  private class InternalSettingsListener implements IDisplaySettingsListener {
    @Override
    public void changedMagnifyingGlass(final boolean enabled) {
      m_editMode.setMagnifyingMode(enabled);
    }
  }
}
