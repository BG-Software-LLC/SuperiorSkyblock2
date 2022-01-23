package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkAccess;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public final class ChunkProviderServer extends MappedObject<net.minecraft.server.level.ChunkProviderServer> {

    public ChunkProviderServer(net.minecraft.server.level.ChunkProviderServer handle) {
        super(handle);
    }

    public <T> void removeTicket(TicketType<T> ticketType, ChunkCoordIntPair chunkCoordIntPair, int i, T value) {
        handle.b(ticketType, chunkCoordIntPair.getHandle(), i, value);
    }

    public <T> void addTicket(TicketType<T> ticketType, ChunkCoordIntPair chunkCoordIntPair, int i, T value) {
        handle.a(ticketType, chunkCoordIntPair.getHandle(), i, value);
    }

    @Nullable
    public ChunkAccess getChunkAt(int x, int z, boolean load) {
        return ChunkAccess.ofNullable(handle.a(x, z, load));
    }

    public ChunkGenerator getGenerator() {
        return handle.g();
    }

    public PlayerChunkMap getPlayerChunkMap() {
        return new PlayerChunkMap(handle.a);
    }


}
