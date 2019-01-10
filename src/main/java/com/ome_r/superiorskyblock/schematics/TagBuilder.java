package com.ome_r.superiorskyblock.schematics;

import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.utils.jnbt.CompoundTag;
import com.ome_r.superiorskyblock.utils.jnbt.IntTag;
import com.ome_r.superiorskyblock.utils.jnbt.StringTag;
import com.ome_r.superiorskyblock.utils.jnbt.Tag;
import com.ome_r.superiorskyblock.utils.TagUtil;
import com.ome_r.superiorskyblock.wrappers.BlockPosition;

import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagBuilder {

    private static SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();
    private Map<String, Tag> compoundValue = new HashMap<>();

    public TagBuilder withBlockPosition(BlockPosition blockPosition){
        compoundValue.put("blockPosition", new StringTag(blockPosition.toString()));
        return this;
    }

    public TagBuilder withCombinedId(int combinedId){
        compoundValue.put("combinedId", new IntTag(combinedId));
        return this;
    }

    public TagBuilder applyBanner(Banner banner){
        compoundValue.put("baseColor", new StringTag(banner.getBaseColor().name()));
        compoundValue.put("patterns", getTagFromPatterns(banner.getPatterns()));
        return this;
    }

    public TagBuilder applyContents(ItemStack[] contents){
        compoundValue.put("contents", TagUtil.inventoryToCompound(contents));
        return this;
    }

    public TagBuilder applyFlower(ItemStack flower){
        compoundValue.put("flower", new StringTag(flower.getType().name() + ":" + flower.getDurability()));
        return this;
    }


    public TagBuilder applySkull(Skull skull){
        compoundValue.put("skullType", new StringTag(skull.getSkullType().name()));
        compoundValue.put("rotation", new StringTag(skull.getRotation().name()));
        compoundValue.put("owner", new StringTag(skull.getOwner()));
        return this;
    }

    public void applySign(Sign sign) {
        for(int i = 0; i < 4; i++)
            compoundValue.put("signLine" + i, new StringTag(sign.getLine(i)));
    }

    public TagBuilder applyEntity(LivingEntity livingEntity, Location center){
        WrappedLocation offset = WrappedLocation.of(livingEntity.getLocation().subtract(center));
        compoundValue.put("entityType", new StringTag(livingEntity.getType().name()));
        compoundValue.put("offset", new StringTag(offset.toString()));
        compoundValue.put("NBT", plugin.getNMSAdapter().getNBTTag(livingEntity));
        return this;
    }

    public CompoundTag build(){
        return new CompoundTag(compoundValue);
    }

    private CompoundTag getTagFromPatterns(List<Pattern> patterns){
        Map<String, Tag> compoundValue = new HashMap<>();

        Map<String, Tag> _compoundValue;
        Pattern pattern;
        for(int i = 0; i < patterns.size(); i++){
            pattern = patterns.get(i);
            _compoundValue = new HashMap<>();

            _compoundValue.put("color", new StringTag(pattern.getColor().name()));
            _compoundValue.put("type", new StringTag(pattern.getPattern().name()));

            compoundValue.put(i + "", new CompoundTag(_compoundValue));
        }

        return new CompoundTag(compoundValue);
    }

}
