package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public final class TagBuilder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final Map<String, Tag<?>> compoundValue = new HashMap<>();

    public TagBuilder withBlockPosition(SchematicPosition blockPosition){
        compoundValue.put("blockPosition", new StringTag(blockPosition.toString()));
        return this;
    }

    public TagBuilder withBlockType(Location location, Material blockType, int data){
        if(ServerVersion.isLegacy()) {
            compoundValue.put("combinedId", new IntTag(plugin.getNMSAlgorithms().getCombinedId(location)));
        }
        else {
            compoundValue.put("type", new StringTag(blockType.name()));
            if(data != 0)
                compoundValue.put("data", new IntTag(data));
        }

        return this;
    }

    public TagBuilder withLightLevels(byte[] lightLevels){
        if(lightLevels.length > 0 && lightLevels[0] > 0)
            compoundValue.put("skyLightLevel", new ByteTag(lightLevels[0]));
        if(lightLevels.length > 1 && lightLevels[1] > 0)
            compoundValue.put("blockLightLevel", new ByteTag(lightLevels[1]));
        return this;
    }

    public TagBuilder withStates(CompoundTag statesTag){
        if(statesTag != null)
            compoundValue.put("states", statesTag);
        return this;
    }

    public TagBuilder withTileEntity(CompoundTag tileEntity){
        if(tileEntity != null)
            compoundValue.put("tileEntity", tileEntity);
        return this;
    }

    public TagBuilder applyEntity(Entity entity, Location min){
        if(!(entity instanceof Player)) {
            Location offset = entity.getLocation().subtract(min);
            compoundValue.put("entityType", new StringTag(entity.getType().name()));
            compoundValue.put("offset", new StringTag(LocationUtils.getLocation(offset)));
            compoundValue.put("NBT", plugin.getNMSTags().getNBTTag(entity));
        }
        return this;
    }

    public CompoundTag build(){
        Map<String, Tag<?>> compoundValue = new HashMap<>();

        for(String key : this.compoundValue.keySet()){
            if(this.compoundValue.get(key) != null)
                compoundValue.put(key, this.compoundValue.get(key));
        }

        return new CompoundTag(compoundValue);
    }

}
