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

package com.google.security.zynamics.bindiff.database;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SqliteDatabase implements AutoCloseable {
  final Connection connection;

  SqliteDatabase(final File database) throws SQLException {
    checkNotNull(database);
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (final ClassNotFoundException e) {
      throw new SQLException("JDBC driver for SQLite not found", e);
    }
    connection =
        DriverManager.getConnection(
            "jdbc:sqlite:"
                // Support paths longer than MAX_PATH on Windows.
                + (SystemHelpers.isRunningWindows() ? "\\\\?\\" : "")
                + database.getPath());
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }
}
