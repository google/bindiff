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

#include "third_party/zynamics/bindiff/match_colors.h"

#include <cerrno>
#include <cmath>
#include <cstdlib>
#include <limits>

#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_replace.h"
#include "third_party/absl/strings/strip.h"
#include "third_party/absl/types/optional.h"
#include "third_party/zynamics/bindiff/config.h"

namespace security::bindiff {

// Parses a hex color triplet in value into a number.
absl::optional<int64_t> HexColorToInt(absl::string_view value) {
  const std::string color_string(
      absl::StripPrefix(absl::StripAsciiWhitespace(value), "#"));
  if (color_string.size() != 6) {
    return absl::nullopt;
  }
  errno = 0;
  uint64_t v = std::strtoul(  // NOLINT(runtime/deprecated_fn)
      color_string.c_str(), nullptr, 16);
  if (errno == ERANGE) {
    return absl::nullopt;
  }
  return v;
}

template <typename StringsT>
std::vector<uint32_t> ParseColorRamp(const StringsT& color_strings) {
  std::vector<uint32_t> color_ramp;
  color_ramp.reserve(color_strings.size());
  for (const auto& color_string : color_strings) {
    if (const auto color = HexColorToInt(color_string)) {
      color_ramp.push_back(*color & std::numeric_limits<uint32_t>::max());
    }
  }
  return color_ramp;
}

uint32_t GetMatchColor(double value) {
  static auto& default_theme =
      config::Defaults().themes().find("Google Material")->second;

  static const auto& theme = []() -> const Config::UiTheme& {
    auto& config = config::Proto();
    const auto& preferences = config.preferences();
    auto found = config.themes().find(preferences.use_theme());
    return found != config.themes().end() ? found->second : default_theme;
  }();

  static auto* color_ramp = []() -> std::vector<uint32_t>* {
    auto* color_ramp =
        new std::vector<uint32_t>(ParseColorRamp(theme.similarity_ramp()));
    if (color_ramp->empty() ||
        color_ramp->size() != theme.similarity_ramp_size()) {
      // Parse error, set default color ramp. This should never fail, as the
      // data is embedded.
      *color_ramp = ParseColorRamp(default_theme.similarity_ramp());
    }
    return color_ramp;
  }();

  static uint32_t manual_match_color = *HexColorToInt(theme.manual_match());

  uint32_t color = 0xffffff;  // Fallback to white
  if (value == kManualMatch) {
    color = manual_match_color;
  } else if (size_t ramp_size = color_ramp->size();
             ramp_size > 0 && value >= 0.0 && value <= 1.0) {
    color = (*color_ramp)[static_cast<int>(value * (ramp_size - 1))];
  }
  return (color << 16) | (color & 0xff00) | (color >> 16);
}

}  // namespace security::bindiff
