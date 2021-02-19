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

// NestedIterator is a "flattening" iterator that can operate on a container of
// containers, iterating on elements of the inner containers. NestedIterator
// is a forward iterator, reverse iteration and random access are not supported.
//
// Example:
//
// using Vec = vector<vector<int>>;
// Vec v;
// ...
// NestedIterator<Vec> it(v.begin(), v.end());
// NestedIterator<Vec> end(v.end());
// while (it != end) {
//   int i = *it;
//   ...
//   ++it;
// }

#ifndef NESTED_ITERATOR_H_
#define NESTED_ITERATOR_H_

#include <cassert>

template<typename OuterIterator> class NestedIterator {
 private:
  template<typename Pointer>
  struct Traits {
    using iterator = typename OuterIterator::value_type::iterator;
  };

  template<typename Pointer> struct Traits<const Pointer*> {
    using iterator = typename OuterIterator::value_type::const_iterator;
  };

 public:
  using iterator_type = NestedIterator<OuterIterator>;

  using InnerIterator =
      typename Traits<typename OuterIterator::pointer>::iterator;
  using reference = typename InnerIterator::reference;
  using pointer = typename InnerIterator::pointer;

  NestedIterator(const OuterIterator& oi, const OuterIterator& oend,
      const InnerIterator& ii)
      : outer_iter_(oi), outer_end_(oend), inner_iter_(ii)
      {}
  NestedIterator(const OuterIterator& oi, const OuterIterator& oend)
      : outer_iter_(oi), outer_end_(oend) {
    if (outer_iter_ != outer_end_) {
      inner_iter_ = outer_iter_->begin();
      NextOuter();
    }
  }
  explicit NestedIterator(const OuterIterator& oend)
      : outer_iter_(oend), outer_end_(oend)
      {}

  reference operator*() const {
    assert(outer_iter_ != outer_end_);
    return inner_iter_.operator*();
  }

  pointer operator->() const {
    assert(outer_iter_ != outer_end_);
    return inner_iter_.operator->();
  }

  bool operator==(const iterator_type& other) const {
    if (outer_iter_ != other.outer_iter_) {
      return false;
    }

    if (outer_iter_ == outer_end_ && other.outer_iter_ == other.outer_end_) {
      return true;
    }

    if (outer_iter_ == outer_end_ || other.outer_iter_ == other.outer_end_) {
      return false;
    }

    if (inner_iter_ == other.inner_iter_) {
      return true;
    }

    return false;
  }

  bool operator!=(const iterator_type& other) const {
    return !(*this == other);
  }

  iterator_type& operator++() {
    assert(outer_iter_ != outer_end_);

    ++inner_iter_;
    if (inner_iter_ == outer_iter_->end()) {
      ++outer_iter_;
      NextOuter();
    }

    return *this;
  }

 private:
  void NextOuter() {
    while (outer_iter_ != outer_end_ &&
      outer_iter_->begin() == outer_iter_->end()) {
      ++outer_iter_;
    }
    if (outer_iter_ != outer_end_) {
      inner_iter_ = outer_iter_->begin();
    }
  }

  OuterIterator outer_iter_;
  OuterIterator outer_end_;
  InnerIterator inner_iter_;
};

#endif  // NESTED_ITERATOR_H_
