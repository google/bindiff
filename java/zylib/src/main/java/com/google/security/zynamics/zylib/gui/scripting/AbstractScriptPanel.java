// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.scripting;

import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.scripting.console.ConsoleStdoutDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.script.ScriptEngineManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public abstract class AbstractScriptPanel extends JPanel implements IScriptPanel {
  private static final long serialVersionUID = 8553550256320119608L;

  private final ScriptEngineManager manager = new ScriptEngineManager();

  private final JTextPane m_inputPane = new JTextPane();

  private final JTextPane m_OutputPane = new JTextPane();

  private String m_progressWindowTitle;

  private final ConsoleStdoutDocument m_PythonStdoutDocument = new ConsoleStdoutDocument();

  private final ArrayList<Pair<String, Object>> bindings = new ArrayList<Pair<String, Object>>();

  private final LanguageBox languageBox;

  public AbstractScriptPanel(final LayoutManager layout) {
    super(layout);

    languageBox = new LanguageBox(getManager());
    languageBox.addActionListener(new InternalLanguageBoxListener());

    m_inputPane.setEditable(true);
    m_inputPane.setBackground(new Color((float) .97, (float) .97, 1));
    m_inputPane.setFont(GuiHelper.getMonospacedFont());

    m_OutputPane.setDocument(m_PythonStdoutDocument);

    m_OutputPane.setEditable(false);
    m_OutputPane.setBackground(new Color((float) .97, (float) .97, 1));
    final JScrollPane inputScrollPane = new JScrollPane(m_inputPane);

    final TitledBorder inputAreaBorder =
        new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Command Line");
    inputScrollPane.setBorder(inputAreaBorder);
    inputScrollPane.setPreferredSize(new Dimension(600, 200));

    final JScrollPane m_OutputScrollPane = new JScrollPane(m_OutputPane);

    final TitledBorder outputAreaBorder =
        new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Output");
    m_OutputScrollPane.setBorder(outputAreaBorder);
    m_OutputScrollPane.setPreferredSize(new Dimension(600, 200));

    add(languageBox, BorderLayout.NORTH);
    add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, inputScrollPane, m_OutputScrollPane));

  }

  @SuppressWarnings("unchecked")
  protected ArrayList<Pair<String, Object>> getBindings() {
    return (ArrayList<Pair<String, Object>>) bindings.clone();
  }

  protected JTextPane getInputPane() {
    return m_inputPane;
  }

  protected ScriptEngineManager getManager() {
    return manager;
  }

  protected ConsoleStdoutDocument getOutputDocument() {
    return m_PythonStdoutDocument;
  }

  protected JTextPane getOutputPane() {
    return m_OutputPane;
  }

  protected String getProgressWindowTitle() {
    return m_progressWindowTitle;
  }

  protected String getSelectedLanguage() {
    return languageBox.getSelectedLanguage();
  }

  protected abstract void initConsole();

  protected abstract void updateDocument();

  public void addBinding(final String key, final Object value) {
    bindings.add(new Pair<String, Object>(key, value));
  }

  public void setLanguage(final String language) {
    languageBox.setSelectedLanguage(language);
  }

  @Override
  public void setOutput(final String output) {
    m_OutputPane.setText(output);
  }

  public void setProgressWindowTitle(final String title) {
    m_progressWindowTitle = title;
  }

  private class InternalLanguageBoxListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent arg0) {
      initConsole();
      updateDocument();
    }
  }
}
