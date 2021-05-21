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

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableChannel;
import reactor.netty.DisposableServer;

final class NettyMSocketServer implements MSocketServer {
  public static final String AWAIT_DAEMON_THREAD_NAME = "MSocket";
  private final Mono<? extends DisposableServer> serverStarter;
  private final Duration lifecycleTimeout;
  private DisposableServer server;

  NettyMSocketServer(Mono<? extends DisposableServer> serverStarter, Duration lifecycleTimeout) {
    this.serverStarter = Objects.requireNonNull(serverStarter);
    this.lifecycleTimeout = lifecycleTimeout;
  }

  @Override
  public void start() {
    this.server = this.block(serverStarter, this.lifecycleTimeout);
    this.startAwaitDaemonThread(this.server);
  }

  private void startAwaitDaemonThread(DisposableServer server) {
    Thread thread = new Thread(() -> server.onDispose().block(), AWAIT_DAEMON_THREAD_NAME);
    thread.setContextClassLoader(getClass().getClassLoader());
    thread.setDaemon(false);
    thread.start();
  }

  @Override
  public void stop() {
    Optional.ofNullable(this.server).ifPresent(DisposableChannel::disposeNow);
  }

  @Override
  public boolean isRunning() {
    return Objects.nonNull(this.server) && !this.server.isDisposed();
  }

  public <T> T block(Mono<T> mono, Duration timeout) {
    return Objects.isNull(timeout) ? mono.block() : mono.block(timeout);
  }
}
