// Copyright 2011-2017 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Range is a slice of a container denoted with a pair of iterators begin and
// end. Behaves like a normal container with elements between begin and end. Can
// be used in range based loops.
//
// Examples:
// std::vector<int> v;
// Range<std::vector<int>> range(v.begin() + n, v.begin() + m);
// for (int i : r) { /* Do something with i */ }

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_RANGE_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_RANGE_H_

#include <utility>

template <typename Container, typename Iterator = typename Container::iterator>
class Range {
 public:
  typedef Iterator iterator;
  typedef typename Container::const_iterator const_iterator;
  typedef typename Container::reference reference;
  typedef typename Container::const_reference const_reference;
  typedef typename Container::pointer pointer;
  typedef typename Container::const_pointer const_pointer;
  typedef typename Container::size_type size_type;

  explicit Range(const Container& c) : Range(c.begin(), c.end()) {}

  explicit Range(Container* c) : Range(c->begin(), c->end()) {}

  explicit Range(const std::pair<Iterator, Iterator>& p)
      : Range(p.first, p.second) {}

  Range(iterator begin, iterator end) : begin_(begin), end_(end) {}

  iterator begin() { return begin_; }

  const_iterator begin() const { return begin_; }

  iterator end() { return end_; }

  const_iterator end() const { return end_; }

  size_type size() const { return end_ - begin_; }

  bool empty() const { return begin_ == end_; }

 private:
  iterator begin_;
  iterator end_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_RANGE_H_
