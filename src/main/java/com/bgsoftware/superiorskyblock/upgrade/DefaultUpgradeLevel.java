package com.bgsoftware.superiorskyblock.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.upgrade.cost.EmptyUpgradeCost;

import java.util.Collections;

public final class DefaultUpgradeLevel extends SUpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final DefaultUpgradeLevel INSTANCE = new DefaultUpgradeLevel();

    private DefaultUpgradeLevel() {
        super(-1, EmptyUpgradeCost.getInstance(), Collections.emptyList(), "", Collections.emptySet(),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getCropGrowth()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getSpawnerRates()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getMobDrops()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getTeamLimit()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getWarpsLimit()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getCoopLimit()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getIslandSize()),
                (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getBlockLimits(),
                (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getEntityLimits(),
                (KeyMap<Integer>[]) plugin.getSettings().getDefaultValues().getGenerators(),
                Collections.emptyMap(),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getBankLimit()),
                plugin.getSettings().getDefaultValues().getRoleLimits()
        );
    }

    public static DefaultUpgradeLevel getInstance() {
        return INSTANCE;
    }

}
