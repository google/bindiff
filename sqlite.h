#ifndef SRC_SQLITE_H_
#define SRC_SQLITE_H_

#include <cstdint>

#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/noncopyable.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/shared_ptr.hpp"
#include "third_party/zynamics/bindiff/utility.h"

struct sqlite3;
struct sqlite3_stmt;
class SqliteStatement;

class SqliteDatabase : private boost::noncopyable {
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

  boost::shared_ptr<SqliteStatement> Statement(const char* statement);

 private:
  sqlite3* database_;
};

class SqliteStatement : private boost::noncopyable {
 public:
  explicit SqliteStatement(SqliteDatabase* database, const char* statement);
  ~SqliteStatement();

  SqliteStatement& BindInt(int value);
  SqliteStatement& BindInt64(int64_t value);
  SqliteStatement& BindDouble(double value);
  SqliteStatement& BindText(const char * value, int length = -1);
  SqliteStatement& BindNull();

  SqliteStatement& Into(int* value, bool* is_null = 0);
  SqliteStatement& Into(int64_t* value, bool* is_null = 0);
  SqliteStatement& Into(Address* value, bool* is_null = 0);
  SqliteStatement& Into(double* value, bool* is_null = 0);
  SqliteStatement& Into(std::string* value, bool* is_null = 0);

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

#endif  // SRC_SQLITE_H_
