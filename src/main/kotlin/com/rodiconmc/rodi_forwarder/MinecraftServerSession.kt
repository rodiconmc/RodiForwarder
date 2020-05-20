package com.rodiconmc.rodi_forwarder

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter


class MinecraftServerSession(private val clientSession: MinecraftClientSession, channel: Channel) : ChannelInboundHandlerAdapter() {

    init {
        channel.closeFuture().addListener {
            clientSession.channel.close()
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        clientSession.channel.writeAndFlush(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}