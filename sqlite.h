// TODO(cblichmann): We may want to export directly to SQLite from BinExport in
//                   the future. In that case, move this file to BinExport.

#ifndef SQLITE_H_
#define SQLITE_H_

#include <cstdint>
#include <memory>

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/types.h"

struct sqlite3;
struct sqlite3_stmt;

namespace security::bindiff {

class SqliteStatement;

class SqliteDatabase {
 public:
  friend class SqliteStatement;

  SqliteDatabase();
  explicit SqliteDatabase(const char* filename);
  ~SqliteDatabase();

  SqliteDatabase(const SqliteDatabase&) = delete;
  SqliteDatabase& operator=(const SqliteDatabase&) = delete;

  void Connect(const char* filename);
  void Disconnect();

  void Begin();
  void Commit();
  void Rollback();

  std::shared_ptr<SqliteStatement> Statement(const char* statement);

 private:
  sqlite3* database_;
};

class SqliteStatement {
 public:
  explicit SqliteStatement(SqliteDatabase* database, const char* statement);
  ~SqliteStatement();

  SqliteStatement(const SqliteStatement&) = delete;
  SqliteStatement& operator=(const SqliteStatement&) = delete;

  SqliteStatement& BindInt(int value);
  SqliteStatement& BindInt64(int64_t value);
  SqliteStatement& BindDouble(double value);
  SqliteStatement& BindText(absl::string_view value);
  SqliteStatement& BindNull();

  SqliteStatement& Into(int* value, bool* is_null = nullptr);
  SqliteStatement& Into(int64_t* value, bool* is_null = nullptr);
  SqliteStatement& Into(Address* value, bool* is_null = nullptr);
  SqliteStatement& Into(double* value, bool* is_null = nullptr);
  SqliteStatement& Into(std::string* value, bool* is_null = nullptr);

  SqliteStatement& Execute();
  SqliteStatement& Reset();
  bool GotData() const;

 private:
  sqlite3* database_;
  sqlite3_stmt* statement_;
  int parameter_;
  int column_;
  bool got_data_;
};

}  // namespace security::bindiff

#endif  // SQLITE_H_
