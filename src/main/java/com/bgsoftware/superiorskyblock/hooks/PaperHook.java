package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public final class PaperHook {

    private static final boolean usingPaper = hasPaperInstalled();
    private static final boolean asyncSupport = usingPaper && ServerVersion.isAtLeast(ServerVersion.v1_13);

    public static void loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> chunkResult){
        if(asyncSupport){
            PaperLib.getChunkAtAsync(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ(), true)
                    .whenComplete((chunk, ex) -> chunkResult.accept(chunk));
        }
        else{
            chunkResult.accept(chunkPosition.loadChunk());
        }
    }

    public static void teleport(Entity entity, Location location){
        teleport(entity, location, r -> {});
    }

    public static void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult){
        if(asyncSupport){
            PaperLib.teleportAsync(entity, location).whenComplete((result, ex) -> teleportResult.accept(result));
        }
        else{
            teleportResult.accept(entity.teleport(location));
        }
    }

    private static boolean hasPaperInstalled(){
        try{
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            return true;
        }catch (Throwable ex){
            return false;
        }
    }

}
