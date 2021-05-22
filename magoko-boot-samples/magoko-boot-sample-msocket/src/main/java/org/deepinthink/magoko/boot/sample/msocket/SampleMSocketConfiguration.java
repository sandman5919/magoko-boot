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

import static org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketFrameLengthCodec.encode;

import io.netty.buffer.ByteBuf;
import org.deepinthink.magoko.boot.core.msocket.netty.transport.NettyMSocketConnectionAcceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class SampleMSocketConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public NettyMSocketConnectionAcceptor sampleConnectionAcceptor() {
    return nettyConnection -> {
      nettyConnection
          .receive()
          .map(ByteBuf::retain)
          .map(byteBuf -> encode(byteBuf.alloc(), byteBuf.readableBytes(), byteBuf))
          .subscribe(nettyConnection::sendFrame);
      return Mono.never();
    };
  }
}
