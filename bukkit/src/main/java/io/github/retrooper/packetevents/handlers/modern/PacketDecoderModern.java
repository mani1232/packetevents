/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.handlers.modern;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.impl.PacketReceiveEvent;
import io.github.retrooper.packetevents.handlers.compression.CustomPacketCompressor;
import io.github.retrooper.packetevents.handlers.compression.CustomPacketDecompressor;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.netty.buffer.ByteBufAbstract;
import com.github.retrooper.packetevents.netty.channel.ChannelHandlerContextAbstract;
import io.github.retrooper.packetevents.handlers.modern.early.CompressionManagerModern;
import io.github.retrooper.packetevents.utils.dependencies.viaversion.CustomPipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PacketDecoderModern extends ByteToMessageDecoder {
    public ByteToMessageDecoder mcDecoder = null;
    public volatile Player player;
    public ConnectionState connectionState = ConnectionState.HANDSHAKING;
    public boolean bypassCompression = false;
    private boolean handledCompression;
    private boolean skipDoubleTransform;

    public PacketDecoderModern() {

    }

    public PacketDecoderModern(PacketDecoderModern decoder) {
        mcDecoder = decoder.mcDecoder;
        player = decoder.player;
        connectionState = decoder.connectionState;
        bypassCompression = decoder.bypassCompression;
        handledCompression = decoder.handledCompression;
        skipDoubleTransform = decoder.skipDoubleTransform;
    }

    public void handle(ChannelHandlerContextAbstract ctx, ByteBufAbstract byteBuf, List<Object> output) {
        if (skipDoubleTransform) {
            skipDoubleTransform = false;
            output.add(byteBuf.retain().rawByteBuf());
        }
        ByteBufAbstract transformedBuf = ctx.alloc().buffer().writeBytes(byteBuf);
        try {
            boolean needsCompress = !bypassCompression && handleCompressionOrder(ctx, transformedBuf);
            int firstReaderIndex = transformedBuf.readerIndex();
            PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(connectionState, ctx.channel(), player, transformedBuf);
            int readerIndex = transformedBuf.readerIndex();
            PacketEvents.getAPI().getEventManager().callEvent(packetReceiveEvent, () -> {
                transformedBuf.readerIndex(readerIndex);
            });
            if (!packetReceiveEvent.isCancelled()) {
                if (packetReceiveEvent.getLastUsedWrapper() != null) {
                    packetReceiveEvent.getByteBuf().clear();
                    packetReceiveEvent.getLastUsedWrapper().writeVarInt(packetReceiveEvent.getPacketID());
                    packetReceiveEvent.getLastUsedWrapper().writeData();
                }
                transformedBuf.readerIndex(firstReaderIndex);
                if (needsCompress) {
                    CustomPacketCompressor.recompress(ctx, transformedBuf);
                    skipDoubleTransform = true;
                }
                output.add(transformedBuf.retain().rawByteBuf());
            }
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        handle(PacketEvents.getAPI().getNettyManager().wrapChannelHandlerContext(ctx), PacketEvents.getAPI().getNettyManager().wrapByteBuf(byteBuf), out);
        if (mcDecoder != null) {
            try {
                Object input = out.get(0);
                out.clear();
                out.addAll(CustomPipelineUtil.callDecode(mcDecoder, ctx, input));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean handleCompressionOrder(ChannelHandlerContextAbstract ctx, ByteBufAbstract buf) {
        if (handledCompression) return false;

        int decoderIndex = ctx.pipeline().names().indexOf("decompress");
        if (decoderIndex == -1) return false;
        handledCompression = true;
        if (decoderIndex > ctx.pipeline().names().indexOf(PacketEvents.DECODER_NAME)) {
            // Need to decompress this packet due to bad order
            ByteBufAbstract decompressed = CustomPacketDecompressor.decompress(ctx, buf);
            return CompressionManagerModern.refactorHandlers((ChannelHandlerContext) ctx.rawChannelHandlerContext(),
                    (ByteBuf) buf.rawByteBuf(), (ByteBuf) decompressed.rawByteBuf());
        }
        return false;
    }
}
