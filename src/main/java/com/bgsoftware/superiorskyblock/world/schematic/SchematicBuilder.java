package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicEntityFilter;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class SchematicBuilder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final Map<String, Tag<?>> compoundValue = new HashMap<>();

    public SchematicBuilder withBlockOffset(BlockOffset blockOffset) {
        compoundValue.put("blockPosition", new StringTag(Serializers.OFFSET_SERIALIZER.serialize(blockOffset)));
        return this;
    }

    public SchematicBuilder withBlockType(Location location, Material blockType, int data) {
        if (ServerVersion.isLegacy()) {
            compoundValue.put("combinedId", new IntTag(plugin.getNMSAlgorithms().getCombinedId(location)));
        } else {
            compoundValue.put("type", new StringTag(blockType.name()));
            if (data != 0)
                compoundValue.put("data", new IntTag(data));
        }

        return this;
    }

    public SchematicBuilder withLightLevels(byte[] lightLevels) {
        if (lightLevels.length > 0 && lightLevels[0] > 0)
            compoundValue.put("skyLightLevel", new ByteTag(lightLevels[0]));
        if (lightLevels.length > 1 && lightLevels[1] > 0)
            compoundValue.put("blockLightLevel", new ByteTag(lightLevels[1]));
        return this;
    }

    public SchematicBuilder withStates(CompoundTag statesTag) {
        if (statesTag != null)
            compoundValue.put("states", statesTag);
        return this;
    }

    public SchematicBuilder withTileEntity(CompoundTag tileEntity) {
        if (tileEntity != null)
            compoundValue.put("tileEntity", tileEntity);
        return this;
    }

    public SchematicBuilder applyEntity(Entity entity, Location min) {
        if (!(entity instanceof Player)) {
            Location offset = entity.getLocation().subtract(min);
            compoundValue.put("entityType", new StringTag(entity.getType().name()));
            compoundValue.put("offset", new StringTag(Serializers.LOCATION_SERIALIZER.serialize(offset)));
            compoundValue.put("NBT", SchematicEntityFilter.filterNBTTag(plugin.getNMSTags().getNBTTag(entity)));
        }
        return this;
    }

    public CompoundTag build() {
        Map<String, Tag<?>> compoundValue = new HashMap<>();

        for (String key : this.compoundValue.keySet()) {
            if (this.compoundValue.get(key) != null)
                compoundValue.put(key, this.compoundValue.get(key));
        }

        return new CompoundTag(compoundValue);
    }

}
