package com.rodiconmc.rodi_forwarder

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.URI


class MinecraftClientSession(val channel: Channel) : ChannelInboundHandlerAdapter() {

    private var byteAccumulator: ByteBuf? = null
    var downstreamServerChannel: Channel? = null

    init {
        channel.closeFuture().addListener {
            downstreamServerChannel?.close()
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val data = msg as ByteBuf
        if (downstreamServerChannel == null) {
            if (byteAccumulator == null) byteAccumulator = ctx.alloc().compositeBuffer()
            byteAccumulator = ctx.alloc().compositeBuffer().addComponent(true, byteAccumulator).addComponent(true, data).consolidate()

            try {
                tryReadPacket(byteAccumulator!!.copy())
            } catch(e: InvalidDataException) {
                channel.close()
            }

        } else {
            downstreamServerChannel!!.writeAndFlush(msg)
        }
    }

    private fun tryReadPacket(buf: ByteBuf): Boolean {
        if (!buf.isReadable) return false
        val original = buf.copy()
        try {
            val size = readVarInt(buf)
            val packetId = readVarInt(buf)
            if (packetId != 0x00) throw InvalidDataException()
            val protocolVersion = readVarInt(buf)
            val serverAddress = readString(buf)
            if (hostMap.containsKey(serverAddress)) {
                val destination = URI("tcp://" + hostMap[serverAddress])
                downstreamServerChannel = Downstream.connectToServer(destination.host, destination.port, this)
                downstreamServerChannel!!.writeAndFlush(original)

            } else return true
        } catch (e: MissingDataException) {}
        return false
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