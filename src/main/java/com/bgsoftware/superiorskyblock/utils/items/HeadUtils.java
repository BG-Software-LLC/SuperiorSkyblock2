package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.TagUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class HeadUtils {

    private static final String NULL_PLAYER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";

    public static String getMaterial() {
        return ServerVersion.isLegacy() ? "SKULL_ITEM:3" : "PLAYER_HEAD";
    }

    public static ItemStack getPlayerHead(ItemStack itemStack, String texture){
        CompoundTag compoundTag = TagUtils.itemToCompound(itemStack);

        CompoundTag nbtTag = (CompoundTag) compoundTag.getValue().getOrDefault("NBT", new CompoundTag(new HashMap<>()));

        CompoundTag skullOwner = (CompoundTag) nbtTag.getValue().getOrDefault("SkullOwner", new CompoundTag(new HashMap<>()));

        skullOwner.setString("Id", new UUID(texture.hashCode(), texture.hashCode()).toString());

        CompoundTag properties = new CompoundTag(new HashMap<>());

        ListTag textures = new ListTag(CompoundTag.class, new ArrayList<>());
        CompoundTag signature = new CompoundTag(new HashMap<>());
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
}
