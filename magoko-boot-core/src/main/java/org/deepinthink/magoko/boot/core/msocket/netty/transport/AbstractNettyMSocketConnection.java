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
import java.util.Objects;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.netty.Connection;

public abstract class AbstractNettyMSocketConnection implements NettyMSocketConnection {
  private final Connection connection;
  private final MSocketServer.TransportType transportType;
  private final EmitterProcessor<ByteBuf> processor;
  private final FluxSink<ByteBuf> sink;

  protected AbstractNettyMSocketConnection(
      Connection connection, MSocketServer.TransportType transportType) {
    this.connection = Objects.requireNonNull(connection);
    this.transportType = transportType;
    this.processor = EmitterProcessor.create();
    this.sink = this.processor.sink();
    this.processor.doOnComplete(
        () -> {
          this.processor.dispose();
          this.sink.complete();
        });

    this.connection
        .channel()
        .closeFuture()
        .addListener(
            future -> {
              if (!isDisposed()) {
                dispose();
              }
            });
  }

  @Override
  public Connection connection() {
    return this.connection;
  }

  @Override
  public EmitterProcessor<ByteBuf> processor() {
    return this.processor;
  }

  @Override
  public void sendFrame(ByteBuf byteBuf) {
    if (!this.processor.isDisposed()) {
      this.sink.next(byteBuf);
    }
  }

  @Override
  public MSocketServer.TransportType transportType() {
    return this.transportType;
  }
}
