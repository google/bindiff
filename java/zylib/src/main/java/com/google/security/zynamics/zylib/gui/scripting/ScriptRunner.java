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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.io.FileUtils;

public class ScriptRunner {
  private static final ScriptEngineManager manager = new ScriptEngineManager();

  /**
   * Determines whether a file has the file extension of a valid script file.
   * 
   * @param file
   * @return true if the there is a script engine that can handle the given extension, false
   *         otherwise.
   */
  public static boolean canRun(final File file) {
    return manager.getEngineByExtension(FileUtils.getFileExtension(file)) != null;
  }

  /**
   * Executes a Python script.
   * 
   * Note that Jython does not honor stdout redirection automatically. If you don't want the output
   * of the Python script to go to stdout you have to set this up manually. To do this you add a
   * "ZYLIB_CONSOLE" => PythonWriter binding. In your script you can then do the following:
   * 
   * import sys sys.stdout = ZYLIB_CONSOLE
   * 
   * After execution of the Python script you can get the console output from the PythonWriter
   * object.
   * 
   * @param script The script to execute.
   * @param bindings Java object bindings that can be used by the executed script.
   * 
   * @throws ScriptException Thrown if the script caused an exception.
   */
  public static void runPythonScript(final String script, final List<Pair<String, Object>> bindings)
      throws ScriptException {
    final ScriptEngine engine = manager.getEngineByName("python");

    runScript(engine, script, bindings);
  }

  /**
   * Executes a script file. The language of the script is determined by the file extension.
   * 
   * @param file
   * @param bindings
   * @throws ScriptException
   * @throws IOException
   */
  public static void runScript(final File file, final List<Pair<String, Object>> bindings)
      throws ScriptException, IOException {
    final ScriptEngine engine = manager.getEngineByExtension(FileUtils.getFileExtension(file));
    Preconditions.checkNotNull(engine, "Error: Script %s has an unknown extension.",
        file.getAbsolutePath());
    final String script = FileUtils.readTextfile(file);

    runScript(engine, script, bindings);
  }

  public static void runScript(final ScriptEngine engine, final File file,
      final List<Pair<String, Object>> bindings) throws ScriptException, IOException {
    Preconditions.checkNotNull(engine, "Error: Script %s has an unknown extension.",
        file.getAbsolutePath());
    final String script = FileUtils.readTextfile(file);
    runScript(engine, script, bindings);
  }

  public static Object runScript(final ScriptEngine engine, final String script,
      final List<Pair<String, Object>> bindings) throws ScriptException {
    for (final Pair<String, Object> pair : bindings) {
      engine.put(pair.first(), pair.second());
    }

    return engine.eval(script);
  }

  public static void runScript(final String languageName, final String script,
      final List<Pair<String, Object>> bindings, final IScriptConsole console)
      throws ScriptException {
    final ScriptEngine engine = manager.getEngineByName(languageName);

    Preconditions.checkNotNull(engine, "Error: Unknown scripting language");

    engine.getContext().setWriter(console.getWriter());

    runScript(engine, script, bindings);
  }
}
