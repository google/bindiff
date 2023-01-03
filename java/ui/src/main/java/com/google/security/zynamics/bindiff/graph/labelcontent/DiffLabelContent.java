// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.labelcontent;

import com.google.security.zynamics.bindiff.enums.EPlaceholderType;
import com.google.security.zynamics.bindiff.graph.labelcontent.editableline.PlaceholderObject;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import java.util.List;

public class DiffLabelContent extends ZyLabelContent {
  public DiffLabelContent(
      final IZyEditableObject nodeModel, final boolean selectable, final boolean editable) {
    super(nodeModel, selectable, false); // TODO: Implement node comment editing! // editable);
  }

  @Override
  public int getFirstLineIndexOfModelAt(final int lineYPos) {
    final ZyLineContent lineContent = getLineContent(lineYPos);

    if (lineContent != null) {
      final IZyEditableObject lineModel = lineContent.getLineObject();

      int indexCopy = lineYPos;

      if (lineModel.isPlaceholder()) {
        final EPlaceholderType type = ((PlaceholderObject) lineModel).getPlaceholderType();

        switch (type) {
          case MATCHED_ABOVE_INSTRUCTION_COMMENT:
            {
              while (--indexCopy > 0) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()
                    && ((PlaceholderObject) lineObject).getPlaceholderType()
                        == EPlaceholderType.MATCHED_ABOVE_INSTRUCTION_COMMENT) {
                  continue;
                }

                break;
              }

              return indexCopy + 1;
            }
          case MATCHED_BEHIND_INSTRUCTION_COMMENT:
            {
              while (--indexCopy > 0) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()) {
                  continue;
                }

                break;
              }

              indexCopy = super.getFirstLineIndexOfModelAt(indexCopy);

              while (--indexCopy > 0) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()
                    && ((PlaceholderObject) lineObject).getPlaceholderType()
                        == EPlaceholderType.MATCHED_ABOVE_INSTRUCTION_COMMENT) {
                  continue;
                }

