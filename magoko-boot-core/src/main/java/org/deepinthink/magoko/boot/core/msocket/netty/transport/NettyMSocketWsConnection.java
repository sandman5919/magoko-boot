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
package org.deepinthink.magoko.boot.core.msocket.netty.transport;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;

public class NettyMSocketWsConnection extends AbstractNettyMSocketConnection {
  public NettyMSocketWsConnection(Connection connection) {
    super(connection, MSocketServer.TransportType.WEB_SOCKET);
    connection()
        .outbound()
        .sendObject(processor().map(BinaryWebSocketFrame::new))
        .then()
        .subscribe();
  }

  @Override
  public Flux<ByteBuf> receive() {
    return connection().inbound().receive();
  }
}
