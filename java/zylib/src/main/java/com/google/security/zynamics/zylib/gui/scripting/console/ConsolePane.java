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

package com.google.security.zynamics.zylib.gui.scripting.console;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessProgressDialog;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.IEndlessProgressListener;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.IEndlessProgressModel;
import com.google.security.zynamics.zylib.gui.scripting.AbstractScriptPanel;
import com.google.security.zynamics.zylib.gui.scripting.ConsoleWriter;
import com.google.security.zynamics.zylib.gui.scripting.IScriptConsole;
import com.google.security.zynamics.zylib.gui.scripting.InitStringFactory;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.resources.Constants;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

public class ConsolePane extends AbstractScriptPanel {
  private static final long serialVersionUID = 3114171042368746418L;

  private final KeyListener m_KeyListener = new InterpreterKeyListener();

  private int m_HistoryPosition = 0;

  private final ArrayList<String> m_LineHistory = new ArrayList<String>();

  private String m_SavedTypedLine = "";

  private final TabCompletionManager m_tabCompletion = new TabCompletionManager();

  private final static boolean m_IsMultilineInput = false;

  private int m_LastAutoCompleteChunkLength = 0;

  private String m_CurrentPrompt;

  private String consoleName;

  private String currentLanguage;

  private IScriptConsole console;

  private ArrayList<Pair<String, Object>> realBindings;

  private ScriptEngine engine;

  private String buffered = "";

  private final TreeSet<String> candidates;

  private String m_libraryPath;

  public ConsolePane() {
    super(new BorderLayout());

    updateDocument();

    candidates = new TreeSet<String>(getDocument().getTabCompletionWords());

    // initConsole();

    getInputPane().addKeyListener(m_KeyListener);
    getDocument().setInputKeyListener(m_KeyListener);
    getOutputPane().setFont(GuiHelper.getMonospacedFont());

    setBorder(new EmptyBorder(5, 5, 5, 5));
  }

  private void executeInitializer() {
    if (m_libraryPath == null) {
      return;
    }

    final List<String> extensions = engine.getFactory().getExtensions();

    for (final String extension : extensions) {
      final String initFile = m_libraryPath + File.separator + "init." + extension;

      if (FileUtils.exists(initFile)) {
        try {
          engine.eval(FileUtils.readTextfile(initFile));

          getOutputPane().setText(console.getOutput());
        } catch (final ScriptException e) {
          getOutputPane().setText(e.getLocalizedMessage());
        } catch (final IOException e) {
          getOutputPane().setText(e.getLocalizedMessage());
        }
      }
    }
  }

  private int getCurrentInputLine() {
    return getDocument().getLineStartOffset(getInputPane().getCaretPosition());
  }

  /**
   * Fetch the the line where the caret currently lies.
   *
   * @return a String object with the contents of the line.
   */
  private String getCurrentLine() {
    final int caretPosition = getInputPane().getCaretPosition();
    String currentLine = null;
    final int skip = m_CurrentPrompt.length();

    /**
     * The trim only removes characters in the end. As the prompt is in the beginning this works
     * fine as we only want to remove the trailing newline.
     */
    currentLine = getDocument().getCurrentLine(caretPosition).trim();
    if (skip < currentLine.length()) {
      currentLine = currentLine.substring(skip, currentLine.length());
    } else {
      currentLine = "";
    }

    return currentLine;
  }

  private ConsoleCodeDocument getDocument() {
    return (ConsoleCodeDocument) getInputPane().getDocument();
  }

  private int getLastInputLine() {
    return getDocument().getLineStartOffset(getInputPane().getText().length());
  }

  /**
   * Set the caret to the end of the pane's text.
   */
  private void goToLastLine() {
    getInputPane().setCaretPosition(getInputPane().getText().length());
  }

  /**
   * Add a new empty interpreter line with the corresponding prompt.
   *
   * @params additionalInput if the input is multi line this can be set to true in order to change
   *         the prompt to indicate the need of additional input
   *
   */
  private void interpreterNewLine(final boolean additionalInput) {
    final int position = getInputPane().getCaretPosition();

    int currentPosition;

    // First invokation
    if (position == 0) {
      currentPosition = 0;
    } else {
      getDocument().insertChar(position, "\n");
      currentPosition = position + 1;
    }

    m_CurrentPrompt = additionalInput ? "... " : ">>> ";
    getDocument().insertPrompt(currentPosition, m_CurrentPrompt);
    getInputPane().setCaretPosition(currentPosition + m_CurrentPrompt.length());
  }

