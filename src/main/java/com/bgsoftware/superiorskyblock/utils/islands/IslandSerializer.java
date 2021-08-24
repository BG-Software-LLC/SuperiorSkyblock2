package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

public final class IslandSerializer {

    private static final Gson gson = new GsonBuilder().create();

    private IslandSerializer(){

    }

    public static String serializeBlockCounts(Map<Key, BigInteger> blockCounts){
        JsonArray blockCountsArray = new JsonArray();
        blockCounts.forEach((key, amount) -> {
            JsonObject blockCountObject = new JsonObject();
            blockCountObject.addProperty("id", key.toString());
            blockCountObject.addProperty("amount", amount.toString());
            blockCountsArray.add(blockCountObject);
        });
        return gson.toJson(blockCountsArray);
    }

    public static String serializeDirtyChunks(Set<ChunkPosition> dirtyChunks){
        JsonObject dirtyChunksObject = new JsonObject();
        dirtyChunks.forEach(chunkPosition -> {
            JsonArray dirtyChunksArray;

            if(dirtyChunksObject.has(chunkPosition.getWorldName())){
                dirtyChunksArray = dirtyChunksObject.getAsJsonArray(chunkPosition.getWorldName());
            }
            else{
                dirtyChunksArray = new JsonArray();
                dirtyChunksObject.add(chunkPosition.getWorldName(), dirtyChunksArray);
            }

            dirtyChunksArray.add(new JsonPrimitive(chunkPosition.getX() + "," + chunkPosition.getZ()));
        });
        return gson.toJson(dirtyChunksObject);
    }

}
