#ifndef READER_H_
#define READER_H_

#include "third_party/zynamics/bindiff/fixedpoints.h"
#include "third_party/zynamics/bindiff/graphutility.h"

struct FlowGraphInfo {
  Address address;
  const std::string* name;
  const std::string* demangled_name;
  std::istream::pos_type file_offset;
  int basic_block_count;
  int edge_count;
  int instruction_count;
};
typedef std::map<Address, FlowGraphInfo> FlowGraphInfos;

struct FixedPointInfo {
  Address primary;
  Address secondary;
  int basic_block_count;
  int edge_count;
  int instruction_count;
  double similarity;
  double confidence;
  int flags;
  const std::string* algorithm;
  bool evaluate;
  bool comments_ported;

  bool IsManual() const;
};
typedef std::set<FixedPointInfo> FixedPointInfos;

bool operator <(const FixedPointInfo& one, const FixedPointInfo& two);

class Reader : private boost::noncopyable {
 public:
  explicit Reader();
  virtual ~Reader() {}

  virtual void Read(CallGraph& call_graph1, CallGraph& call_graph2,
                    FlowGraphInfos& flow_graphs1, FlowGraphInfos& flow_graphs2,
                    FixedPointInfos& fixed_points) = 0;

  double GetSimilarity() const;
  double GetConfidence() const;

 protected:
  double similarity_;
  double confidence_;
};

#endif  // READER_H_
