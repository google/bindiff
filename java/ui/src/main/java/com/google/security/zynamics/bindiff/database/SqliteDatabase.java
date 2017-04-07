package com.google.security.zynamics.bindiff.database;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SqliteDatabase implements AutoCloseable {
  protected final Connection connection;

  public SqliteDatabase(final File database) throws SQLException {
    Preconditions.checkNotNull(database);
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (final ClassNotFoundException e) {
      throw new SQLException("JDBC driver for SQLite not found", e);
    }
    connection = DriverManager.getConnection("jdbc:sqlite:" + database.getPath());
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }
}
