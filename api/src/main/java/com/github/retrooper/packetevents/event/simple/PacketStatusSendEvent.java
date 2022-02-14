/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
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

package com.github.retrooper.packetevents.event.simple;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufAbstract;
import com.github.retrooper.packetevents.netty.channel.ChannelAbstract;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;

import java.net.InetSocketAddress;
import java.util.List;

public class PacketStatusSendEvent extends PacketSendEvent {
    public PacketStatusSendEvent(ChannelAbstract channel, User user, Object player, ByteBufAbstract byteBuf) throws PacketProcessException  {
        super(channel, user, player, byteBuf);
    }

    public PacketStatusSendEvent(ConnectionState connectionState, ChannelAbstract channel,
                                 User user, Object player, ByteBufAbstract byteBuf) throws PacketProcessException {
        super(connectionState, channel, user, player, byteBuf);
    }

    public PacketStatusSendEvent(Object channel, User user, Object player, Object rawByteBuf) throws PacketProcessException{
        super(channel, user, player, rawByteBuf);
    }

    public PacketStatusSendEvent(ConnectionState connectionState, Object channel,
                                 User user, Object player, Object rawByteBuf) throws PacketProcessException{
        super(connectionState, channel, user, player, rawByteBuf);
    }

    public PacketStatusSendEvent(boolean cloned, int packetID, PacketTypeCommon packetType, ServerVersion serverVersion,
                                 InetSocketAddress socketAddress, ChannelAbstract channel, User user, Object player,
                                 ByteBufAbstract byteBuf) throws PacketProcessException{
        super(cloned, packetID, packetType, serverVersion, socketAddress, channel, user, player, byteBuf);
    }

    public PacketType.Status.Server getPacketType() {
        return (PacketType.Status.Server) super.getPacketType();
    }
}