package com.rodiconmc.rodi_forwarder

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil


class LoopbackHandler(val session: ForwarderSession, val self: Channel) : ChannelInboundHandlerAdapter() {

    init {
        self.closeFuture().addListener {
            session.self.close()
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        session.self.writeAndFlush(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}