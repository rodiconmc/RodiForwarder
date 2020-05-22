package com.rodiconmc.rodi_forwarder

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * Forwards all data to a specified MinecraftSession
 */
class ForwarderHandler(private val forwardToSession: MinecraftSession): ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        forwardToSession.send(msg)
    }
}