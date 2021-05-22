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

import java.net.InetAddress;
import org.deepinthink.magoko.boot.core.msocket.MSocketServer;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MSocketServerProperties.PREFIX)
public class MSocketServerProperties {
  public static final String PREFIX = "magoko.boot.msocket.server";

  private InetAddress host;
  private int port;
  private MSocketServer.TransportType transportType = MSocketServer.TransportType.WEB_SOCKET;
  private String mappingPath = "/ws";

  public InetAddress getHost() {
    return host;
  }

  public void setHost(InetAddress host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public MSocketServer.TransportType getTransportType() {
    return transportType;
  }

  public void setTransportType(MSocketServer.TransportType transportType) {
    this.transportType = transportType;
  }

  public String getMappingPath() {
    return mappingPath;
  }

  public void setMappingPath(String mappingPath) {
    this.mappingPath = mappingPath;
  }
}
