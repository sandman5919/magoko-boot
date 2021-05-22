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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import org.deepinthink.magoko.boot.core.msocket.ConfigurableMSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import org.deepinthink.magoko.boot.core.msocket.MSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.netty.transport.*;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

public final class NettyMSocketServerFactory
    implements MSocketServerFactory, ConfigurableMSocketServerFactory {
  private final NettyMSocketConnectionAcceptor connectionAcceptor;

  private InetAddress host;
  private int port;
  private MSocketServer.TransportType transportType;
  private LoopResources loopResources;
  private String mappingPath;
  private Duration lifecycleTimeout;

  public NettyMSocketServerFactory(NettyMSocketConnectionAcceptor connectionAcceptor) {
    this.connectionAcceptor = Objects.requireNonNull(connectionAcceptor);
  }

  @Override
  public MSocketServer createServer() {
    NettyMSocketServerTransport serverTransport = createServerTransport();
    Mono<? extends DisposableServer> serverStarter = serverTransport.bind(this.connectionAcceptor);
    return new NettyMSocketServer(serverStarter, this.lifecycleTimeout);
  }

  private NettyMSocketServerTransport createServerTransport() {
    return (this.transportType == MSocketServer.TransportType.WEB_SOCKET)
        ? createWsServerTransport()
        : createTcpServerTransport();
  }

  private NettyMSocketWsServerTransport createWsServerTransport() {
    if (Objects.nonNull(this.loopResources)) {
      HttpServer httpServer =
          HttpServer.create()
              .tcpConfiguration(tcpServer -> tcpServer.runOn(this.loopResources))
              .bindAddress(this::getListenAddress);
      return NettyMSocketWsServerTransport.create(httpServer, this.mappingPath);
    }
    return NettyMSocketWsServerTransport.create(this::getListenAddress, this.mappingPath);
  }

  private NettyMSocketTcpServerTransport createTcpServerTransport() {
    if (Objects.nonNull(this.loopResources)) {
      TcpServer tcpServer =
          TcpServer.create().runOn(this.loopResources).bindAddress(this::getListenAddress);
      return NettyMSocketTcpServerTransport.create(tcpServer);
    }
    return NettyMSocketTcpServerTransport.create(this::getListenAddress);
  }

  @Override
  public void setHost(InetAddress host) {
    this.host = host;
  }

  @Override
  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public void setTransportType(MSocketServer.TransportType transportType) {
    this.transportType = transportType;
  }

  public void setLoopResources(LoopResources loopResources) {
    this.loopResources = loopResources;
  }

  @Override
  public void setMappingPath(String mappingPath) {
    this.mappingPath = mappingPath;
  }

  @Override
  public void setLifecycleTimeout(Duration lifecycleTimeout) {
    this.lifecycleTimeout = lifecycleTimeout;
  }

  private InetSocketAddress getListenAddress() {
    return Objects.isNull(this.host)
        ? new InetSocketAddress(this.port)
        : new InetSocketAddress(this.host, this.port);
  }
}
