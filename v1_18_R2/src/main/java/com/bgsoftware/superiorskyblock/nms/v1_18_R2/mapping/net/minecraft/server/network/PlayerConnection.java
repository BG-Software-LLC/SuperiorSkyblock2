package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.network;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.network.protocol.Packet;

public final class PlayerConnection extends MappedObject<net.minecraft.server.network.PlayerConnection> {

    public PlayerConnection(net.minecraft.server.network.PlayerConnection handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
            name = "send",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void sendPacket(Packet<?> packet) {
        handle.a(packet);
    }

}
