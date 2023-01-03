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

package com.google.security.zynamics.zylib.gui.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.JComboBox;

public class LanguageBox extends JComboBox<ScriptingLanguage> {

  public LanguageBox(final ScriptEngineManager manager) {
    fillLanguageBox(manager);
  }

  private void fillLanguageBox(final ScriptEngineManager manager) {
    final List<ScriptEngineFactory> factories = manager.getEngineFactories();

    final List<ScriptingLanguage> languages = new ArrayList<ScriptingLanguage>();

    for (final ScriptEngineFactory factory : factories) {
      // Disable Rhino scripting engine for JavaScript / ECMAScript.
      if (factory.getLanguageName().equals("python")) {
        languages.add(
            new ScriptingLanguage(factory.getLanguageName(), factory.getLanguageVersion()));
      }
    }

    Collections.sort(languages);

    for (final ScriptingLanguage language : languages) {
      addItem(language);
    }
  }

  public String getSelectedLanguage() {
    return getSelectedItem() != null ? ((ScriptingLanguage) getSelectedItem()).getName() : null;
  }

  public void setSelectedLanguage(final String language) {
    for (int i = 0; i < getModel().getSize(); i++) {
      final String element = getItemAt(i).toString();
      if (element.equals(language)) {
        setSelectedIndex(i);
        return;
      }
    }
  }
}
