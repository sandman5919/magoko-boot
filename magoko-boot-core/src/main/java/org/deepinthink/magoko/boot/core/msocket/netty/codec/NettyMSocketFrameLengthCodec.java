/*
 * Copyright 2021-present DEEPINTHINK. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deepinthink.magoko.boot.core.msocket.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class NettyMSocketFrameLengthCodec {
  public static final int FRAME_LENGTH_MASK = 0xFFFFFF;
  public static final int FRAME_LENGTH_SIZE = 3;

  private static void encodeLength(final ByteBuf byteBuf, final int length) {
    if ((length & ~FRAME_LENGTH_MASK) != 0) {
      throw new IllegalArgumentException("Length is larger than 24 bits");
    }
    // Write each byte separately in reverse order, this mean we can write 1 << 23 without
    // overflowing.
    byteBuf.writeByte(length >> 16);
    byteBuf.writeByte(length >> 8);
    byteBuf.writeByte(length);
  }

  private static int decodeLength(final ByteBuf byteBuf) {
    int length = (byteBuf.readByte() & 0xFF) << 16;
    length |= (byteBuf.readByte() & 0xFF) << 8;
    length |= byteBuf.readByte() & 0xFF;
    return length;
  }

  public static ByteBuf encode(ByteBufAllocator allocator, int length, ByteBuf frame) {
    ByteBuf buffer = allocator.buffer();
    encodeLength(buffer, length);
    return allocator.compositeBuffer(2).addComponents(true, buffer, frame);
  }

  public static int length(ByteBuf byteBuf) {
    byteBuf.markReaderIndex();
    int length = decodeLength(byteBuf);
    byteBuf.resetReaderIndex();
    return length;
  }

  public static ByteBuf frame(ByteBuf byteBuf) {
    byteBuf.markReaderIndex();
    byteBuf.skipBytes(FRAME_LENGTH_SIZE);
    ByteBuf slice = byteBuf.slice();
    byteBuf.resetReaderIndex();
    return slice;
  }
}
