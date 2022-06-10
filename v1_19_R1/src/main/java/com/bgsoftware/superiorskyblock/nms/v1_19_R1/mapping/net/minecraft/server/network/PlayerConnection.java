package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.network;

import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import net.minecraft.network.protocol.Packet;

public final class PlayerConnection extends MappedObject<net.minecraft.server.network.PlayerConnection> {

    public PlayerConnection(net.minecraft.server.network.PlayerConnection handle) {
        super(handle);
    }

    public void sendPacket(Packet<?> packet) {
        handle.a(packet);
    }

}
