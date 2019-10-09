package com.bgsoftware.superiorskyblock.utils.tags;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class TagUtils {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void assignIntoBlocks(Island island, List<Tag> blocks, Location offset, Runnable callback){
        Runnable _callback = () ->
            Executor.sync(() -> {
                for(Tag tag : blocks){
                    assignIntoBlock(island, (CompoundTag) tag, offset);
                }
                callback.run();
            }, 1L);

        Executor.async(() -> {
            for (Tag tag : blocks) {
                Map<String, Tag> compoundValue = ((CompoundTag) tag).getValue();
                Location block = SchematicPosition.of(((StringTag) compoundValue.get("blockPosition")).getValue()).addToLocation(offset);
                int combinedId = ((IntTag) compoundValue.get("combinedId")).getValue();
                Executor.sync(() -> {
                    plugin.getNMSAdapter().setBlock(block, combinedId);
                    if (blocks.indexOf(tag) == blocks.size() - 1)
                        _callback.run();
                });
            }
        });
    }

    public static void assignIntoBlock(Island island, CompoundTag compoundTag, Location offset){
        Map<String, Tag> compoundValue = compoundTag.getValue();
        Location blockLocation = SchematicPosition.of(((StringTag) compoundValue.get("blockPosition")).getValue()).addToLocation(offset);
        Block block = blockLocation.getBlock();
        if(block.getState() instanceof Banner){
            Banner banner = (Banner) block.getState();
            if(compoundValue.containsKey("baseColor"))
                banner.setBaseColor(DyeColor.valueOf(((StringTag) compoundValue.get("baseColor")).getValue()));
            if(compoundValue.containsKey("patterns"))
                banner.setPatterns(getPatternsFromTag((CompoundTag) compoundValue.get("patterns")));
            banner.update();
        }
        else if(block.getState() instanceof InventoryHolder) {
            if(compoundValue.containsKey("contents"))
                ((InventoryHolder) block.getState()).getInventory().setContents(compoundToInventory((CompoundTag) compoundValue.get("contents")));
        }
        else if(block.getType() == Material.FLOWER_POT){
            if(compoundValue.containsKey("flower")) {
                String[] sections = ((StringTag) compoundValue.get("flower")).getValue().split(":");
                plugin.getNMSAdapter().setFlowerPot(blockLocation, new ItemStack(Material.valueOf(sections[0]), 1, Short.parseShort(sections[1])));
            }
        }
        else if(block.getState() instanceof Skull){
            Skull skull = (Skull) block.getState();
            if(compoundValue.containsKey("skullType"))
                try {
                    skull.setSkullType(SkullType.valueOf(((StringTag) compoundValue.get("skullType")).getValue()));
                }catch(UnsupportedOperationException ignored){}
            if(compoundValue.containsKey("rotation"))
                skull.setRotation(BlockFace.valueOf(((StringTag) compoundValue.get("rotation")).getValue()));
            if(compoundValue.containsKey("owner"))
                skull.setOwner(((StringTag) compoundValue.get("owner")).getValue());
            skull.update();
        }else if(block.getState() instanceof Sign){
            Sign sign = (Sign) block.getState();
            for(int i = 0; i < 4; i++)
                if(compoundValue.containsKey("signLine" + i))
                    sign.setLine(i, ((StringTag) compoundValue.get("signLine" + i)).getValue()
                            .replace("{player}", island == null ? "" : island.getOwner().getName())
                            .replace("{island}", island == null ? "" : island.getName()));
            sign.update();
        }
    }

    public static void spawnEntity(CompoundTag compoundTag, Location center){
        Map<String, Tag> compoundValue = compoundTag.getValue();
        EntityType entityType = EntityType.valueOf(((StringTag) compoundValue.get("entityType")).getValue());
        SBlockPosition offset = SBlockPosition.of(((StringTag) compoundValue.get("offset")).getValue());
        CompoundTag nbtTagCompound = (CompoundTag) compoundValue.get("NBT");

        LivingEntity livingEntity = (LivingEntity) center.getWorld().spawnEntity(offset.parse(center.getWorld()).add(center), entityType);
        plugin.getNMSAdapter().getFromNBTTag(livingEntity, nbtTagCompound);
    }

    public static CompoundTag inventoryToCompound(ItemStack[] itemStacks){
        Map<String, Tag> compoundValues = new HashMap<>();

        for(int i = 0; i < itemStacks.length; i++){
            if(itemStacks[i] != null && itemStacks[i].getType() != Material.AIR){
                compoundValues.put(i + "", itemToCompound(itemStacks[i]));
            }
        }

        return new CompoundTag(compoundValues);
    }

    public static ItemStack[] compoundToInventory(CompoundTag compoundTag){
        Map<String, Tag> compoundValue = compoundTag.getValue();

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
        Map<String, Tag> compoundValues = new HashMap<>();

        compoundValues.put("type", new StringTag(itemStack.getType().name()));
        compoundValues.put("amount", new IntTag(itemStack.getAmount()));
        compoundValues.put("data", new ShortTag(itemStack.getDurability()));
        compoundValues.put("NBT", nbtTagToCompound(itemStack));

        return new CompoundTag(compoundValues);
    }

    public static ItemStack compoundToItem(CompoundTag compoundTag) {
        Map<String, Tag> compoundValues = compoundTag.getValue();

        Material type = Material.valueOf(((StringTag) compoundValues.get("type")).getValue());
        int amount = ((IntTag) compoundValues.get("amount")).getValue();
        short data = ((ShortTag) compoundValues.get("data")).getValue();

        ItemStack itemStack = new ItemStack(type, amount, data);

        return compoundToNBTTag(itemStack, (CompoundTag) compoundValues.get("NBT"));
    }

    public static CompoundTag nbtTagToCompound(ItemStack itemStack){
        return plugin.getNMSAdapter().getNBTTag(itemStack);
    }

    public static ItemStack compoundToNBTTag(ItemStack itemStack, CompoundTag compoundTag){
        return plugin.getNMSAdapter().getFromNBTTag(itemStack, compoundTag);
    }

    private static List<Pattern> getPatternsFromTag(CompoundTag tag){
        List<Pattern> patterns = new ArrayList<>();
        Map<String, Tag> compoundValues = tag.getValue();
        int counter = 0;

        while(compoundValues.containsKey(counter + "")){
            Map<String, Tag> patternValues = ((CompoundTag) compoundValues.get(counter + "")).getValue();

            DyeColor dyeColor = DyeColor.valueOf(((StringTag) patternValues.get("color")).getValue());
            PatternType patternType = PatternType.valueOf(((StringTag) patternValues.get("type")).getValue());

            patterns.add(new Pattern(dyeColor, patternType));

            counter++;
        }

        return patterns;
    }

}
