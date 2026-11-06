package com.bgsoftware.superiorskyblock.service.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.world.schematic.BaseSchematic;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class IslandEndCrystals {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Nullable
    private static final EntityType END_CRYSTAL_TYPE = EnumHelper.getEnum(EntityType.class, "END_CRYSTAL", "ENDER_CRYSTAL");

    private IslandEndCrystals() {

    }

    /**
     * Get the locations of the end crystals in the schematic of the island in the given dimension.
     * These crystals stand on the spikes of the island, similar to the crystals on the vanilla end spikes.
     * If the schematic cannot be found anymore, or it has no end crystals, an empty list is returned.
     *
     * @param island    The island to get the end crystals of.
     * @param dimension The dimension of the schematic.
     */
    public static List<Location> getSchematicCrystals(Island island, Dimension dimension) {
        if (END_CRYSTAL_TYPE == null)
            return Collections.emptyList();

        String islandSchematic = island.getSchematicName();
        if (islandSchematic.isEmpty())
            return Collections.emptyList();

        // Same schematic names that are used when the island and its dimensions are generated.
        String schematicName = dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension() ?
                islandSchematic : islandSchematic + "_" + dimension.getName().toLowerCase(Locale.ENGLISH);

        Schematic schematic = plugin.getSchematics().getSchematic(schematicName);
        if (!(schematic instanceof BaseSchematic))
            return Collections.emptyList();

        BlockPosition center = island.getCenterPosition();

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location schematicLocation = wrapper.getHandle();
            schematicLocation.setWorld(plugin.getGrid().getIslandsWorld(island, dimension));
            schematicLocation.setX(center.getX());
            // Schematics are pasted one block below the center of the island.
            schematicLocation.setY(center.getY() - 1);
            schematicLocation.setZ(center.getZ());
            return ((BaseSchematic) schematic).getEntityLocations(END_CRYSTAL_TYPE, schematicLocation);
        }
    }

}
