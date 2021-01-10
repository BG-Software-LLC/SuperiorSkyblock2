package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntArrayTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.TagUtils;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class HeadUtils {

    private static final String NULL_PLAYER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";
    private static final Registry<String, String> entitySkullTextures = Registry.createRegistry();

    private HeadUtils(){

    }

    public static void readTextures(SuperiorSkyblockPlugin plugin){
        entitySkullTextures.clear();

        File file = new File(plugin.getDataFolder(), "heads.yml");

        if(!file.exists())
            plugin.saveResource("heads.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, plugin.getResource("heads.yml"));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        for(String entityType : cfg.getConfigurationSection("").getKeys(false))
            entitySkullTextures.add(entityType, cfg.getString(entityType));
    }

    public static ItemStack getPlayerHead(ItemStack itemStack, String texture){
        CompoundTag compoundTag = TagUtils.itemToCompound(itemStack);

        CompoundTag nbtTag = (CompoundTag) compoundTag.getValue().getOrDefault("NBT", new CompoundTag());

        CompoundTag skullOwner = (CompoundTag) nbtTag.getValue().getOrDefault("SkullOwner", new CompoundTag());

        UUID ownerUUID = new UUID(texture.hashCode(), texture.hashCode());

        if(ServerVersion.isAtLeast(ServerVersion.v1_16)){
            skullOwner.setTag("Id", IntArrayTag.fromUUID(ownerUUID));
        }
        else {
            skullOwner.setString("Id", ownerUUID.toString());
        }

        CompoundTag properties = new CompoundTag();

        ListTag textures = new ListTag(CompoundTag.class, new ArrayList<>());
        CompoundTag signature = new CompoundTag();
        signature.setString("Value", texture);
        textures.addTag(signature);

        properties.setTag("textures", textures);

        skullOwner.setTag("Properties", properties);

        nbtTag.setTag("SkullOwner", skullOwner);

        compoundTag.setTag("NBT", nbtTag);

        return TagUtils.compoundToItem(compoundTag);
    }

    public static String getNullPlayerTexture(){
        return NULL_PLAYER_TEXTURE;
    }

    public static String getTexture(String entityType){
        return entitySkullTextures.get(entityType, "");
    }

}
