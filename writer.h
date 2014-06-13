#ifndef SRC_WRITER_H_
#define SRC_WRITER_H_

#include <list>

#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/shared_ptr.hpp"
#include "third_party/zynamics/bindiff/fixedpoints.h"
#include "third_party/zynamics/bindiff/graphutility.h"

class Writer : private boost::noncopyable {
 public:
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points) = 0;
  virtual ~Writer();
};

class ChainWriter : public Writer {
 public:
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  void Add(boost::shared_ptr<Writer> writer);
  bool IsEmpty() const;

 private:
  typedef std::list<boost::shared_ptr<Writer> > Writers;
  Writers writers_;
};

#endif  // SRC_WRITER_H_
