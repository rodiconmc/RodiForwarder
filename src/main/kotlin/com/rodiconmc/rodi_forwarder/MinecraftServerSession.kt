package com.rodiconmc.rodi_forwarder

import io.netty.channel.socket.SocketChannel
import io.netty.util.concurrent.Future


class MinecraftServerSession(val clientSession: MinecraftClientSession, private val channel: SocketChannel): MinecraftSession() {

    /**
     * The address this session is associated with
     */
    override val address: String
        get() = channel.remoteAddress().toString()

    init {
        channel.pipeline().addLast(ForwarderHandler(clientSession))
        clientSession.onDisconnect {
            this.channel.close()
            logger.info("Client at ${clientSession.address} disconnected")
        }
    }

    /**
     * Sends data to this server
     */
    override fun send(data: Any) {
        channel.writeAndFlush(data)
    }

    /**
     * Add a callback to when this server disconnects
     */
    override fun onDisconnect(callback: (Future<in Void>) -> Unit) {
        channel.closeFuture().addListener(callback)
    }

}