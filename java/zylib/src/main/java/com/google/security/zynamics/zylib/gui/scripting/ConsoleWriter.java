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

package com.google.security.zynamics.zylib.gui.scripting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.google.security.zynamics.zylib.general.ListenerProvider;


public class ConsoleWriter extends PrintWriter implements IScriptConsole {
  private final StringWriter m_writer;

  private final ListenerProvider<IScriptConsoleListener> m_listeners =
      new ListenerProvider<IScriptConsoleListener>();

  /** Attribute needed by Jython 2.5 */
  public boolean softspace = false;

  public ConsoleWriter(final StringWriter writer) {
    super(writer);

    m_writer = writer;
  }

  @Override
  public void addListener(final IScriptConsoleListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public String getOutput() {
    return m_writer.toString();
  }

  public String getOutputAndClearBuffer() {
    final String currentBuffer = getOutput();
    m_writer.getBuffer().delete(0, m_writer.getBuffer().length());
    return currentBuffer;
  }

  @Override
  public Writer getWriter() {
    return this;
  }

  @Override
  public void removeListener(final IScriptConsoleListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void write(final String s) {
    super.write(s);

    for (final IScriptConsoleListener listener : m_listeners) {
      try {
        listener.changedOutput(this);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }
}
