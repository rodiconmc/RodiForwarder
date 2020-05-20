package com.rodiconmc.rodi_forwarder

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel


object BackendClient {

    private val workerGroup: EventLoopGroup = NioEventLoopGroup()

    fun connectToServer(host: String, port: Int, session: ForwarderSession): Channel {
        try {
            val b = Bootstrap()
            b.group(workerGroup)
            b.channel(NioSocketChannel::class.java)
            b.option(ChannelOption.SO_KEEPALIVE, true)
            b.handler(object : ChannelInitializer<SocketChannel>() {
                public override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(LoopbackHandler(session, ch))
                }
            })

            // Start the client.
            val f: ChannelFuture = b.connect(host, port).sync()
            return f.channel()
        } finally {}
    }
}