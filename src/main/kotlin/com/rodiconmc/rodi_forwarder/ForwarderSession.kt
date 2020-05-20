package com.rodiconmc.rodi_forwarder

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil


/**
 * Handles a server-side channel.
 */
class ForwarderSession(val self: Channel) : ChannelInboundHandlerAdapter() {

    var destination: String? = null
    val byteList = mutableListOf<Byte>()
    var pointer = 0
    var client: Channel? = null

    init {
        self.closeFuture().addListener {
            client?.close()
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val data = msg as ByteBuf
        if (destination == null) {
            data.forEachByte {
                byteList.add(it)
            }
            val shouldTerminate = tryReadPacket(ctx)
            if (shouldTerminate) ctx.close()
            if (destination == null) ReferenceCountUtil.release(msg)
        } else {
            client!!.writeAndFlush(msg)
        }
    }

    private fun tryReadPacket(ctx: ChannelHandlerContext): Boolean {
        pointer = 0
        if (byteList.isEmpty()) return false
        try {
            val size = readVarInt()
            if (byteList.size - pointer < size) return false
            val packetId = readVarInt()
            if (packetId != 0x00) return true
            val protocolVersion = readVarInt()
            val serverAddress = readString()
            if (hostMap.containsKey(serverAddress)) {
                destination = hostMap[serverAddress]
                client = BackendClient.connectToServer(destination!!.split(":")[0], destination!!.split(":")[1].toInt(), this)
                val buff = ctx.alloc().buffer(byteList.size)
                buff.writeBytes(byteList.toByteArray())
                client!!.writeAndFlush(buff)

            } else return true
        } finally {
        }
        return false
    }


    private fun readVarInt(): Int {
        var reachedEnd = false
        var result = 0
        while (!reachedEnd) {
            if (pointer >= byteList.size) throw MissingDataException()
            val byte = byteList[pointer]
            result += byte.toInt() and 0b01111111
            if (byte.toInt() shr 7 == 0) reachedEnd = true
            pointer++
        }
        return result
    }

    private fun readString(): String {
        val size = readVarInt()
        val goalPointer = pointer + size
        var string = ""
        while (pointer < goalPointer) {
            if (pointer >= byteList.size) throw MissingDataException()
            string += byteList[pointer].toChar()
            pointer++
        }
        return string
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }

    class MissingDataException : Exception()
}