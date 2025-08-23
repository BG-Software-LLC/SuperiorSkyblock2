package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicEntityFilter;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@SuppressWarnings("UnusedReturnValue")
public class SchematicBuilder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final CompoundTag compoundTag = CompoundTag.of();

    public SchematicBuilder withBlockOffset(BlockOffset blockOffset) {
        this.compoundTag.setString("blockPosition", Serializers.OFFSET_SERIALIZER.serialize(blockOffset));
        return this;
    }

    public SchematicBuilder withBlockType(Material blockType, int data) {
        if (ServerVersion.isLegacy()) {
            this.compoundTag.setInt("combinedId", plugin.getNMSAlgorithms().getCombinedId(blockType, (byte) data));
        } else {
            this.compoundTag.setString("type", blockType.name());
            if (data != 0)
                this.compoundTag.setInt("data", data);
        }

        return this;
    }

    public SchematicBuilder withLightLevels(byte[] lightLevels) {
        if (lightLevels.length > 0 && lightLevels[0] > 0)
            this.compoundTag.setByte("skyLightLevel", lightLevels[0]);
        if (lightLevels.length > 1 && lightLevels[1] > 0)
            this.compoundTag.setByte("blockLightLevel", lightLevels[1]);
        return this;
    }

    public SchematicBuilder withStates(CompoundTag statesTag) {
        if (statesTag != null)
            this.compoundTag.setTag("states", statesTag);
        return this;
    }

    public SchematicBuilder withTileEntity(CompoundTag tileEntity) {
        if (tileEntity != null)
            this.compoundTag.setTag("tileEntity", tileEntity);
        return this;
    }

    public SchematicBuilder applyEntity(EntityType entityType, CompoundTag entityTag, Location offset) {
        this.compoundTag.setString("offset", Serializers.LOCATION_SERIALIZER.serialize(offset));
        this.compoundTag.setString("entityType", entityType.name());
        this.compoundTag.setTag("NBT", SchematicEntityFilter.filterNBTTag(entityTag));

        return this;
    }

    public CompoundTag build() {
        return this.compoundTag;
    }

}
