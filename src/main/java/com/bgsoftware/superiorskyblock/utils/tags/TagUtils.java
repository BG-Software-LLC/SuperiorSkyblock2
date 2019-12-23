package com.bgsoftware.superiorskyblock.utils.tags;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class TagUtils {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static CompoundTag inventoryToCompound(ItemStack[] itemStacks){
        Map<String, Tag<?>> compoundValues = new HashMap<>();

        for(int i = 0; i < itemStacks.length; i++){
            if(itemStacks[i] != null && itemStacks[i].getType() != Material.AIR){
                compoundValues.put(i + "", itemToCompound(itemStacks[i]));
            }
        }

        return new CompoundTag(compoundValues);
    }

    public static ItemStack[] compoundToInventory(CompoundTag compoundTag){
        Map<String, Tag<?>> compoundValue = compoundTag.getValue();

        int size = 0;

        for(int i = 0; i < 9*6; i++){
            if(compoundValue.containsKey(i + "") && i >= size)
                size = i;
        }

        ItemStack[] itemStacks = new ItemStack[++size];

        for(int i = 0; i < itemStacks.length; i++){
            if(compoundValue.containsKey(i + "")){
                itemStacks[i] = compoundToItem((CompoundTag) compoundValue.get(i + ""));
            }else{
                itemStacks[i] = null;
            }
        }

        return itemStacks;
    }

    public static CompoundTag itemToCompound(ItemStack itemStack){
        Map<String, Tag<?>> compoundValues = new HashMap<>();

        compoundValues.put("type", new StringTag(itemStack.getType().name()));
        compoundValues.put("amount", new IntTag(itemStack.getAmount()));
        compoundValues.put("data", new ShortTag(itemStack.getDurability()));
        compoundValues.put("NBT", nbtTagToCompound(itemStack));

        return new CompoundTag(compoundValues);
    }

    public static ItemStack compoundToItem(CompoundTag compoundTag) {
        Map<String, Tag<?>> compoundValues = compoundTag.getValue();

        Material type = Material.valueOf(((StringTag) compoundValues.get("type")).getValue());
        int amount = ((IntTag) compoundValues.get("amount")).getValue();
        short data = ((ShortTag) compoundValues.get("data")).getValue();

        ItemStack itemStack = new ItemStack(type, amount, data);

        return compoundToNBTTag(itemStack, (CompoundTag) compoundValues.get("NBT"));
    }

    public static CompoundTag nbtTagToCompound(ItemStack itemStack){
        return plugin.getNMSTags().getNBTTag(itemStack);
    }

    public static ItemStack compoundToNBTTag(ItemStack itemStack, CompoundTag compoundTag){
        return plugin.getNMSTags().getFromNBTTag(itemStack, compoundTag);
    }

    public static List<Pattern> getPatternsFromTag(CompoundTag tag){
        List<Pattern> patterns = new ArrayList<>();
        Map<String, Tag<?>> compoundValues = tag.getValue();
        int counter = 0;

        while(compoundValues.containsKey(counter + "")){
            Map<String, Tag<?>> patternValues = ((CompoundTag) compoundValues.get(counter + "")).getValue();

            DyeColor dyeColor = DyeColor.valueOf(((StringTag) patternValues.get("color")).getValue());
            PatternType patternType = PatternType.valueOf(((StringTag) patternValues.get("type")).getValue());

            patterns.add(new Pattern(dyeColor, patternType));

            counter++;
        }

        return patterns;
    }

}
