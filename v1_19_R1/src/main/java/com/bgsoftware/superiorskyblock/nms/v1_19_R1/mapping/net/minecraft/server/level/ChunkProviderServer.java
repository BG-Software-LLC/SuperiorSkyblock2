package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.Nullable;

public final class ChunkProviderServer extends MappedObject<net.minecraft.server.level.ChunkProviderServer> {

    public ChunkProviderServer(net.minecraft.server.level.ChunkProviderServer handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "getChunk",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Nullable
    public ChunkAccess getChunkAt(int x, int z, boolean load) {
        return ChunkAccess.ofNullable(handle.a(x, z, load));
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "getGenerator",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public ChunkGenerator getGenerator() {
        return handle.g();
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "chunkMap",
            type = Remap.Type.FIELD,
            remappedName = "a")
    public PlayerChunkMap getPlayerChunkMap() {
        return new PlayerChunkMap(handle.a);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "randomState",
            type = Remap.Type.METHOD,
            remappedName = "h")
    public RandomState getRandomState() {
        return handle.h();
    }


}
