package com.rodiconmc.rodi_forwarder

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel


object ForwarderServer {

    /**
     * Starts the server to accept incoming connections
     */
    fun run(port: Int) {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        public override fun initChannel(ch: SocketChannel) {
                            MinecraftClientSession(ch)
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

            // Bind and start to accept incoming connections.
            val channel = bootstrap.bind(port).sync().channel()
            logger.info("RodiForwarder started on port $port")

            // Wait until the server socket is closed.
            channel.closeFuture().sync()
            logger.error("RodiForwarder socket closed")
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }

}