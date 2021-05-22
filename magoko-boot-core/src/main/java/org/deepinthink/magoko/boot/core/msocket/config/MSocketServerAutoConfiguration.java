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
package org.deepinthink.magoko.boot.core.msocket.config;

import org.deepinthink.magoko.boot.core.msocket.MSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.context.MSocketServerBootstrap;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyMSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyMSocketServerFactoryCustomizer;
import org.deepinthink.magoko.boot.core.msocket.netty.transport.NettyMSocketConnectionAcceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MSocketServerProperties.class)
public class MSocketServerAutoConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnProperty(prefix = MSocketServerProperties.PREFIX, name = "port")
  public class EmbeddedServerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NettyMSocketConnectionAcceptor nettyMSocketConnectionAcceptor() {
      return mSocketConnection -> Mono.never();
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyMSocketServerFactory nettyMSocketServerFactory(
        MSocketServerProperties properties,
        NettyMSocketConnectionAcceptor connectionAcceptor,
        ObjectProvider<NettyMSocketServerFactoryCustomizer> serverFactoryCustomizers) {
      NettyMSocketServerFactory serverFactory = new NettyMSocketServerFactory(connectionAcceptor);
      PropertyMapper propertyMapper = PropertyMapper.get();
      propertyMapper.from(properties::getHost).to(serverFactory::setHost);
      propertyMapper.from(properties::getPort).to(serverFactory::setPort);
      propertyMapper.from(properties::getTransportType).to(serverFactory::setTransportType);
      propertyMapper.from(properties::getMappingPath).to(serverFactory::setMappingPath);
      serverFactoryCustomizers.orderedStream().forEach(c -> c.customize(serverFactory));
      return serverFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public MSocketServerBootstrap mSocketServerBootstrap(MSocketServerFactory serverFactory) {
      return new MSocketServerBootstrap(serverFactory);
    }
  }
}
