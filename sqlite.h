#ifndef SQLITE_H_
#define SQLITE_H_

#include <cstdint>
#include <memory>

#include "base/macros.h"
#include "third_party/zynamics/bindiff/utility.h"

struct sqlite3;
struct sqlite3_stmt;
class SqliteStatement;

class SqliteDatabase {
 public:
  friend class SqliteStatement;

  SqliteDatabase();
  explicit SqliteDatabase(const char* filename);
  ~SqliteDatabase();

  void Connect(const char* filename);
  void Disconnect();

  void Begin();
  void Commit();
  void Rollback();

  std::shared_ptr<SqliteStatement> Statement(const char* statement);

 private:
  sqlite3* database_;

  DISALLOW_COPY_AND_ASSIGN(SqliteDatabase);
};

class SqliteStatement {
 public:
  explicit SqliteStatement(SqliteDatabase* database, const char* statement);
  ~SqliteStatement();

  SqliteStatement& BindInt(int value);
  SqliteStatement& BindInt64(int64_t value);
  SqliteStatement& BindDouble(double value);
  SqliteStatement& BindText(const char * value, int length = -1);
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

  DISALLOW_COPY_AND_ASSIGN(SqliteStatement);
};

#endif  // SQLITE_H_
