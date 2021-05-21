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
package org.deepinthink.magoko.boot.sample.msocket;

import java.time.Duration;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyMSocketServerFactoryCustomizer;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyTcpServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SampleMSocketConfig {

  @Bean
  public NettyMSocketServerFactoryCustomizer mSocketServerFactoryCustomizer() {
    return serverFactory -> serverFactory.setReadIdleTimeout(Duration.ofSeconds(3));
  }

  // telnet localhost 8002
  @Bean
  public NettyTcpServerCustomizer nettyTcpServerCustomizer() {
    return tcpServer -> tcpServer.doOnConnection(System.out::println);
  }
}
