package me.purplex.packetevents.example;

import me.purplex.packetevents.PacketEvents;
import me.purplex.packetevents.enums.EntityUseAction;
import me.purplex.packetevents.enums.PlayerDigType;
import me.purplex.packetevents.packetevent.PacketEvent;
import me.purplex.packetevents.packetevent.impl.PacketReceiveEvent;
import me.purplex.packetevents.packetevent.impl.PacketSendEvent;
import me.purplex.packetevents.packetevent.impl.ServerTickEvent;
import me.purplex.packetevents.packetevent.handler.PacketHandler;
import me.purplex.packetevents.packetevent.listener.PacketListener;
import me.purplex.packetevents.packetevent.packet.Packet;
import me.purplex.packetevents.packetwrappers.in.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TestExample implements PacketListener, Listener {
    /**
     * How to register a packet listener?
     * <p>
     * PacketEvents.getPacketManager().registerListener(new TestExample());
     */

    /**
     * How to unregister a packet listener?
     * PacketEvents.getPacketManager().unregisterListener(new TestExample());
     */

    private int tick;

    @PacketHandler
    public void onPacketReceive(PacketReceiveEvent e) {
        final Player p = e.getPlayer();
        final long timestamp = e.getTimestamp();
        //ONLY CLIENT PACKETS ALLOWED HERE!
        switch (e.getPacketName()) {
            case Packet.Client.USE_ENTITY:
                final WrappedPacketPlayInUseEntity useEntity = new WrappedPacketPlayInUseEntity(e.getPacket());
                final Entity entity = useEntity.getEntity();
                if (useEntity.getEntityUseAction() == EntityUseAction.ATTACK) {
                    final double dist = entity.getLocation().distanceSquared(p.getLocation());
                    //p.sendMessage("dist: " + dist);
                }
                break;
            case Packet.Client.ABILITIES:
                final WrappedPacketPlayInAbilities abilities = new WrappedPacketPlayInAbilities(e.getPacket());
                final boolean a = abilities.a;
                final boolean b = abilities.b;
                final boolean c = abilities.c;
                final boolean d = abilities.d;
                final float eFloat = abilities.e;
                final float fFloat = abilities.f;
                break;
            case Packet.Client.BLOCK_DIG:
                final WrappedPacketPlayInBlockDig blockDig = new WrappedPacketPlayInBlockDig(e.getPacket());
                final PlayerDigType type = blockDig.getPlayerDigType();
                break;
            case Packet.Client.FLYING:
                final WrappedPacketPlayInFlying flying = new WrappedPacketPlayInFlying(e.getPacket());
                final float pitch = flying.pitch;
                final float yaw = flying.yaw;
                final boolean hasPos = flying.hasPos;
                final boolean hasLook = flying.hasLook;
                break;
            case Packet.Client.POSITION:
                final WrappedPacketPlayInFlying.WrappedPacketPlayInPosition position =
                        new WrappedPacketPlayInFlying.WrappedPacketPlayInPosition(e.getPacket());
                final boolean isPos = position.hasPos; //true
                break;
            case Packet.Client.POSITION_LOOK:
                final WrappedPacketPlayInFlying.WrappedPacketPlayInPosition_Look position_look =
                        new WrappedPacketPlayInFlying.WrappedPacketPlayInPosition_Look(e.getPacket());
                final boolean isLook = position_look.hasLook; //true
                break;
        }
    }

    @PacketHandler
    public void onPacket(PacketEvent e) {
        if(e instanceof PacketSendEvent) {
            PacketSendEvent p = (PacketSendEvent)e;
            onPacketSend(p);
        }
    }

    private void onPacketSend(PacketSendEvent e) {
        //code
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        CustomMoveEvent customMoveEvent = new CustomMoveEvent(e.getPlayer(),e.getTo(), e.getFrom());
        e.setCancelled(customMoveEvent.isCancelled());
        PacketEvents.getPacketManager().callEvent(customMoveEvent);

        if(e.getTo() != customMoveEvent.getTo()) {
            e.setTo(customMoveEvent.getTo());
        }
        if(e.getFrom() != customMoveEvent.getFrom()) {
            e.setFrom(customMoveEvent.getFrom());
        }
    }

    @PacketHandler
    public void onServerTick(ServerTickEvent e) {

        if (tick % 20 == 0) {
           //one tick passed
        }
        tick++;
    }

}
