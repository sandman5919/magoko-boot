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
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public final class NettyMSocketWsServerTransport implements NettyMSocketServerTransport {
  private final HttpServer httpServer;
  private final String mappingPath;

  final WebsocketServerSpec.Builder specBuilder =
      WebsocketServerSpec.builder().maxFramePayloadLength(FRAME_LENGTH_MASK);

  private NettyMSocketWsServerTransport(HttpServer httpServer, String mappingPath) {
    this.httpServer = Objects.requireNonNull(httpServer);
    this.mappingPath = mappingPath;
  }

  public static NettyMSocketWsServerTransport create(HttpServer httpServer, String mappingPath) {
    return new NettyMSocketWsServerTransport(httpServer, mappingPath);
  }

  public static NettyMSocketWsServerTransport create(
      Supplier<? extends SocketAddress> bindAddressSupplier, String mappingPath) {
    HttpServer httpServer = HttpServer.create().bindAddress(bindAddressSupplier);
    return create(httpServer, mappingPath);
  }

  @Override
  public Mono<? extends DisposableServer> bind(NettyMSocketConnectionAcceptor connectionAcceptor) {
    return this.httpServer
        .route(
            routes ->
                routes.ws(this.mappingPath, newHandler(connectionAcceptor), specBuilder.build()))
        .bind();
  }

  public static BiFunction<WebsocketInbound, WebsocketOutbound, Publisher<Void>> newHandler(
      NettyMSocketConnectionAcceptor connectionAcceptor) {
    return ((inbound, outbound) ->
        connectionAcceptor
            .apply(new NettyMSocketWsConnection((Connection) inbound))
            .then(outbound.neverComplete()));
  }
}