  /**
   * Set the text in the last interpreter prompt.
   *
   * @param line String object with the line to set
   */
  private void setCurrentLine(final String line) {
    goToLastLine();
    getDocument().setCurrentLine(getInputPane().getCaretPosition(), m_CurrentPrompt.length(), line);
  }

  @Override
  protected void initConsole() {
    currentLanguage = getSelectedLanguage();

    console = new ConsoleWriter(new StringWriter());

    buffered = InitStringFactory.getInitString(currentLanguage, consoleName, m_libraryPath);

    realBindings = getBindings();

    if (consoleName != null) {
      realBindings.add(new Pair<String, Object>(consoleName, console));
    }

    if (currentLanguage == null) {
      return;
    }

    engine = getManager().getEngineByName(currentLanguage);

    Preconditions.checkNotNull(engine, "Error: Unknown scripting language");

    engine.getContext().setWriter(console.getWriter());

    for (final Pair<String, Object> pair : realBindings) {
      engine.put(pair.first(), pair.second());
    }

    executeInitializer();
  }

  @Override
  protected void updateDocument() {
    getInputPane().setDocument(new ConsoleCodeDocument());
    interpreterNewLine(false);
  }

  public void setConsoleName(final String consoleName) {
    this.consoleName = consoleName;

    initConsole();
    updateDocument();
  }

  public void setLibraryPath(final String libraryPath) {
    m_libraryPath = libraryPath;

    initConsole();
    updateDocument();
  }

  /**
   * Class implementing the KeyListener interface. The class handles events such as history browsing
   * and line execution on pressing "Enter".
   */
  private class InterpreterKeyListener extends KeyAdapter {
    private void execute() {
      final InternalScriptRunner loader = new InternalScriptRunner();

      final CEndlessProgressDialog dlg = new CEndlessProgressDialog(null, getProgressWindowTitle(),
          Constants.MESSAGE_RUNNING_SCRIPT, loader);

      loader.run();

      dlg.setVisible(true);

      if (loader.quitProperly()) {
        getOutputPane().setText(console.getOutput());
      }
    }

    private void handleBackspace(final KeyEvent e) {
      if ((getInputPane().getSelectionStart() == getInputPane().getSelectionEnd()) && (
          getDocument().getCaretOffsetInLine(getInputPane().getCaretPosition())
          <= m_CurrentPrompt.length())) {
        e.consume();
      }
    }

    private void handleDownKey(final KeyEvent e) {
      if (m_HistoryPosition < (m_LineHistory.size() - 1)) {
        // Bring back the next entry in the line history

        setCurrentLine(m_LineHistory.get(++m_HistoryPosition));
      } else if (m_HistoryPosition == (m_LineHistory.size() - 1)) {
        // Bring back the saved line

        m_HistoryPosition++;

        setCurrentLine(m_SavedTypedLine);
      }

      e.consume();
    }

    private void handleEnter(final KeyEvent e) {
      goToLastLine();

      final String line = getCurrentLine();

      buffered += line + "\n";

      m_SavedTypedLine = "";

      if (e.isControlDown()) {
        execute();

        buffered = InitStringFactory.getInitString(currentLanguage, consoleName, m_libraryPath);

        // Handle single-line input

        getOutputDocument().flip();

        if (line.trim().length() > 0) {
          m_LineHistory.add(line);
        }

        m_HistoryPosition = m_LineHistory.size();

        interpreterNewLine(false);
      } else {
        // Handle input that requires more input

        // Add the new line to the line history
        if (line.trim().length() > 0) {
          m_LineHistory.add(line);
        }

        m_HistoryPosition = m_LineHistory.size();

        interpreterNewLine(true);

        if (!m_IsMultilineInput) {
          getOutputDocument().flip();
        }
      }

      getDocument().flushRemainingText(getInputPane().getText().length());

      e.consume();
    }

