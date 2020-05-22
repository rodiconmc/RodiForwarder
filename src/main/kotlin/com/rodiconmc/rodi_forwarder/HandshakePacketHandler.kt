package com.rodiconmc.rodi_forwarder

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * Tries to read the handshake packet from minecraft and parse the host
 */
class HandshakePacketHandler(private val onError: (e: Exception) -> Unit,
                             private val foundHost: (host: String) -> Unit): ChannelInboundHandlerAdapter() {

    private var byteAccumulator: ByteBuf = ByteBufAllocator.DEFAULT.buffer()
    private var disabled = false

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (disabled) {
            ctx.fireChannelRead(msg)
            return
        }
        val data = msg as ByteBuf
        byteAccumulator = ctx.alloc().compositeBuffer().addComponent(true, byteAccumulator).addComponent(true, data)

        try {
            val host = tryReadHandshakePacket(byteAccumulator.copy())
            if (host != null) {
                foundHost(host)
            }
        } catch(e: java.lang.Exception) {
            onError(e)
        }
    }

    /**
     * Gets what data was intercepted by this handler that still needs to be forwarded
     */
    fun getRemainingBuf(): ByteBuf {
        disabled = true
        return byteAccumulator
    }

    private fun tryReadHandshakePacket(buf: ByteBuf): String? {
        if (!buf.isReadable) return null
        try {
            readVarInt(buf) //Read size
            val packetId = readVarInt(buf)
            if (packetId != 0x00) throw InvalidDataException()
            readVarInt(buf) //Read protocol version
            return readString(buf) //Return server address
        } catch (e: MissingDataException) {}
        return null
    }

    private fun readVarInt(buf: ByteBuf): Int {
        try {
            var reachedEnd = false
            var result = 0
            while (!reachedEnd) {
                val byte = buf.readByte()
                result += byte.toInt() and 0b01111111
                if (byte.toInt() shr 7 == 0) reachedEnd = true
            }
            return result
        } catch (e: IndexOutOfBoundsException) {
            throw MissingDataException()
        }
    }

    private fun readString(buf: ByteBuf): String {
        try {
            val size = readVarInt(buf)
            var string = ""
            for (i in 1..size) {
                string += buf.readByte().toChar()
            }
            return string
        } catch (e: IndexOutOfBoundsException) {
            throw MissingDataException()
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }

    class MissingDataException : Exception()
    class InvalidDataException: Exception()
}