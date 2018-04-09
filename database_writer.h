#ifndef DATABASE_WRITER_H_
#define DATABASE_WRITER_H_

#include <string>

#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/writer.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

class DatabaseWriter : public Writer {
 public:
  // Regular constructor for creating result databases.
  explicit DatabaseWriter(const string& path);

  // Special constructor for creating the temporary database.
  explicit DatabaseWriter(const string& path, bool recreate);
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  void Close();
  void WriteToTempDatabase(const FixedPoint& fixed_point);
  void DeleteFromTempDatabase(Address primary, Address secondary);
  const string& GetFilename() const;

  // Mark all matches in the database for which comments have been ported.
  void SetCommentsPorted(const FixedPointInfos& fixed_points);

  // Exposing internal details for use in the temporary database.
  SqliteDatabase* GetDatabase();

 private:
  void PrepareDatabase();
  void WriteMetaData(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);
  void WriteMatches(const FixedPoints& fixed_points);
  void WriteAlgorithms();

  using NameToId = std::map<string, int>;
  NameToId basic_block_steps_;
  NameToId function_steps_;
  SqliteDatabase database_;
  string filename_;
};

class DatabaseTransmuter : public Writer {
 public:
  DatabaseTransmuter(SqliteDatabase& database,
                     const FixedPointInfos& fixed_points);
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  static string GetTempFile();
  static void DeleteTempFile();
  static void MarkPortedComments(SqliteDatabase* database,
                                 const char* temp_database,
                                 const FixedPointInfos& fixed_points);

 private:
  using TempFixedPoints = std::set<std::pair<Address, Address>>;

  void DeleteMatches(const TempFixedPoints& kill_me);

  SqliteDatabase& database_;
  TempFixedPoints fixed_points_;
  const FixedPointInfos& fixed_point_infos_;
};

class DatabaseReader : public Reader {
 public:
  explicit DatabaseReader(SqliteDatabase& database,
                          const string& filename,
                          const string& temp_directory);
  virtual void Read(CallGraph& call_graph1, CallGraph& call_graph2,
                    FlowGraphInfos& flow_graphs1, FlowGraphInfos& flow_graphs2,
                    FixedPointInfos& fixed_points);

  static void ReadFullMatches(SqliteDatabase* database,
                              CallGraph* call_graph1, CallGraph* call_graph2,
                              FlowGraphs* flow_graphs1,
                              FlowGraphs* flow_graphs2,
                              FixedPoints* fixed_points);

  string GetInputFilename() const;
  string GetPrimaryFilename() const;
  string GetSecondaryFilename() const;
  const Counts& GetBasicBlockFixedPointInfo() const;

 private:
  SqliteDatabase& database_;
  string input_filename_;
  string primary_filename_;
  string secondary_filename_;
  string path_;
  string temporary_directory_;
  Counts basic_block_fixed_point_info_;
};

}  // namespace bindiff
}  // namespace security

#endif  // DATABASE_WRITER_H_
