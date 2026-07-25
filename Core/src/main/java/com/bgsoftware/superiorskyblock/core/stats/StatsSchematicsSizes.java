package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.mutable.MutableLong;
import com.bgsoftware.superiorskyblock.world.schematic.impl.SuperiorSchematic;
import com.google.gson.JsonObject;

import java.util.List;

public class StatsSchematicsSizes implements IStatsCollector {

    public static final StatsSchematicsSizes INSTANCE = new StatsSchematicsSizes();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private boolean collected = false;

    private StatsSchematicsSizes() {

    }

    @Override
    public void collect(JsonObject statsObject) {
        if (collected)
            return;

        collected = true;

        List<String> schematicNames = plugin.getSchematics().getSchematics();
        if (schematicNames.isEmpty())
            return;

        JsonObject schematics = new JsonObject();

        schematicNames.forEach(schematicName -> {
            Schematic schematic = plugin.getSchematics().getSchematic(schematicName);
            if (schematic instanceof SuperiorSchematic) {
                MutableLong blocksCount = new MutableLong(0);
                schematic.getBlockCounts().values().forEach(count -> blocksCount.set(blocksCount.get() + count));
                if (blocksCount.get() > 0)
                    schematics.addProperty(schematicName, blocksCount.get());
            }
        });

        statsObject.add("schematics", schematics);
    }
}
