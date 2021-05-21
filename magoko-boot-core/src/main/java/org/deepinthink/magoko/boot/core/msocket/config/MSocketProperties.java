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

@ConfigurationProperties(MSocketProperties.PREFIX)
public class MSocketProperties {
  public static final String PREFIX = "magoko.boot.msocket";
  public static final int DEFAULT_SERVER_PORT = 8002;
  public static final String DEFAULT_MAPPING_PATH = "/ws";

  private final Server server = new Server();

  public Server getServer() {
    return server;
  }

  public static class Server {
    private InetAddress host;
    private int port = DEFAULT_SERVER_PORT;
    private MSocketServer.TransportType transportType = MSocketServer.TransportType.WEB_SOCKET;
    private String mappingPath = DEFAULT_MAPPING_PATH;

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
}
