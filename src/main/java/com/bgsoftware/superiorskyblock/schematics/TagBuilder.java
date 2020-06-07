package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.tags.TagUtils;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;

import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public final class TagBuilder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final Registry<String, Tag<?>> compoundValue = Registry.createRegistry();

    public TagBuilder withBlockPosition(SchematicPosition blockPosition){
        compoundValue.add("blockPosition", new StringTag(blockPosition.toString()));
        return this;
    }

    public TagBuilder withCombinedId(int combinedId){
        compoundValue.add("combinedId", new IntTag(combinedId));
        return this;
    }

    public TagBuilder applyBanner(Banner banner){
        compoundValue.add("baseColor", new StringTag(banner.getBaseColor().name()));
        compoundValue.add("patterns", getTagFromPatterns(banner.getPatterns()));
        return this;
    }

    public TagBuilder applyContents(BlockState blockState){
        compoundValue.add("contents", TagUtils.inventoryToCompound(((InventoryHolder) blockState).getInventory().getContents()));
        compoundValue.add("name", new StringTag(plugin.getNMSBlocks().getTileName(blockState.getLocation())));
        return this;
    }

    public TagBuilder applyFlower(ItemStack flower){
        compoundValue.add("flower", new StringTag(flower.getType().name() + ":" + flower.getDurability()));
        return this;
    }


    public TagBuilder applySkull(Skull skull){
        compoundValue.add("skullType", new StringTag(skull.getSkullType().name()));
        compoundValue.add("rotation", new StringTag(skull.getRotation().name()));
        if(skull.getOwner() != null)
             compoundValue.add("owner", new StringTag(skull.getOwner()));
        return this;
    }

    public void applySign(Sign sign) {
        for(int i = 0; i < 4; i++)
            compoundValue.add("signLine" + i, new StringTag(sign.getLine(i)));
    }

    public void applySpawner(CreatureSpawner creatureSpawner) {
        compoundValue.add("spawnedType", new StringTag(creatureSpawner.getSpawnedType().name()));
    }

    public TagBuilder applyEntity(LivingEntity livingEntity, Location min){
        if(!(livingEntity instanceof Player)) {
            Location offset = livingEntity.getLocation().subtract(min);
            compoundValue.add("entityType", new StringTag(livingEntity.getType().name()));
            compoundValue.add("offset", new StringTag(LocationUtils.getLocation(offset)));
            compoundValue.add("NBT", plugin.getNMSTags().getNBTTag(livingEntity));
        }
        return this;
    }

    public CompoundTag build(){
        Map<String, Tag<?>> compoundValue = new HashMap<>();

        for(String key : this.compoundValue.keys()){
            if(this.compoundValue.get(key) != null)
                compoundValue.put(key, this.compoundValue.get(key));
        }

        return new CompoundTag(compoundValue);
    }

    private CompoundTag getTagFromPatterns(List<Pattern> patterns){
        Map<String, Tag<?>> compoundValue = new HashMap<>();

        for(int i = 0; i < patterns.size(); i++){
            Map<String, Tag<?>> _compoundValue = new HashMap<>();
            Pattern pattern = patterns.get(i);

            _compoundValue.put("color", new StringTag(pattern.getColor().name()));
            _compoundValue.put("type", new StringTag(pattern.getPattern().name()));

            compoundValue.put(i + "", new CompoundTag(_compoundValue));
        }

        return new CompoundTag(compoundValue);
    }

}
