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

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <kernwin.hpp>                                       // NOLINT

// Due to conflicting integer types, include this header after kernwin.hpp.
#include "third_party/zynamics/binexport/ida/ui.h"  // NOLINT

namespace {

std::string FormatMessage(StringPiece message, bool cancellable) {
  return (cancellable ? "" : "HIDECANCEL\n") + std::string(message);
}

}  // namespace

WaitBox::WaitBox(StringPiece message, WaitBox::Cancellable cancel_state)
    : cancellable_(cancel_state == WaitBox::kCancellable) {
  show_wait_box("%s", FormatMessage(message, cancellable_).c_str());
}

WaitBox::~WaitBox() { hide_wait_box(); }

bool WaitBox::IsCancelled() { return wasBreak(); }

void WaitBox::ReplaceText(StringPiece message) const {
  replace_wait_box("%s", FormatMessage(message, cancellable_).c_str());
}
