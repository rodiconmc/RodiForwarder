package com.rodiconmc.rodi_forwarder

import io.netty.util.concurrent.Future

abstract class MinecraftSession {
    /**
     * Sends data to the socket associated with this session
     */
    abstract fun send(data: Any)

    /**
     * Add a callback for when the socket associated with this session disconnects
     */
    abstract fun onDisconnect(callback: (Future<in Void>) -> Unit)

    /**
     * The address this session is associated with
     */
    abstract val address: String
}