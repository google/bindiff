package com.google.security.zynamics.bindiff.database;

import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.CFileUtils;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceDatabase extends SqliteDatabase {
  private static final String STATEMENT_CREATE_METADATA_TABLE =
      "CREATE TABLE IF NOT EXISTS metadata (version INT NOT NULL)";

  private static final String STATEMENT_CREATE_DIFFS_TABLE =
      "CREATE TABLE IF NOT EXISTS diffs ("
          + "matchesDbPath VARCHAR NOT NULL, isfunctiondiff NUMERIC NOT NULL DEFAULT 0)";

  public WorkspaceDatabase(final File database) throws SQLException {
    super(database);

    createTables();
  }

  private void setFormatVersionNumber() throws SQLException {
    try (final PreparedStatement statement =
        connection.prepareStatement("INSERT INTO metadata (version) VALUES (?)")) {
      statement.setInt(1, Constants.WORKSPACE_DATABASE_FORMAT_VERSION);
      statement.executeUpdate();
    }
  }

  private void createTables() throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(STATEMENT_CREATE_METADATA_TABLE);
      setFormatVersionNumber();
    }
    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(STATEMENT_CREATE_DIFFS_TABLE);
    }
  }

  public List<String> loadDiffPaths(final boolean isFunctionDiff) throws SQLException {
    final List<String> diffPaths = new ArrayList<>();
    try (final PreparedStatement statement =
        connection.prepareStatement("SELECT matchesDbPath FROM diffs WHERE isfunctiondiff = ?")) {
      statement.setBoolean(1, isFunctionDiff);

      final ResultSet result = statement.executeQuery();
      while (result.next()) {
        diffPaths.add(CFileUtils.forceFileSeparator(result.getString("matchesDbPath")));
      }
    } catch (final SQLException e) {
      Logger.logException(e, e.getMessage());
      if (isFunctionDiff) {
        throw new SQLException("Failed to load workspace: Couldn't load function diff paths.", e);
      }
      throw new SQLException("Failed to load workspace: Couldn't load diff paths.", e);
    }

    return diffPaths;
  }

  public void saveWorkspace(final Workspace workspace) throws SQLException {
    connection.setAutoCommit(false);
    try (final PreparedStatement statement1 = connection.prepareStatement("DELETE FROM diffs")) {
      statement1.executeUpdate();
      connection.commit();
    } catch (final SQLException e) {
      connection.rollback();
      Logger.logException(e, e.getMessage());
      throw new SQLException(
          "Failed to write to workspace file. Couldn't delete old entries: " + e.getMessage(), e);
    } finally {
      connection.setAutoCommit(true);
    }

    connection.setAutoCommit(false);
    try (final PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO diffs (matchesDbPath, isfunctiondiff) VALUES (?, ?)")) {
      final String workspaceRoot = FileUtils.ensureTrailingSlash(workspace.getWorkspaceDirPath());
      for (final Diff diff : workspace.getDiffList()) {
        String matchesDbPath = diff.getMatchesDatabase().getAbsolutePath();
        if (matchesDbPath.startsWith(workspaceRoot)) {
          matchesDbPath = matchesDbPath.substring(workspaceRoot.length());
        }

        statement.setString(1, matchesDbPath);
        statement.setInt(2, !diff.isFunctionDiff() ? 0 : 1);
        statement.addBatch();
      }
      statement.executeBatch();
      connection.commit();
    } catch (final SQLException e) {
      connection.rollback();
      Logger.logException(e, e.getMessage());
      throw new SQLException(
          "Failed to save workspace file. Couldn't write new entries: " + e.getMessage(), e);
    } finally {
      connection.setAutoCommit(true);
    }
  }
}
