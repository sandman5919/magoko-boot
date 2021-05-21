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

import io.netty.channel.ChannelHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.deepinthink.magoko.boot.core.msocket.ConfigurableMSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import org.deepinthink.magoko.boot.core.msocket.MSocketServerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public class NettyMSocketServerFactory
    implements MSocketServerFactory, ConfigurableMSocketServerFactory {
  private InetAddress host;
  private int port;
  private MSocketServer.TransportType transportType;
  private String mappingPath;
  private Duration lifecycleTimeout;
  private Duration readIdleTimeout;
  private List<NettyTcpServerCustomizer> customizers;

  @Override
  public MSocketServer createServer() {
    AtomicReference<TcpServer> tcpServerRef =
        new AtomicReference<>(
            TcpServer.create()
                .bindAddress(this::getListenAddress)
                .doOnConnection(this::initializeConnection));
    Optional.ofNullable(this.customizers)
        .ifPresent(
            customizers ->
                customizers.forEach(
                    customizer -> tcpServerRef.set(customizer.customize(tcpServerRef.get()))));
    return Mono.fromSupplier(tcpServerRef::get)
        .map(tcpServer -> tcpServer.doOnConnection(this::createMessagingMSocket))
        .map(TcpServer::bind)
        .map(this::createNettyServer)
        .block();
  }

  private void createMessagingMSocket(Connection connection) {
    // new MessagingMSocket(connection, this.readIdleTimeout);
  }

  private void initializeConnection(Connection connection) {
    this.createChannelHandlers().forEach(connection::addHandlerFirst);
  }

  private NettyMSocketServer createNettyServer(Mono<? extends DisposableServer> starter) {
    return new NettyMSocketServer(starter, lifecycleTimeout);
  }

  private List<ChannelHandler> createChannelHandlers() {
    return (this.transportType == MSocketServer.TransportType.WEB_SOCKET)
        ? NettyMSocketChanelHandler.createWebSocketChannelHandlers(this.mappingPath)
        : NettyMSocketChanelHandler.createTcpChannelHandlers();
  }

  private SocketAddress getListenAddress() {
    return Objects.isNull(this.host)
        ? new InetSocketAddress(this.port)
        : new InetSocketAddress(this.host.getHostAddress(), this.port);
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

  @Override
  public void setMappingPath(String mappingPath) {
    this.mappingPath = mappingPath;
  }

  @Override
  public void setLifecycleTimeout(Duration lifecycleTimeout) {
    this.lifecycleTimeout = lifecycleTimeout;
  }

  @Override
  public void setReadIdleTimeout(Duration readIdleTimeout) {
    this.readIdleTimeout = readIdleTimeout;
  }

  public void setCustomizers(List<NettyTcpServerCustomizer> customizers) {
    this.customizers = customizers;
  }
}
