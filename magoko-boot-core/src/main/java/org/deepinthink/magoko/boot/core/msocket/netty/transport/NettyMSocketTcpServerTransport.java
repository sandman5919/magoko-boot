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

import static org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketFrameLengthCodec.FRAME_LENGTH_MASK;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.function.Supplier;
import org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketLengthCodec;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public final class NettyMSocketTcpServerTransport implements NettyMSocketServerTransport {
  private final TcpServer tcpServer;

  private NettyMSocketTcpServerTransport(TcpServer tcpServer) {
    this.tcpServer = Objects.requireNonNull(tcpServer);
  }

  public static NettyMSocketTcpServerTransport create(TcpServer tcpServer) {
    return new NettyMSocketTcpServerTransport(tcpServer);
  }

  public static NettyMSocketTcpServerTransport create(
      Supplier<? extends SocketAddress> bindAddressSupplier) {
    TcpServer tcpServer = TcpServer.create().bindAddress(bindAddressSupplier);
    return new NettyMSocketTcpServerTransport(tcpServer);
  }

  @Override
  public Mono<? extends DisposableServer> bind(NettyMSocketConnectionAcceptor connectionAcceptor) {
    return this.tcpServer
        .doOnConnection(
            connection -> {
              connection.addHandlerFirst(new NettyMSocketLengthCodec(FRAME_LENGTH_MASK));
              connectionAcceptor
                  .apply(new NettyMSocketTcpConnection(connection))
                  .then(Mono.<Void>never())
                  .subscribe(connection.disposeSubscriber());
            })
        .bind();
  }
}
