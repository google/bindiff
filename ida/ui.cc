// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/binexport/ida/ui.h"

#ifdef WIN32
#include <windows.h>
#endif

#include <string>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <expr.hpp>                                             // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/process.h"

namespace {

std::string FormatMessage(absl::string_view message, bool cancellable) {
  return (cancellable ? "" : "HIDECANCEL\n") + std::string(message);
}

}  // namespace

WaitBox::WaitBox(absl::string_view message, WaitBox::Cancellable cancel_state)
    : cancellable_(cancel_state == WaitBox::kCancellable) {
  show_wait_box("%s", FormatMessage(message, cancellable_).c_str());
}

WaitBox::~WaitBox() { hide_wait_box(); }

bool WaitBox::IsCancelled() { return user_cancelled(); }

void WaitBox::ReplaceText(absl::string_view message) const {
  replace_wait_box("%s", FormatMessage(message, cancellable_).c_str());
}

absl::Status CopyToClipboard(absl::string_view data) {
#ifdef _WIN32
  using ::security::binexport::GetLastOsError;

  if (!OpenClipboard(0)) {
    return absl::UnknownError(GetLastOsError());
  }
  struct ClipboardCloser {
    ~ClipboardCloser() { CloseClipboard(); }
  } deleter;

  if (!EmptyClipboard()) {
    return absl::UnknownError(GetLastOsError());
  }

  // Allocate one byte more than the string size because the CF_TEXT format
  // expects a NUL byte at the end.
  HGLOBAL buffer_handle =
      GlobalAlloc(GMEM_MOVEABLE | GMEM_ZEROINIT, data.size() + 1);
  if (!buffer_handle) {
    return absl::UnknownError(GetLastOsError());
  }

  bool fail = true;
  auto* buffer = static_cast<char*>(GlobalLock(buffer_handle));
  if (buffer) {
    memcpy(buffer, data.data(), data.size());
    if (GlobalUnlock(buffer) &&
        SetClipboardData(CF_TEXT, buffer_handle /* Transfer ownership */)) {
      fail = false;
    }
  }
  if (fail) {
    // Only free on failure, as SetClipboardData() takes ownership.
    GlobalFree(buffer_handle);
    return absl::UnknownError(GetLastOsError());
  }
#else
  // Clipboard handling on Linux is complicated. Luckily, IDA uses Qt and
  // exposes Python bindings. Since Qt abstracts away all the platform
  // differences, we just use a tiny IDAPython script to copy plain text data
  // to clipboard.
  extlang_object_t python = find_extlang_by_name("Python");
  if (python.operator->() == nullptr) {  // Need to call operator -> directly
    return absl::InternalError("Cannot find IDAPyton");
  }
  qstring error;
  std::string escaped_snippet;
  escaped_snippet.reserve(data.size() * 4);
  for (const auto& c : data) {
    // Unconditionally convert to Python hex escape sequence to be binary safe.
    absl::StrAppend(&escaped_snippet, "\\x", absl::Hex(c, absl::kZeroPad2));
  }
  if (!python->eval_snippet(
          absl::StrCat(
              "from PyQt5 import Qt; cb = Qt.QApplication.clipboard(); "
              "cb.setText('",
              escaped_snippet, "', mode=cb.Clipboard)")
              .c_str(),
          &error)) {
    return absl::InternalError(error.c_str());
  }
#endif
  return absl::OkStatus();
}
