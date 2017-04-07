package com.google.security.zynamics.bindiff.database;

import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CommentsDatabase extends SqliteDatabase {
  private static final String STATEMENT_CREATE_BASICBLOCK_COMMENT_TABLE =
      "CREATE TABLE IF NOT EXISTS bd_basicblockComments (pe_hash VARCHAR(40) NOT NULL, "
          + "functionAddr BIGINT NOT NULL, basicblockAddr BIGINT NOT NULL, "
          + "comment long VARCHAR NOT NULL, primary key (pe_hash, functionAddr, basicblockAddr))";

  private static final String STATEMENT_CREATE_INSTRUCTION_COMMENT_TABLE =
      "CREATE TABLE IF NOT EXISTS bd_instructionComments(pe_hash VARCHAR(40) NOT NULL, "
          + "functionAddr BIGINT NOT NULL, instructionAddr BIGINT NOT NULL, "
          + "placement SMALLINT NOT NULL, comment long VARCHAR NOT NULL, "
          + "PRIMARY KEY (pe_hash, functionAddr, instructionAddr, placement))";

  public CommentsDatabase(final Workspace workspace, final boolean createTables)
      throws SQLException {
    // Read the data from the existing workspace database
    super(workspace.getWorkspaceFile());

    if (createTables) {
      createTables();
    }
  }

  private void createTables() throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(STATEMENT_CREATE_BASICBLOCK_COMMENT_TABLE);
    } catch (final SQLException e) {
      throw new SQLException("Could not create basic block comment tables: " + e.getMessage(), e);
    }

    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(STATEMENT_CREATE_INSTRUCTION_COMMENT_TABLE);
    } catch (final SQLException e) {
      throw new SQLException("Could not create instruction comment tables: " + e.getMessage(), e);
    }
  }

  public Map<IAddress, String> readBasicblockComments(
      final String imageHash, final IAddress functionAddr) throws SQLException {
    final Map<IAddress, String> commentMap = new HashMap<>();
    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT basicblockAddr, comment FROM bd_basicblockComments "
                + "WHERE pe_hash = ? AND functionAddr = ?")) {
      statement.setString(1, imageHash);
      statement.setLong(2, functionAddr.toLong());

      final ResultSet result = statement.executeQuery();
      while (result.next()) {
        final IAddress addr = new CAddress(result.getLong("basicblockAddr"));
        final String comment = result.getString("comment");
        commentMap.put(addr, comment);
      }
    } catch (final SQLException e) {
      throw new SQLException("Couldn't read basic block comments: " + e.getMessage(), e);
    }
    return commentMap;
  }

  public Map<IAddress, String> readFunctionComments(final String image) throws SQLException {
    final Map<IAddress, String> commentMap = new HashMap<>();

    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT functionAddr, comment FROM bd_functionComments WHERE pe_hash = ?")) {
      statement.setString(1, image);

      final ResultSet result = statement.executeQuery();
      while (result.next()) {
        final IAddress addr = new CAddress(result.getLong("functionAddr"));
        final String comment = result.getString("comment");

        commentMap.put(addr, comment);
      }
    } catch (final SQLException e) {
      throw new SQLException("Couldn't read function comments: " + e.getMessage(), e);
    }
    return commentMap;
  }

  public Map<Pair<IAddress, ECommentPlacement>, String> readInstructionComments(
      final String image, final IAddress functionAddr) throws SQLException {
    final Map<Pair<IAddress, ECommentPlacement>, String> commentMap = new HashMap<>();
    try (final PreparedStatement statement =
        connection.prepareStatement(
            "SELECT instructionAddr, placement, comment FROM bd_instructionComments "
                + "WHERE pe_hash = ? AND functionAddr = ?")) {
      statement.setString(1, image);
      statement.setLong(2, functionAddr.toLong());

      final ResultSet result = statement.executeQuery();

      while (result.next()) {
        final IAddress addr = new CAddress(result.getLong("instructionAddr"));
        final ECommentPlacement placement = ECommentPlacement.valueOf(result.getInt("placement"));
        final String comment = result.getString("comment");

        final Pair<IAddress, ECommentPlacement> pair = Pair.make(addr, placement);

        commentMap.put(pair, comment);
      }
    } catch (final SQLException e) {
      throw new SQLException("Couldn't read instruction comments: " + e.getMessage(), e);
    }
    return commentMap;
  }

  public void writeBasicblockComment(
      final String image,
      final IAddress functionAddr,
      final IAddress basicblockAddr,
      final String comment)
      throws SQLException {
    try (final PreparedStatement numCommentsStmt =
        connection.prepareStatement(
            "SELECT COUNT(*) AS counter FROM bd_basicblockComments "
                + "WHERE pe_hash = ? AND functionAddr = ? AND basicblockAddr = ?")) {
      numCommentsStmt.setString(1, image);
      numCommentsStmt.setLong(2, functionAddr.toLong());
      numCommentsStmt.setLong(3, basicblockAddr.toLong());

      final ResultSet result = numCommentsStmt.executeQuery();

      if (result.next()) {
        if (result.getInt("counter") == 0) {
          if (!comment.isEmpty()) {
            try (final PreparedStatement statement =
                connection.prepareStatement(
                    "INSERT INTO bd_basicblockComments VALUES (?, ?, ?, ?)")) {
              statement.setString(1, image);
              statement.setLong(2, functionAddr.toLong());
              statement.setLong(3, basicblockAddr.toLong());
              statement.setString(4, comment);
              statement.executeUpdate();
            }
          }
        } else if (comment.isEmpty()) {
          try (final PreparedStatement statement =
              connection.prepareStatement(
                  "DELETE FROM bd_basicblockComments "
                      + "WHERE pe_hash = ? AND functionAddr = ? AND basicblockAddr = ?")) {
            statement.setString(1, image);
            statement.setLong(2, functionAddr.toLong());
            statement.setLong(3, basicblockAddr.toLong());
            statement.executeUpdate();
          }
        } else {
          try (final PreparedStatement statement =
              connection.prepareStatement(
                  "UPDATE bd_basicblockComments SET comment = ? "
                      + "WHERE pe_hash = ? AND functionAddr = ? AND basicblockAddr = ?")) {
            statement.setString(1, comment);
            statement.setString(2, image);
            statement.setLong(3, functionAddr.toLong());
            statement.setLong(4, basicblockAddr.toLong());
            statement.executeUpdate();
          }
        }
      }
    } catch (final SQLException e) {
      throw new SQLException(
          "Couldn't write basic block comment into database: " + e.getMessage(), e);
    }
  }

  public void writeFunctionComment(
      final String image, final IAddress functionAddr, final String comment) throws SQLException {
    try (final PreparedStatement numCommentsStmt =
        connection.prepareStatement(
            "SELECT COUNT(*) AS counter FROM bd_functionComments "
                + "WHERE pe_hash = ? AND functionAddr = ?")) {
      numCommentsStmt.setString(1, image);
      numCommentsStmt.setLong(2, functionAddr.toLong());

      final ResultSet result = numCommentsStmt.executeQuery();
      if (!result.next()) {
        return;
      }
      if (result.getInt("counter") == 0) {
        if (!comment.isEmpty()) {
          try (final PreparedStatement statement =
              connection.prepareStatement("INSERT INTO bd_functionComments VALUES (?, ?, ?)")) {
            statement.setString(1, image);
            statement.setLong(2, functionAddr.toLong());
            statement.setString(3, comment);
            statement.executeUpdate();
          }
        }
      } else if (comment.isEmpty()) {
        try (final PreparedStatement statement =
            connection.prepareStatement(
                "DELETE FROM bd_functionComments WHERE pe_hash = ? AND functionAddr = ?")) {
          statement.setString(1, image);
          statement.setLong(2, functionAddr.toLong());
          statement.executeUpdate();
        }
      } else {
        try (final PreparedStatement statement =
            connection.prepareStatement(
                "UPDATE bd_functionComments SET comment = ? WHERE pe_hash = ? AND functionAddr = ?")) {
          statement.setString(1, comment);
          statement.setString(2, image);
          statement.setLong(3, functionAddr.toLong());
          statement.executeUpdate();
        }
      }
    } catch (final SQLException e) {
      throw new SQLException("Couldn't save function comment in database: " + e.getMessage(), e);
    }
  }

  public void writeInstructionComment(
      final String imageHash,
      final IAddress functionAddr,
      final IAddress instructionAddr,
      final ECommentPlacement placement,
      final String comment)
      throws SQLException {
    try {
      if (!comment.isEmpty()) {
        try (PreparedStatement statement =
            connection.prepareStatement(
                "INSERT OR REPLACE INTO bd_instructionComments "
                    + "(pe_hash, functionAddr, instructionAddr, placement, comment) "
                    + "VALUES (?, ?, ?, ?, ?)")) {
          statement.setString(1, imageHash);
          statement.setLong(2, functionAddr.toLong());
          statement.setLong(3, instructionAddr.toLong());
          statement.setShort(4, (short) ECommentPlacement.getOrdinal(placement));
          statement.setString(5, comment);
          statement.executeUpdate();
        }
      } else {
        try (PreparedStatement statement =
            connection.prepareStatement(
                "DELETE FROM bd_instructionComments WHERE "
                    + "pe_hash = ? AND functionAddr = ? AND instructionAddr = ? AND placement = ?")) {
          statement.setString(1, imageHash);
          statement.setLong(2, functionAddr.toLong());
          statement.setLong(3, instructionAddr.toLong());
          statement.setShort(4, (short) ECommentPlacement.getOrdinal(placement));
          statement.executeUpdate();
        }
      }
    } catch (final SQLException e) {
      throw new SQLException(
          "Couldn't store instruction comment in database: " + e.getMessage(), e);
    }
  }
}
