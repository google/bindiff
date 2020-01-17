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
