#ifndef WRITER_H_
#define WRITER_H_

#include <list>
#include <memory>

#include "base/macros.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/graph_util.h"

class Writer {
 public:
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points) = 0;
  virtual ~Writer();

 protected:
  Writer();

 private:
  DISALLOW_COPY_AND_ASSIGN(Writer);
};

class ChainWriter : public Writer {
 public:
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  void Add(std::shared_ptr<Writer> writer);
  bool IsEmpty() const;

 private:
  typedef std::list<std::shared_ptr<Writer>> Writers;
  Writers writers_;
};

#endif  // WRITER_H_
