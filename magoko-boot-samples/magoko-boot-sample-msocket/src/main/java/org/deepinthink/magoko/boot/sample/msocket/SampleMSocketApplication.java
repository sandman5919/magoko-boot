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

import static org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketFrameLengthCodec.FRAME_LENGTH_MASK;
import static org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketFrameLengthCodec.encode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.deepinthink.magoko.boot.core.MagOKOBanner;
import org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketFrameLengthCodec;
import org.deepinthink.magoko.boot.core.msocket.netty.codec.NettyMSocketLengthCodec;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;

@SpringBootApplication
public class SampleMSocketApplication implements CommandLineRunner {
  public static void main(String[] args) {
    new SpringApplicationBuilder()
        .sources(SampleMSocketApplication.class)
        .banner(MagOKOBanner.builder().tag("Sample MSocket Server Application").build())
        .run(args);
  }

  @Override
  public void run(String... args) throws Exception {
    TcpClient.create()
        .port(8002)
        .doOnConnected(this::createConnection)
        .doOnDisconnected(c -> System.out.println("disconnected"))
        .connectNow();
  }

  private void createConnection(Connection connection) {
    new ClientConnection(
        connection.addHandlerFirst(new NettyMSocketLengthCodec(FRAME_LENGTH_MASK)));
  }

  static class ClientConnection {
    NettyInbound inbound;
    NettyOutbound outbound;

    ClientConnection(Connection connection) {
      this.inbound = connection.inbound();
      this.outbound = connection.outbound();
      this.inbound.receive().map(NettyMSocketFrameLengthCodec::frame).subscribe(this::onReceive);
      this.outbound.send(Mono.fromSupplier(this::createSimpleMessage)).then().subscribe();
    }

    private ByteBuf createSimpleMessage() {
      String helloMsg = "MagOKO Boot :: MSocket";
      ByteBuf dataBuf = PooledByteBufAllocator.DEFAULT.buffer();
      dataBuf.writeBytes(helloMsg.getBytes());
      return encode(dataBuf.alloc(), dataBuf.readableBytes(), dataBuf);
    }

    private void onReceive(ByteBuf byteBuf) {
      ;
      this.outbound
          .send(
              Mono.just(byteBuf)
                  .map(ByteBuf::retain)
                  .doOnNext(buf -> System.out.println(buf.toString(StandardCharsets.UTF_8)))
                  .delayElement(Duration.ofSeconds(1))
                  .map(buf -> encode(buf.alloc(), buf.readableBytes(), buf)))
          .then()
          .subscribe();
    }
  }
}
