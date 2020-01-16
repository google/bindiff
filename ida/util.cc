// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/ida/util.h"

#include "third_party/absl/strings/string_view.h"

namespace security::binexport {

std::string ToString(const qstring& ida_string) {
  return std::string(ida_string.c_str(), ida_string.length());
}

absl::string_view ToStringView(const qstring& ida_string) {
  return absl::string_view(&*ida_string.begin(), ida_string.length());
}

}  // namespace security::binexport
