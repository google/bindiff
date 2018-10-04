#ifndef CALL_GRAPH_H_
#define CALL_GRAPH_H_

#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT(readability/boost)

#include "third_party/zynamics/bindiff/comments.h"
#include "third_party/zynamics/bindiff/graph_util.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

class FlowGraph;

class CallGraph {
 public:
  struct VertexInfo {
    Address address_ = 0;       // Function address
    string name_;               // Function name
    string demangled_name_;     // Only set iff different from name_
    uint32_t bfs_top_down_ = 0;   // Breadth-first-search level top down
    uint32_t bfs_bottom_up_ = 0;  // Breadth-first-search level bottom up
    uint32_t flags_ = 0;
    FlowGraph* flow_graph_ = nullptr;  // Flow graph (if loaded and attached)
  };

  struct EdgeInfo {
    uint32_t flags_ = 0;
    double md_index_proximity_ = -1.0;  // MD index proximity
    double md_index_top_down_ = 0.0;    // MD index top down
    double md_index_bottom_up_ = 0.0;   // MD index bottom up
  };

  using Graph = boost::compressed_sparse_row_graph<
      boost::bidirectionalS,  // we need to be able to iterate in/out edges
      VertexInfo,             // vertex properties
      EdgeInfo,               // edge properties
      boost::no_property,     // graph properties
      uint32_t,                 // index type for vertices
      uint32_t>;                // index type for edges

  using Vertex = boost::graph_traits<Graph>::vertex_descriptor;
  using VertexIterator = boost::graph_traits<Graph>::vertex_iterator;
  using Edge = boost::graph_traits<Graph>::edge_descriptor;
  using EdgeIterator = boost::graph_traits<Graph>::edge_iterator;
  using OutEdgeIterator = boost::graph_traits<Graph>::out_edge_iterator;
  using InEdgeIterator = boost::graph_traits<Graph>::in_edge_iterator;

  enum {
    // Edge flags
    EDGE_DUPLICATE = 1
  };
  enum {
    // Vertex flags
    VERTEX_LIBRARY = 1 << 0,         // This is a library function.
    VERTEX_STUB = 1 << 1,            // Stub function, i.e. single jmp.
    VERTEX_NAME = 1 << 2,            // Function has a non-auto-generated name.
    VERTEX_DEMANGLED_NAME = 1 << 3,  // C++ demangled name.
  };

  // A constant denoting a non-existent vertex.
  static Vertex kInvalidVertex;

  // Constructs an empty call graph.
  CallGraph();

  // Reads and initializes the call graph from "proto". "filename" is passed in
  // and remembered for informational purposes only (we want to be able to
  // construct default save filenames with it for example).
  void Read(const BinExport2& proto, const string& filename);

  // Get just the filename part (without path or extension) passed into Read().
  string GetFilename() const;

  // Get the fully specified filename including path and extension as passed
  // into read().
  string GetFilePath() const;

  // free all associated data and reset to virgin state
  void Reset();

  // O(logn) binary search over all vertices to return the one with "address".
  // returns ms_InvalidVertex if address couldn't be found.
  Vertex GetVertex(Address address) const;

  // O(1) return the vertice's address.
  Address GetAddress(Vertex vertex) const;

  // O(1) calculate and return the edge's MD index.
  double GetMdIndex(const Edge& edge) const;

  // O(1) return cached MD index for the whole graph.
  double GetMdIndex() const;

  // O(1) set cached MD index for the whole graph.
  void SetMdIndex(double index);

  // Return MD index for a given vertex. Inverted MD index uses the bottom up
  // breadth first node level for calculating MD indices. This method is quite
  // expensive: it iterates its in and out edges and calculates and sums the
  // edge MD indices.
  double GetMdIndex(Vertex vertex) const;
  double GetMdIndexInverted(Vertex vertex) const;

  // O(|V| + |E|) two breadth first searches over the graph. Stores resulting
  // BFS indices in vertices.
  void CalculateTopology();

  // Access internal boost graph structure.
  inline Graph& GetGraph() {
    return graph_;
  }
  inline const Graph& GetGraph() const {
    return graph_;
  }

  // Associate the given flow graph with the corresponding call graph vertex.
  // The call graph will _not_ take ownership of the flow graph!
  void AttachFlowGraph(FlowGraph* flow_graph);
  void DetachFlowGraph(FlowGraph* flow_graph);
  FlowGraph* GetFlowGraph(Address address) const;  // TODO(soerenme) Remove!!!
  inline FlowGraph* GetFlowGraph(Vertex vertex) const {
    return graph_[vertex].flow_graph_;
  }

  // Returns true iff the edge is a circular/recursive edge, i.e. source and
  // target vertex are the same.
  bool IsCircular(const Edge& edge) const;

  // Returns true if this edge is parallel to another one. This happens quite
  // often in a call graph when one function calls another one multiple times.
  // We often don't care about all but the first call.
  void SetDuplicate(const Edge& edge, bool duplicate);
  inline bool IsDuplicate(const Edge& edge) const {
    return graph_[edge].flags_ & EDGE_DUPLICATE;
  }

  // Is this vertex a library function?
  bool IsLibrary(Vertex vertex) const;
  void SetLibrary(Vertex vertex, bool library);

  // Is this vertex a stub function? TODO(soerenme) define stub
  bool IsStub(Vertex vertex) const;
  void SetStub(Vertex vertex, bool stub);

  // Does this vertex have a real, i.e. user supplied, name? Binaries without
  // symbols often have auto generated names like "sub_baadf00d".
  bool HasRealName(Vertex vertex) const;

  // Returns the pair<edge,true> if the edge could be found or
  // pair<Edge(),false> if it doesn't exist in the graph. Search is very
  // efficient with the source lookup happening in O(1) time and edge lookup
  // in O(num_of_outedges(source)).
  std::pair<Edge, bool> FindEdge(Vertex source, Vertex target) const;

  // Return the name of the function "vertex". This is actually the only place
  // where this information is stored. The flow graphs themselves don't know
  // their own names.
  const string& GetName(Vertex vertex) const;
  void SetName(Vertex vertex, const string& name);
  const string& GetDemangledName(Vertex vertex) const;
  void SetDemangledName(Vertex vertex, const string& name);
  // Returns the demangled name if available, raw name otherwise.
  const string& GetGoodName(Vertex vertex) const;

  // Return the name of the original input binary if known, else empty string.
  const string& GetExeFilename() const;
  void SetExeFilename(const string& name);

  // Return the hex encoded hash (SHA256, SHA1 or obsolete MD5) of the original
  // input binary.
  const string& GetExeHash() const;
  void SetExeHash(const string& hash);

  // Access comments. The call graph stores theses globally even for operands
  // because we don't want to store them multiple times for shared basic blocks.
  Comments& GetComments();
  const Comments& GetComments() const;

  // Potentially very expensive function: it reduces the graph to the immediate
  // vicinity of "edge" and recalculates MD indices on that subgraph. The idea
  // is to become resilient against non-local changes to the call graph.
  double GetProximityMdIndex(const Edge& edge);

  // Expensive and buggy function (see notes in implementation). Try to avoid.
  void DeleteVertices(Address start, Address end);

 private:
  void Init();
  double CalculateProximityMdIndex(Edge edge);

  Graph graph_;
  double md_index_;
  string exe_filename_;
  string exe_hash_;
  Comments comments_;
  string filename_;
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_H_
