// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettingsListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;

public class CGraphSettingsSynchronizer {
  // TODO: Rename or displace functionality. The only job of this class is to enable and disable the
  // magnifying glass

  private final AbstractZyGraphSettings m_settings;
  private final ZyEditMode<?, ?> m_editMode;

  private final InternalSettingsListener m_settingsListener = new InternalSettingsListener();

  public CGraphSettingsSynchronizer(final ZyEditMode<?, ?> editMode,
      final AbstractZyGraphSettings settings) {
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