                break;
              }

              ++indexCopy;

              return indexCopy;
            }
          case UNMATCHED_ABOVE_INSTRUCTION_COMMENT:
          case UNMATCHED_INSTRUCTION:
          case UNMATCHED_BEHIND_INSTRUCTION_COMMENT:
            {
              boolean wasUnmatchedInstruction =
                  type == EPlaceholderType.UNMATCHED_INSTRUCTION
                      || type == EPlaceholderType.UNMATCHED_ABOVE_INSTRUCTION_COMMENT;

              while (--indexCopy > 0) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()) {
                  final EPlaceholderType placeholderType =
                      ((PlaceholderObject) lineObject).getPlaceholderType();

                  if (wasUnmatchedInstruction) {
                    if (placeholderType == EPlaceholderType.UNMATCHED_INSTRUCTION
                        || placeholderType
                            == EPlaceholderType.UNMATCHED_BEHIND_INSTRUCTION_COMMENT) {
                      break;
                    }
                  } else {
                    wasUnmatchedInstruction =
                        placeholderType == EPlaceholderType.UNMATCHED_INSTRUCTION
                            || placeholderType
                                == EPlaceholderType.UNMATCHED_ABOVE_INSTRUCTION_COMMENT;
                  }

                  continue;
                } else {
                  break;
                }
              }

              ++indexCopy;

              return indexCopy;
            }
          case BASIC_BLOCK_COMMENT:
            {
              while (--indexCopy > 0) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()
                    && ((PlaceholderObject) lineObject).getPlaceholderType()
                        == EPlaceholderType.BASIC_BLOCK_COMMENT) {
                  continue;
                }

                if (lineObject.getPersistentModel() instanceof RawBasicBlock) {
                  return super.getFirstLineIndexOfModelAt(indexCopy);
                }

                ++indexCopy;

                return indexCopy;
              }
            }
        }
      } else {
        while (--indexCopy >= 0) {
          final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

          if (getContent().get(indexCopy).getLineObject() != lineModel) {
            if (lineObject.isPlaceholder()
                && ((PlaceholderObject) lineObject).getPlaceholderType()
                    == EPlaceholderType.MATCHED_ABOVE_INSTRUCTION_COMMENT) {
              continue;
            }

            ++indexCopy;

            return indexCopy;
          }
        }
      }
    }

    return lineYPos;
  }

  @Override
  public int getLastLineIndexOfModelAt(final int lineYPos) {
    final List<ZyLineContent> content = getContent();

    final int lineCount = content.size();

    final ZyLineContent lineContent = getLineContent(lineYPos);

    if (lineContent != null) {
      final IZyEditableObject lineModel = lineContent.getLineObject();

      int indexCopy = lineYPos;

      if (lineModel.isPlaceholder()) {
        final EPlaceholderType type = ((PlaceholderObject) lineModel).getPlaceholderType();

        switch (type) {
          case MATCHED_ABOVE_INSTRUCTION_COMMENT:
            {
              while (++indexCopy < lineCount) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()) {
                  continue;
                }
                break;
              }

              indexCopy = super.getLastLineIndexOfModelAt(indexCopy);

              while (++indexCopy < lineCount) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()
                    && ((PlaceholderObject) lineObject).getPlaceholderType()
                        == EPlaceholderType.MATCHED_BEHIND_INSTRUCTION_COMMENT) {
                  continue;
                }

                --indexCopy;

                break;
              }

              if (indexCopy >= lineCount) {
                indexCopy = lineCount - 1;
              }

              return indexCopy;
            }
          case MATCHED_BEHIND_INSTRUCTION_COMMENT:
            {
              while (++indexCopy < lineCount) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()
                    && ((PlaceholderObject) lineObject).getPlaceholderType()
                        == EPlaceholderType.MATCHED_BEHIND_INSTRUCTION_COMMENT) {
                  continue;
                }

                break;
              }

              return lineCount - 1;
            }
          case UNMATCHED_ABOVE_INSTRUCTION_COMMENT:
          case UNMATCHED_INSTRUCTION:
          case UNMATCHED_BEHIND_INSTRUCTION_COMMENT:
            {
              boolean wasUnmatchedInstruction =
                  type == EPlaceholderType.UNMATCHED_INSTRUCTION
                      || type == EPlaceholderType.UNMATCHED_BEHIND_INSTRUCTION_COMMENT;

              while (++indexCopy < lineCount) {
                final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

                if (lineObject.isPlaceholder()) {
                  final EPlaceholderType placeholderType =
                      ((PlaceholderObject) lineObject).getPlaceholderType();

                  if (wasUnmatchedInstruction) {
                    if (placeholderType == EPlaceholderType.UNMATCHED_INSTRUCTION
                        || placeholderType
                            == EPlaceholderType.UNMATCHED_ABOVE_INSTRUCTION_COMMENT) {
                      break;
                    }
                  } else {
                    wasUnmatchedInstruction =
                        placeholderType == EPlaceholderType.UNMATCHED_INSTRUCTION
                            || placeholderType
                                == EPlaceholderType.UNMATCHED_BEHIND_INSTRUCTION_COMMENT;
                  }

                  continue;
                } else {
                  break;
                }
              }

              --indexCopy;

              return indexCopy;
            }
          case BASIC_BLOCK_COMMENT:
            {
              return lineCount - 1;
            }
        }
      } else {
        while (++indexCopy < lineCount) {
          if (content.get(indexCopy).getLineObject() != lineModel) {
            final IZyEditableObject lineObject = getContent().get(indexCopy).getLineObject();

            if (lineObject.isPlaceholder()
                && ((PlaceholderObject) lineObject).getPlaceholderType()
                    == EPlaceholderType.MATCHED_BEHIND_INSTRUCTION_COMMENT) {
              continue;
            }

            --indexCopy;

            return indexCopy;
          }
        }
      }

      return lineCount - 1;
    }

    return lineYPos;
  }
}
