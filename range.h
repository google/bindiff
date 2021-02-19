// Copyright 2011-2021 Google LLC
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

#ifndef RANGE_H_
#define RANGE_H_

#include <utility>

template <typename Container, typename Iterator = typename Container::iterator>
class Range {
 public:
  using iterator = Iterator;
  using const_iterator = typename Container::const_iterator;
  using reference = typename Container::reference;
  using const_reference = typename Container::const_reference;
  using pointer = typename Container::pointer;
  using const_pointer = typename Container::const_pointer;
  using size_type = typename Container::size_type;

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

#endif  // RANGE_H_
