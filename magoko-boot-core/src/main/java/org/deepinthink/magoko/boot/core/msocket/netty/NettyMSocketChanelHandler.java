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
package org.deepinthink.magoko.boot.core.msocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.util.Arrays;
import java.util.List;

abstract class NettyMSocketChanelHandler {

  static List<ChannelHandler> createWebSocketChannelHandlers(String mappingPath) {
    return Arrays.asList(
        new HttpServerCodec(),
        new HttpObjectAggregator(65536),
        new ChunkedWriteHandler(),
        new WebSocketServerCompressionHandler(),
        new WebSocketServerProtocolHandler(mappingPath, null, true),
        new MessageToMessageDecoder<WebSocketFrame>() {
          @Override
          protected void decode(
              ChannelHandlerContext context, WebSocketFrame frame, List<Object> in) {
            if (frame instanceof BinaryWebSocketFrame) {
              BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
              ByteBuf byteBuf = binaryFrame.content();
              byteBuf.retain();
            }
          }
        });
  }

  static List<ChannelHandler> createTcpChannelHandlers() {
    return Arrays.asList();
  }
}
