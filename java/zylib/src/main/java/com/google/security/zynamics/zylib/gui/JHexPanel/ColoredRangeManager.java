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

package com.google.security.zynamics.zylib.gui.JHexPanel;

import java.util.ArrayList;
import java.util.Collections;

public class ColoredRangeManager {
  private final ArrayList<ColoredRange> ranges = new ArrayList<ColoredRange>();

  public void addRange(final ColoredRange range) {
    ranges.add(range);

    Collections.sort(ranges);
  }

  public void clear() {
    ranges.clear();
  }

  public ColoredRange findRange(final long offset) {
    for (final ColoredRange range : ranges) {
      if (range.getStart() >= offset) {
        return range;
      }
    }

    return null;
  }

  public ColoredRange findRangeWith(final long offset) {
    for (final ColoredRange range : ranges) {
      if (range.containsOffset(offset)) {
        return range;
      }
    }

    return null;
  }

  public void removeRange(final long offset, final int size) {
    // Try to find the range that contains the offset
    ColoredRange range = findRangeWith(offset);

    // If there is no such range, at least find the range right
    // before the offset.
    if (range == null) {
      range = findRange(offset);
    }

    // If there is no such range then there is nothing to remove.
    if (range == null) {
      return;
    }

    // TODO: The <= is a hack; used to be just ==; Check this again
    if (offset <= range.getStart()) {
      // The offset starts exactly at the range. That means
      // we start decolorizing at the beginning of the range.

      if (range.getSize() == size) {
        // If the sizes are equal the entire range is decolorized and we're done.

        ranges.remove(range);
      } else if (range.getSize() < size) {
        // If the range is smaller the entire range is decolorized and the
        // remaining bytes are decolorized in the next step.

        ranges.remove(range);
        removeRange(range.getStart() + range.getSize(), size - range.getSize());
      } else
      // if (range.getSize() > size)
      {
        // If the range is larger than the decolorizing area, ditch the
        // range and add a new range that contains the area that stays colorized.

        ranges.remove(range);
        addRange(new ColoredRange(offset + size, range.getSize() - size, range.getColor(),
            range.getBackgroundColor()));
      }
    } else
    // if (offset > range.getStart())
    {
      // We start to decolorize somewhere in the middle of the range.

      if ((offset + size) == (range.getStart() + range.getSize())) {
        // If the last offset of the range and the decolorizing area is equal,
        // then the latter part of the range is ditched. Get rid of the range
        // and create a new range that represents the first part of the area.

        ranges.remove(range);

        final long newStart = range.getStart();
        final int newSize = range.getSize() - size;
        // int newSize = (int)(offset - range.getStart());
        addRange(new ColoredRange(newStart, newSize, range.getColor(), range.getBackgroundColor()));
      } else if ((offset + size) < (range.getStart() + range.getSize())) {
        // Some part is cut out of the middle of the range. Get rid of the old range
        // and create two new ranges for the first part and last part of the old range.

        ranges.remove(range);

        final long newStartFirst = range.getStart();
        final int newSizeFirst = (int) (offset - range.getStart());

        addRange(new ColoredRange(newStartFirst, newSizeFirst, range.getColor(),
            range.getBackgroundColor()));

        final long newStartLast = offset + size;
        final int newSizeLast = (int) ((range.getStart() + range.getSize()) - offset - size);

        addRange(new ColoredRange(newStartLast, newSizeLast, range.getColor(),
            range.getBackgroundColor()));
      } else
      // if (offset + size > range.getStart() + range.getSize())
      {
        // More than the current range must be decolorized. That means the entire range
        // except for the start is ditched.

        ranges.remove(range);

        final long newStart = range.getStart();
        final int newSize = (int) (offset - range.getStart());

        addRange(new ColoredRange(newStart, newSize, range.getColor(), range.getBackgroundColor()));
        removeRange(range.getStart() + range.getSize(),
            size - (int) ((range.getStart() + range.getSize()) - offset));
      }
    }
  }
}
