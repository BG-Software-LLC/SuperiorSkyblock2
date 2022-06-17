package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.network;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.network.protocol.Packet;

public class PlayerConnection extends MappedObject<net.minecraft.server.network.PlayerConnection> {

    public PlayerConnection(net.minecraft.server.network.PlayerConnection handle) {
        super(handle);
    }

    public void sendPacket(Packet<?> packet) {
        handle.a(packet);
    }

}
