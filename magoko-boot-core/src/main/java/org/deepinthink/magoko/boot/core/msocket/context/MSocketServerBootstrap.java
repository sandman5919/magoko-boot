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
package org.deepinthink.magoko.boot.core.msocket.context;

import java.util.Objects;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import org.deepinthink.magoko.boot.core.msocket.MSocketServerFactory;
import org.springframework.context.*;

public final class MSocketServerBootstrap
    implements ApplicationEventPublisherAware, SmartLifecycle {
  private final MSocketServer server;
  private ApplicationEventPublisher eventPublisher;

  public MSocketServerBootstrap(MSocketServerFactory serverFactory) {
    this.server = Objects.requireNonNull(serverFactory.createServer());
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void start() {
    this.server.start();
    this.eventPublisher.publishEvent(new MSocketServerInitializedEvent(this.server));
  }

  @Override
  public void stop() {
    this.server.stop();
  }

  @Override
  public boolean isRunning() {
    return this.server.isRunning();
  }
}
