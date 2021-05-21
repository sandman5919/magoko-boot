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

import java.util.stream.Collectors;
import org.deepinthink.magoko.boot.core.msocket.MSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.context.MSocketServerBootstrap;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyMSocketServerFactory;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyMSocketServerFactoryCustomizer;
import org.deepinthink.magoko.boot.core.msocket.netty.NettyTcpServerCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MSocketProperties.class)
public class MSocketServerAutoConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnProperty(prefix = MSocketProperties.PREFIX + ".server", name = "port")
  static class EmbeddedServerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MSocketServerFactory mSocketServerFactory(
        MSocketProperties properties,
        ObjectProvider<NettyMSocketServerFactoryCustomizer> serverFactoryCustomizers,
        ObjectProvider<NettyTcpServerCustomizer> tcpServerCustomizers) {
      NettyMSocketServerFactory serverFactory = new NettyMSocketServerFactory();
      PropertyMapper mapper = PropertyMapper.get();
      mapper.from(properties.getServer()::getHost).to(serverFactory::setHost);
      mapper.from(properties.getServer()::getPort).to(serverFactory::setPort);
      mapper.from(properties.getServer()::getTransportType).to(serverFactory::setTransportType);
      mapper.from(properties.getServer()::getMappingPath).to(serverFactory::setMappingPath);
      mapper
          .from(tcpServerCustomizers.stream().collect(Collectors.toList()))
          .to(serverFactory::setCustomizers);
      serverFactoryCustomizers
          .orderedStream()
          .forEach(customizer -> customizer.customize(serverFactory));
      return serverFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public MSocketServerBootstrap mSocketServerBootstrap(MSocketServerFactory serverFactory) {
      return new MSocketServerBootstrap(serverFactory);
    }
  }
}
