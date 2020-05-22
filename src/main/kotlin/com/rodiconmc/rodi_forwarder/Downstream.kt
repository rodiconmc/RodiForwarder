package com.rodiconmc.rodi_forwarder

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel


object Downstream {

    fun connectToServer(host: String, port: Int, clientSession: MinecraftClientSession): MinecraftServerSession {
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        val bootstrap = Bootstrap()
        bootstrap.group(workerGroup)
        bootstrap.channel(NioSocketChannel::class.java)
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
        bootstrap.handler(object: ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {}
        })
        val channel = bootstrap.connect(host, port).sync().channel() as SocketChannel
        return MinecraftServerSession(clientSession, channel)
    }
}