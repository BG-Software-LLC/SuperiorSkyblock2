package com.bgsoftware.superiorskyblock.core.database.serialization;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.DirtyChunk;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class IslandsSerializer {

    private static final Gson gson = new GsonBuilder().create();

    private IslandsSerializer() {

    }

    public static String serializeBlockCounts(Map<Key, BigInteger> blockCounts) {
        JsonArray blockCountsArray = new JsonArray();
        blockCounts.forEach((key, amount) -> {
            JsonObject blockCountObject = new JsonObject();
            blockCountObject.addProperty("id", key.toString());
            blockCountObject.addProperty("amount", amount.toString());
            blockCountsArray.add(blockCountObject);
        });
        return gson.toJson(blockCountsArray);
    }

    public static String serializeDirtyChunks(List<DirtyChunk> dirtyChunks) {
        JsonObject dirtyChunksObject = new JsonObject();
        dirtyChunks.forEach(dirtyChunk -> {
            JsonArray dirtyChunksArray;

            if (dirtyChunksObject.has(dirtyChunk.getWorldName())) {
                dirtyChunksArray = dirtyChunksObject.getAsJsonArray(dirtyChunk.getWorldName());
            } else {
                dirtyChunksArray = new JsonArray();
                dirtyChunksObject.add(dirtyChunk.getWorldName(), dirtyChunksArray);
            }

            dirtyChunksArray.add(new JsonPrimitive(dirtyChunk.getX() + "," + dirtyChunk.getZ()));
        });
        return gson.toJson(dirtyChunksObject);
    }

    public static String serializeDirtyChunkPositions(List<ChunkPosition> dirtyChunks) {
        JsonObject dirtyChunksObject = new JsonObject();
        dirtyChunks.forEach(dirtyChunk -> {
            JsonArray dirtyChunksArray;

            if (dirtyChunksObject.has(dirtyChunk.getWorldName())) {
                dirtyChunksArray = dirtyChunksObject.getAsJsonArray(dirtyChunk.getWorldName());
            } else {
                dirtyChunksArray = new JsonArray();
                dirtyChunksObject.add(dirtyChunk.getWorldName(), dirtyChunksArray);
            }

            dirtyChunksArray.add(new JsonPrimitive(dirtyChunk.getX() + "," + dirtyChunk.getZ()));
        });
        return gson.toJson(dirtyChunksObject);
    }

}
