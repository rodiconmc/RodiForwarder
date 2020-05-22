package com.rodiconmc.rodi_forwarder

import io.netty.channel.socket.SocketChannel
import io.netty.util.concurrent.Future
import java.net.URI

/**
 * Represents a connection to a minecraft client
 */
class MinecraftClientSession(private val channel: SocketChannel): MinecraftSession() {

    var destinationSession: MinecraftServerSession? = null
        private set

    /**
     * The address this session is associated with
     */
    override val address: String
        get() = channel.remoteAddress().toString()

    private lateinit var hostname: String

    init {
        channel.pipeline().addLast(HandshakePacketHandler(this::error, this::foundHostname))
    }

    /**
     * Gets called if the HandshakePacketHandler gets an error
     */
    private fun error(e: Exception) {
        when (e) {
            is HandshakePacketHandler.InvalidDataException -> {
                channel.close()
                logger.warn("Client at ${channel.remoteAddress()} sent invalid data")
            }
            else -> throw e
        }
    }

    /**
     * Send data to this minecraft client
     */
    override fun send(data: Any) {
        channel.writeAndFlush(data)
    }

    /**
     * Add a callback to when this client disconnects
     */
    override fun onDisconnect(callback: (Future<in Void>) -> Unit) {
        channel.closeFuture().addListener(callback)
    }

    /**
     * Gets called when a handshake packet is received
     */
    private fun foundHostname(hostname: String) {
        if (destinationSession != null) return
        this.hostname = hostname
        if (hostMap.containsKey(hostname)) {
            val destination = URI("tcp://" + hostMap[hostname])
            val destinationSession = Downstream.connectToServer(destination.host, destination.port, this)
            destinationSession.onDisconnect { this.channel.close() }
            channel.pipeline().addLast(ForwarderHandler(destinationSession))
            val oldHandler = channel.pipeline().remove(HandshakePacketHandler::class.java)
            val remainingData = oldHandler.getRemainingBuf()
            destinationSession.send(remainingData)
            logger.info("Client at ${channel.remoteAddress()} connected with $hostname and is being forwarded to ${destinationSession.address}")
            this.destinationSession = destinationSession
        } else {
            logger.warn("Client ${channel.remoteAddress()} tried to connect using ${hostname}, which has no mapping assigned.")
            this.channel.close()
        }
    }

    private fun destinationConnectedSuccessful(destination: MinecraftServerSession) {
        destinationSession = destination

    }
}