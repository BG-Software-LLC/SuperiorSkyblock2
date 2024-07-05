package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStack2TagSerializer implements ISerializer<ItemStack, CompoundTag> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ItemStack2TagSerializer INSTANCE = new ItemStack2TagSerializer();

    public static ItemStack2TagSerializer getInstance() {
        return INSTANCE;
    }

    private ItemStack2TagSerializer() {
    }

    @Override
    @NotNull
    public CompoundTag serialize(@Nullable ItemStack serializable) {
        if (serializable == null)
            return new CompoundTag();

        return plugin.getNMSTags().serializeItem(serializable);
    }

    @Nullable
    @Override
    public ItemStack deserialize(@Nullable CompoundTag deserializable) {
        if (deserializable == null)
            return new ItemStack(Material.AIR);

        return plugin.getNMSTags().deserializeItem(deserializable);
    }

}
