package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataInput;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataOutput;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpCategoryInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpInfo;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.google.common.collect.Iterators;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CacheSerializer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private CacheSerializer() {

    }

    public static void serializePlayers(Collection<SuperiorPlayer> players, ByteArrayDataOutput dataOutput) {
        dataOutput.writeInt(players.size());
        players.forEach(player -> dataOutput.writeUUID(player.getUniqueId()));
    }

    public static Collection<SuperiorPlayer> deserializePlayers(ByteArrayDataInput dataInput) {
        int playersAmount = dataInput.readInt();
        List<SuperiorPlayer> players = new ArrayList<>(playersAmount);
        for (int i = 0; i < playersAmount; ++i) {
            players.add(deserializePlayer(dataInput));
        }
        return players;
    }

    public static SuperiorPlayer deserializePlayer(ByteArrayDataInput dataInput) {
        return plugin.getPlayers().getSuperiorPlayer(dataInput.readUUID());
    }

    public static void serializeLocation(@Nullable Location location, ByteArrayDataOutput dataOutput) {
        if (location == null) {
            dataOutput.writeString("");
            return;
        }

        dataOutput.writeString(getWorldName(location));
        dataOutput.writeDouble(location.getX());
        dataOutput.writeDouble(location.getY());
        dataOutput.writeDouble(location.getZ());
        dataOutput.writeFloat(location.getYaw());
        dataOutput.writeFloat(location.getPitch());
    }

    public static Location deserializeLocation(ByteArrayDataInput dataInput) {
        String worldName = dataInput.readString();
        return worldName.isEmpty() ? null : new LazyWorldLocation(worldName, dataInput.readDouble(), dataInput.readDouble(),
                dataInput.readDouble(), dataInput.readFloat(), dataInput.readFloat());
    }

    public static void serializePermissionNode(PlayerPrivilegeNode permissionNode, ByteArrayDataOutput dataOutput) {
        dataOutput.writeUUID(permissionNode.getSuperiorPlayer().getUniqueId());
        Map<IslandPrivilege, Boolean> permissions = permissionNode.getCustomPermissions();
        dataOutput.writeInt(permissions.size());
        permissions.forEach(((islandPrivilege, isEnabled) -> {
            dataOutput.writeInt(islandPrivilege.ordinal());
            dataOutput.writeBoolean(isEnabled);
        }));
    }

    public static PlayerPrivilegeNode deserializePermissionNode(ByteArrayDataInput dataInput) {
        PlayerPrivilegeNode playerPrivilegeNode = new PlayerPrivilegeNode(deserializePlayer(dataInput), null);
        int permissionsAmount = dataInput.readInt();
        for (int i = 0; i < permissionsAmount; ++i) {
            playerPrivilegeNode.loadPrivilege(Iterators.get(IslandPrivilege.values().iterator(), dataInput.readInt()),
                    (byte) (dataInput.readBoolean() ? 1 : 0));
        }
        return playerPrivilegeNode;
    }

    public static void serializeChunkPosition(ChunkPosition chunkPosition, ByteArrayDataOutput dataOutput) {
        dataOutput.writeString(chunkPosition.getWorldName());
        dataOutput.writeInt(chunkPosition.getX());
        dataOutput.writeInt(chunkPosition.getZ());
    }

    public static ChunkPosition deserializeChunkPosition(ByteArrayDataInput dataInput) {
        return ChunkPosition.of(dataInput.readString(), dataInput.readInt(), dataInput.readInt());
    }

    public static void serializeWarpCategory(WarpCategory warpCategory, ByteArrayDataOutput dataOutput) {
        dataOutput.writeString(warpCategory.getName());
        dataOutput.writeInt(warpCategory.getSlot());
        serializeItemStack(warpCategory.getRawIcon(), dataOutput);
        List<IslandWarp> islandWarps = warpCategory.getWarps();
        dataOutput.writeInt(islandWarps.size());
        islandWarps.forEach(islandWarp -> serializeIslandWarp(islandWarp, dataOutput));
    }

    public static CachedWarpCategoryInfo deserializeWarpCategory(ByteArrayDataInput dataInput) {
        CachedWarpCategoryInfo cachedWarpCategoryInfo = new CachedWarpCategoryInfo();
        cachedWarpCategoryInfo.name = dataInput.readString();
        cachedWarpCategoryInfo.slot = dataInput.readInt();
        cachedWarpCategoryInfo.icon = deserializeItemStack(dataInput);
        return cachedWarpCategoryInfo;
    }

    private static void serializeIslandWarp(IslandWarp islandWarp, ByteArrayDataOutput dataOutput) {
        dataOutput.writeString(islandWarp.getName());
        serializeLocation(islandWarp.getLocation(), dataOutput);
        dataOutput.writeBoolean(islandWarp.hasPrivateFlag());
        serializeItemStack(islandWarp.getRawIcon(), dataOutput);
    }

    public static CachedWarpInfo deserializeIslandWarp(ByteArrayDataInput dataInput) {
        CachedWarpInfo cachedWarpInfo = new CachedWarpInfo();
        cachedWarpInfo.name = dataInput.readString();
        cachedWarpInfo.location = deserializeLocation(dataInput);
        cachedWarpInfo.isPrivate = dataInput.readBoolean();
        cachedWarpInfo.icon = deserializeItemStack(dataInput);
        return cachedWarpInfo;
    }

    public static void serializeIslandChest(IslandChest islandChest, ByteArrayDataOutput dataOutput) {
        dataOutput.writeInt(islandChest.getIndex());

        ItemStack[] contents = islandChest.getContents();

        dataOutput.writeInt(contents.length);
        for (int i = 0; i < contents.length; ++i) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                dataOutput.writeInt(i);
                serializeItemStack(contents[i], dataOutput);
            }
        }

        dataOutput.writeInt(-1);
    }

    public static ItemStack[] deserializeIslandChest(ByteArrayDataInput dataInput) {
        ItemStack[] contents = new ItemStack[dataInput.readInt()];

        int itemStackSlot;

        while ((itemStackSlot = dataInput.readInt()) != -1) {
            contents[itemStackSlot] = deserializeItemStack(dataInput);
        }

        return contents;
    }

    public static void serializeItemStack(ItemStack itemStack, ByteArrayDataOutput dataOutput) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        CompoundTag itemTag = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.serialize(itemStack);

        try {
            itemTag.write(new DataOutputStream(outputStream));
        } catch (IOException ignored) {

        }

        dataOutput.writeBytes(outputStream.toByteArray());
    }

    public static ItemStack deserializeItemStack(ByteArrayDataInput dataInput) {
        byte[] data = new byte[dataInput.readInt()];

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        CompoundTag compoundTag = null;

        try {
            compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
        } catch (Exception ignored) {
        }

        return Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize(compoundTag);
    }

    private static String getWorldName(Location location) {
        return location instanceof LazyWorldLocation ? ((LazyWorldLocation) location).getWorldName() :
                location.getWorld().getName();
    }

}