    private void handleTabKey(final KeyEvent e) {
      // Get the position where the last completion was started (i.e. where in the
      // middle of a word)
      //
      final int currPos = getInputPane().getCaretPosition() - m_LastAutoCompleteChunkLength;

      // If no completions are found, the TAB will be passed along. Otherwise
      // a completion will be offered and the TAB consumed.

      final String word = getDocument().getWord(currPos);

      if (word.length() > 0) {
        final String completion = m_tabCompletion.getCompletion(word);

        // If there are possible completions available, substitute the last
        // completion (if any) with the new one found.
        //
        if (completion != null) {
          final String completionChunk = completion.substring(word.length(), completion.length());

          try {
            getDocument().remove(currPos, m_LastAutoCompleteChunkLength);
            getDocument().insertString(currPos, completionChunk, null);
          } catch (final BadLocationException excp) {
            excp.printStackTrace();
          }

          // Store state information for subsequent completion requests
          m_LastAutoCompleteChunkLength = completionChunk.length();
          e.consume();
        }
      }

    }

    private void handleUpKey(final KeyEvent e) {
      if (m_HistoryPosition > 0) {
        /**
         * If the history is just beginning to be browsed backwards and there's text already typed
         * it'll be saved.
         */
        if (m_HistoryPosition == m_LineHistory.size()) {
          goToLastLine();

          m_SavedTypedLine = getCurrentLine();
        }
        /**
         * If there's previous history, fetch the last line and write it into the last available
         * prompt line.
         */
        setCurrentLine(m_LineHistory.get(--m_HistoryPosition));
      } else {
        m_HistoryPosition = 0;
      }

      e.consume();
    }

    @Override
    public void keyPressed(final KeyEvent e) {
      final int lastLineOffset = getLastInputLine();
      final int currLineOffset = getCurrentInputLine();

      // Only allow input in the last line
      if (lastLineOffset != currLineOffset) {
        goToLastLine();
      }

      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        handleEnter(e);
      } else if (e.getModifiers() == 0) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          handleUpKey(e);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          handleDownKey(e);
        } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
          handleTabKey(e);
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
          handleBackspace(e);
        }

        // If something else than TAB was pressed, reset the autocompletion state
        if (e.getKeyCode() != KeyEvent.VK_TAB) {
          m_tabCompletion.reset();
          m_LastAutoCompleteChunkLength = 0;
        }
      }
    }

    private class InternalScriptRunner implements IEndlessProgressModel {
      private final ListenerProvider<IEndlessProgressListener> listeners =
          new ListenerProvider<IEndlessProgressListener>();
      private Thread m_thread;

      private boolean quitProperly = true;

      @Override
      public void addProgressListener(final IEndlessProgressListener listener) {
        listeners.addListener(listener);
      }

      @SuppressWarnings("deprecation")
      @Override
      public void closeRequested() {
        m_thread.stop();

        for (final IEndlessProgressListener listener : listeners) {
          listener.finished();
        }
      }

      public boolean quitProperly() {
        return quitProperly;
      }

      @Override
      public void removeProgressListener(final IEndlessProgressListener listener) {
        listeners.removeListener(listener);
      }

      public void run() {
        m_thread = new Thread() {
          @Override
          public void run() {
            try {
              engine.eval(buffered);
            } catch (final ScriptException e) {
              getOutputPane().setText(e.getLocalizedMessage());
              quitProperly = false;
            }

            for (final IEndlessProgressListener listener : listeners) {
              listener.finished();
            }
          }
        };

        m_thread.start();
      }

    }
  }

  private class TabCompletionManager {
    private int m_completionIndex = 0;

    /**
     * Search for completions for a given string within a given list of completion candidates
     * skipping a given number of them.
     *
     * @param candidates Array of completion candidates
     * @param str string to autocomplete
     * @param completionIdx number of completions to skip. It will wrap around if it's bigger than
     *        the number of completions.
     */
    private String getCompletionCandidate(final String candidates[], final String str,
        int completionIdx) {
      boolean completionsExist = false;

      // Loop through the candidate list searching for completions while
      // skipping as many as requested
      //
      for (final String element : candidates) {
        if (element.startsWith(str)) {
          completionsExist = true;

          if (completionIdx == 0) {
            m_completionIndex++;

            return element;
          }

          completionIdx--;
        }
      }

      // If there are matches, yet nothing was found because the completionIdx
      // grew beyond the number of matches... we search again, effectively wrapping around.
      if (completionsExist) {
        return getCompletionCandidate(candidates, str, completionIdx);
      }

      return null;
    }

    /**
     * Find a completion for the given string skipping a givin number of completions.
     *
     * @param str string to autocomplete
     */
    public String getCompletion(final String str) {
      return getCompletionCandidate(candidates.toArray(new String[0]), str, m_completionIndex);
    }

    public void reset() {
      m_completionIndex = 0;
    }

  }
}
