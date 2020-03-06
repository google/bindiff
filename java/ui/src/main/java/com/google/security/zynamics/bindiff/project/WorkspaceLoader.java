// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.project;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.database.WorkspaceDatabase;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetadata;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public final class WorkspaceLoader extends CEndlessHelperThread {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final int MAX_LOAD_WORKSPACE_ERRORS = 15;
  private final Workspace workspace;
  private final File workspaceFile;
  private StringBuilder errors = new StringBuilder();

  public WorkspaceLoader(final File workspaceFile, final Workspace workspace) {
    this.workspaceFile = checkNotNull(workspaceFile);
    this.workspace = checkNotNull(workspace);
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    loadMetaData();
  }

  public String getErrorMessage() {
    return errors.toString();
  }

  public boolean hasErrors() {
    return "".equals(getErrorMessage());
  }

  public void loadMetaData() throws IOException, SQLException {
    workspace.setLoaded(false);

    if (workspaceFile == null) {
      throw new IOException("Load workspace failed. Workspace file cannot be null.");
    }
    if (workspaceFile.isDirectory()) {
      throw new IOException("Load workspace failed. Workspace file is a directory.");
    }

    logger.at(Level.INFO).log("Loading workspace '%s'...", workspaceFile.getPath());
    setDescription("Reading workspace data...");

    try (final WorkspaceDatabase workspaceDatabase = new WorkspaceDatabase(workspaceFile)) {
      final List<String> diffPaths = workspaceDatabase.loadDiffPaths(false);
      final List<String> functionDiffPaths = workspaceDatabase.loadDiffPaths(true);

      int diffCounter = 0;
      int allDiffs = diffPaths.size();

      errors.setLength(0);

      final String workspaceRoot =
          FileUtils.ensureTrailingSlash(workspaceFile.getParentFile().getAbsolutePath());
      for (final String path : diffPaths) {
        diffCounter++;

        // Try to look up by workspace-relative path first
        final File workspaceAbs = new File(workspaceRoot + path);
        final File matchesDatabaseFile = workspaceAbs.exists() ? workspaceAbs : new File(path);

        logger.at(Level.INFO).log(" - Preloading Diff '%s'", matchesDatabaseFile.getPath());

        final DiffMetadata matchesMetadata;
        try (final MatchesDatabase matchesDatabase = new MatchesDatabase(matchesDatabaseFile)) {
          setDescription(
              String.format(
                  "Preloading Diffs %d/%d '%s'",
                  diffCounter, allDiffs, matchesDatabaseFile.getName()));

          matchesMetadata = matchesDatabase.loadDiffMetadata(matchesDatabaseFile);
        } catch (final SQLException e) {
          logger.at(Level.SEVERE).withCause(e).log();
          errors.append(" - ").append(matchesDatabaseFile.getName()).append("\n");
          continue;
        }

        errors.append(workspace.addDiff(matchesDatabaseFile, matchesMetadata, false));
      }

      diffCounter = 0;
      allDiffs = functionDiffPaths.size();
      for (final String path : functionDiffPaths) {
        diffCounter++;

        // Try to look up by workspace-relative path first
        final File workspaceAbs = new File(workspaceRoot + path);
        final File matchesDatabaseFile = workspaceAbs.exists() ? workspaceAbs : new File(path);

        logger.at(Level.INFO).log(
            " - Preloading Function Diff '%s'", matchesDatabaseFile.getPath());

        FunctionDiffMetadata matchesMetadata = null;
        setDescription(
            String.format(
                "Preloading Function Diffs %d/%d '%s'",
                diffCounter, allDiffs, matchesDatabaseFile.getName()));

        try (final MatchesDatabase matchesDatabase = new MatchesDatabase(matchesDatabaseFile)) {
          matchesMetadata = matchesDatabase.loadFunctionDiffMetadata(false);
        } catch (final SQLException e) {
          errors.append(" - ").append(matchesDatabaseFile.getName()).append("\n");

          continue;
        }

        errors.append(workspace.addDiff(matchesDatabaseFile, matchesMetadata, true));
      }

      int fromIndex = 0;
      int foundIndex = 0;
      int counter = 0;

      while (foundIndex != -1) {
        foundIndex = errors.indexOf("\n", fromIndex);

        if (foundIndex != -1) {
          counter++;

          if (counter >= MAX_LOAD_WORKSPACE_ERRORS) {
            errors.setLength(foundIndex);
            errors.append("...");

            break;
          }

          fromIndex = foundIndex + 1;
        }
      }

      if (errors.length() > 0) {
        errors.insert(0, "Diff loading failed for at least one item:\n\n");
      }

      workspace.setWorkspaceFile(workspaceFile);

      setDescription("Connecting to comment database...");

      workspace.setLoaded(true);

      for (final IWorkspaceListener listener : workspace.getListeners()) {
        listener.loadedWorkspace(workspace);
      }

      logger.at(Level.INFO).log("Workspace loaded");
    }
  }
}
