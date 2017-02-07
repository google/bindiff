#ifndef DATABASE_WRITER_H_
#define DATABASE_WRITER_H_

#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/writer.h"

class DatabaseWriter : public Writer {
 public:
  // Regular constructor for creating result databases.
  explicit DatabaseWriter(const std::string& path);

  // Special constructor for creating the temporary database.
  explicit DatabaseWriter(const std::string& path, bool recreate);
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  void Close();
  void WriteToTempDatabase(const FixedPoint& fixed_point);
  void DeleteFromTempDatabase(Address primary, Address secondary);
  const std::string& GetFilename() const;

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

  typedef std::map<std::string, int> NameToId;
  NameToId basic_block_steps_;
  NameToId function_steps_;
  SqliteDatabase database_;
  std::string filename_;
};

class DatabaseTransmuter : public Writer {
 public:
  DatabaseTransmuter(SqliteDatabase& database,
                     const FixedPointInfos& fixed_points);
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  static std::string GetTempFile();
  static void DeleteTempFile();
  static void MarkPortedComments(SqliteDatabase* database,
                                 const char* temp_database,
                                 const FixedPointInfos& fixed_points);

 private:
  typedef std::set<std::pair<Address, Address> > TempFixedPoints;

  void DeleteMatches(const TempFixedPoints& kill_me);

  SqliteDatabase& database_;
  TempFixedPoints fixed_points_;
  const FixedPointInfos& fixed_point_infos_;
};

class DatabaseReader : public Reader {
 public:
  explicit DatabaseReader(SqliteDatabase& database,
                          const std::string& filename,
                          const std::string& temp_directory);
  virtual void Read(CallGraph& call_graph1, CallGraph& call_graph2,
                    FlowGraphInfos& flow_graphs1, FlowGraphInfos& flow_graphs2,
                    FixedPointInfos& fixed_points);

  static void ReadFullMatches(SqliteDatabase* database,
                              CallGraph* call_graph1, CallGraph* call_graph2,
                              FlowGraphs* flow_graphs1,
                              FlowGraphs* flow_graphs2,
                              FixedPoints* fixed_points);

  std::string GetInputFilename() const;
  std::string GetPrimaryFilename() const;
  std::string GetSecondaryFilename() const;
  const Counts& GetBasicBlockFixedPointInfo() const;

 private:
  SqliteDatabase& database_;
  std::string input_filename_;
  std::string primary_filename_;
  std::string secondary_filename_;
  std::string path_;
  std::string temporary_directory_;
  Counts basic_block_fixed_point_info_;
};

#endif  // DATABASE_WRITER_H_
