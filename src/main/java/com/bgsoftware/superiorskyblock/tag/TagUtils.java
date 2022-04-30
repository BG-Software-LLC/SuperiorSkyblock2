package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class TagUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private TagUtils() {

    }

    public static CompoundTag itemToCompound(ItemStack itemStack) {
        Map<String, Tag<?>> compoundValues = new HashMap<>();

        compoundValues.put("type", new StringTag(itemStack.getType().name()));
        compoundValues.put("amount", new IntTag(itemStack.getAmount()));
        compoundValues.put("data", new ShortTag(itemStack.getDurability()));
        compoundValues.put("NBT", plugin.getNMSTags().getNBTTag(itemStack));

        return new CompoundTag(compoundValues);
    }

    public static ItemStack compoundToItem(CompoundTag compoundTag) {
        Material type = Material.valueOf(compoundTag.getString("type"));
        int amount = compoundTag.getInt("amount");
        short data = compoundTag.getShort("data");

        ItemStack itemStack = new ItemStack(type, amount, data);

        return plugin.getNMSTags().getFromNBTTag(itemStack, compoundTag.getCompound("NBT"));
    }

}
