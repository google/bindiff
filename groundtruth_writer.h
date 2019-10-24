// Save BinDiff results to a simple text based "ground truth" format for use in
// regression tests. This code is non-Google3 as it is being used from within
// the BinDiff IDA plugin, hence the use of std streams vs the Google3 file API.
//
// The output format is an ASCII text file containing a pair of hex addresses
// followed by a pair of function names per line.
// Example:
// 000472E0 00043C1C ERR_add_error_data ERR_add_error_data
// Basic block and instruction matches are ignored completely.

#ifndef GROUNDTRUTH_WRITER_H_
#define GROUNDTRUTH_WRITER_H_

#include <string>

#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/writer.h"

namespace security::bindiff {

// Writes a text file which is suitable for easy text comparison to a known good
// file.
class GroundtruthWriter : public Writer {
 public:
  explicit GroundtruthWriter(const std::string& filename);

  // Special constructor, only used in BinDiff plugin so we are able to save
  // from loaded results. Not used anywhere else.
  GroundtruthWriter(const std::string& filename,
                    const FixedPointInfos& fixed_point_infos,
                    const FlowGraphInfos& primary,
                    const FlowGraphInfos& secondary);
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

 private:
  std::string filename_;
  const FixedPointInfos* fixed_point_infos_;
  const FlowGraphInfos* primary_;
  const FlowGraphInfos* secondary_;
};

}  // namespace security::bindiff

#endif  // GROUNDTRUTH_WRITER_H_
