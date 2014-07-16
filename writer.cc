#include "third_party/zynamics/bindiff/writer.h"

Writer::Writer() {}

Writer::~Writer() {}

void ChainWriter::Write(const CallGraph& call_graph1,
                        const CallGraph& call_graph2,
                        const FlowGraphs& flow_graphs1,
                        const FlowGraphs& flow_graphs2,
                        const FixedPoints& fixed_points) {
  for (auto& writer : writers_) {
    writer->Write(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                  fixed_points);
  }
}

void ChainWriter::Add(std::shared_ptr<Writer> writer) {
  writers_.push_back(writer);
}

bool ChainWriter::IsEmpty() const { return writers_.empty(); }
