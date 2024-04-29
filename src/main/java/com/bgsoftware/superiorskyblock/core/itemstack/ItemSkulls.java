package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.itemstack.heads.MinecraftHeadsClient;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class ItemSkulls {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final String NULL_PLAYER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";
    private static final Map<String, String> entitySkullTextures = new HashMap<>();
    private static final LazyReference<MinecraftHeadsClient> minecraftHeadsClient = new LazyReference<MinecraftHeadsClient>() {
        @Override
        protected MinecraftHeadsClient create() {
            return new MinecraftHeadsClient();
        }
    };

    private ItemSkulls() {

    }

    public static void readTextures(SuperiorSkyblockPlugin plugin) {
        entitySkullTextures.clear();

        File file = new File(plugin.getDataFolder(), "heads.yml");

        if (!file.exists())
            plugin.saveResource("heads.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, plugin.getResource("heads.yml"));
        } catch (Exception error) {
            Log.entering("ENTER");
            Log.error(error, "An unexpected error occurred while syncing heads file:");
        }

        for (String entityType : cfg.getConfigurationSection("").getKeys(false))
            entitySkullTextures.put(entityType, parseTexture(cfg.getString(entityType)));
    }

    public static ItemStack getPlayerHead(ItemStack itemStack, String texture) {
        return plugin.getNMSTags().getSkullWithTexture(itemStack, texture);
    }

    public static ItemStack getPlayerHeadNoNMS(ItemStack itemStack, String texture) {
        CompoundTag compoundTag = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.serialize(itemStack);

        CompoundTag nbtTag = compoundTag.getCompound("NBT", new CompoundTag());

        CompoundTag skullOwner = nbtTag.getCompound("SkullOwner", new CompoundTag());

        UUID ownerUUID = new UUID(texture.hashCode(), texture.hashCode());

        if (ServerVersion.isAtLeast(ServerVersion.v1_16)) {
            skullOwner.setTag("Id", IntArrayTag.fromUUID(ownerUUID));
        } else {
            skullOwner.setString("Id", ownerUUID.toString());
        }

        CompoundTag properties = new CompoundTag();

        ListTag textures = new ListTag(CompoundTag.class, Collections.emptyList());
        CompoundTag signature = new CompoundTag();
        signature.setString("Value", texture);
        textures.addTag(signature);

        properties.setTag("textures", textures);

        skullOwner.setTag("Properties", properties);

        nbtTag.setTag("SkullOwner", skullOwner);

        compoundTag.setTag("NBT", nbtTag);

        return Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize(compoundTag);
    }

    public static String getNullPlayerTexture() {
        return NULL_PLAYER_TEXTURE;
    }

    public static String getTexture(String entityType) {
        return entitySkullTextures.getOrDefault(entityType, "");
    }

    public static String parseTexture(String texture) {
        return MinecraftHeadsClient.getMinecraftHeadsTextureId(texture)
                .map(textureId -> minecraftHeadsClient.get().getTexture(textureId))
                .orElse(texture);
    }

}
