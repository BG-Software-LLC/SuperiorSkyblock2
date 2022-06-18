package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class ChunkProviderServer extends MappedObject<net.minecraft.server.level.ChunkProviderServer> {

    public ChunkProviderServer(net.minecraft.server.level.ChunkProviderServer handle) {
        super(handle);
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
