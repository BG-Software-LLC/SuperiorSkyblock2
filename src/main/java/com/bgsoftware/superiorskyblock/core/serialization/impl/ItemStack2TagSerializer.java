package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.ShortTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ItemStack2TagSerializer implements ISerializer<ItemStack, CompoundTag> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ItemStack2TagSerializer INSTANCE = new ItemStack2TagSerializer();

    public static ItemStack2TagSerializer getInstance() {
        return INSTANCE;
    }

    private ItemStack2TagSerializer() {
    }

    @Override
    public @NotNull
    CompoundTag serialize(@Nullable ItemStack serializable) {
        if (serializable == null)
            return new CompoundTag();

        Map<String, Tag<?>> compoundValues = new HashMap<>();

        compoundValues.put("type", new StringTag(serializable.getType().name()));
        compoundValues.put("amount", new IntTag(serializable.getAmount()));
        compoundValues.put("data", new ShortTag(serializable.getDurability()));
        compoundValues.put("NBT", plugin.getNMSTags().getNBTTag(serializable));

        return new CompoundTag(compoundValues);
    }

    @Nullable
    @Override
    public ItemStack deserialize(@Nullable CompoundTag deserializable) {
        if (deserializable == null)
            return new ItemStack(Material.AIR);

        Material type = Material.valueOf(deserializable.getString("type"));
        int amount = deserializable.getInt("amount");
        short data = deserializable.getShort("data");

        ItemStack itemStack = new ItemStack(type, amount, data);

        return plugin.getNMSTags().getFromNBTTag(itemStack, deserializable.getCompound("NBT"));
    }

}
