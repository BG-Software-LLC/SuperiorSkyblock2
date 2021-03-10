package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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

    public TagBuilder withBlockType(Location location, Material blockType, int data){
        if(ServerVersion.isLegacy()) {
            compoundValue.add("combinedId", new IntTag(plugin.getNMSBlocks().getCombinedId(location)));
        }
        else {
            compoundValue.add("type", new StringTag(blockType.name()));
            if(data != 0)
                compoundValue.add("data", new IntTag(data));
        }

        return this;
    }

    public TagBuilder withLightLevels(byte[] lightLevels){
        if(lightLevels.length > 0 && lightLevels[0] > 0)
            compoundValue.add("skyLightLevel", new ByteTag(lightLevels[0]));
        if(lightLevels.length > 1 && lightLevels[1] > 0)
            compoundValue.add("blockLightLevel", new ByteTag(lightLevels[1]));
        return this;
    }

    public TagBuilder withStates(CompoundTag statesTag){
        if(statesTag != null)
            compoundValue.add("states", statesTag);
        return this;
    }

    public TagBuilder withTileEntity(CompoundTag tileEntity){
        if(tileEntity != null)
            compoundValue.add("tileEntity", tileEntity);
        return this;
    }

    public TagBuilder applyEntity(Entity entity, Location min){
        if(!(entity instanceof Player)) {
            Location offset = entity.getLocation().subtract(min);
            compoundValue.add("entityType", new StringTag(entity.getType().name()));
            compoundValue.add("offset", new StringTag(LocationUtils.getLocation(offset)));
            compoundValue.add("NBT", plugin.getNMSTags().getNBTTag(entity));
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
