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

import java.util.Arrays;

public class SimpleDataProvider implements IDataProvider {
  private final byte[] m_data;

  public SimpleDataProvider(final byte[] data) {
    this.m_data = data;
  }

  @Override
  public void addListener(final IDataChangedListener hexView) {
    // TODO: Implement listener handling, or throw exception
  }

  @Override
  public byte[] getData() {
    return getData(0, getDataLength());
  }

  @Override
  public byte[] getData(final long offset, final int length) {
    return Arrays.copyOfRange(m_data, (int) offset, (int) (offset + length));
  }

  @Override
  public int getDataLength() {
    return m_data.length;
  }

  public long getOffset() {
    // Always return zero
    return 0;
  }

  @Override
  public boolean hasData(final long start, final int length) {
    return true;
  }

  @Override
  public boolean isEditable() {
    return true;
  }

  @Override
  public boolean keepTrying() {
    return true;
  }

  @Override
  public void removeListener(final IDataChangedListener m_listener) {
    // TODO: Implement listener handling, or throw exception
  }

  @Override
  public void setData(final long offset, final byte[] data) {
    System.arraycopy(data, 0, m_data, (int) offset, data.length);
  }
}
