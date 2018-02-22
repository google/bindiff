#ifndef LOG_WRITER_H_
#define LOG_WRITER_H_

#include <string>

#include "third_party/zynamics/bindiff/writer.h"

// Writes a human readable log file for debugging purposes.
class ResultsLogWriter : public Writer {
 public:
  explicit ResultsLogWriter(const string& filename);
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

 private:
  string filename_;
};

#endif  // LOG_WRITER_H_
