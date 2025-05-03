package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.value.DoubleValue;
import com.bgsoftware.superiorskyblock.core.value.IntValue;
import com.bgsoftware.superiorskyblock.core.value.Value;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.EmptyUpgradeCost;

import java.util.Collections;

public class DefaultUpgradeLevel extends SUpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final DefaultUpgradeLevel INSTANCE = new DefaultUpgradeLevel();

    private DefaultUpgradeLevel() {
        super(-1, EmptyUpgradeCost.getInstance(), Collections.emptyList(), "", Collections.emptySet(),
                DoubleValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getCropGrowth()),
                DoubleValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getSpawnerRates()),
                DoubleValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getMobDrops()),
                IntValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getTeamLimit()),
                IntValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getWarpsLimit()),
                IntValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getCoopLimit()),
                IntValue.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getIslandSize()),
                Value.syncedSupplied(() -> (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getBlockLimits()),
                Value.syncedSupplied(() -> (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getEntityLimits()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getRealGeneratorsMap()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getIslandEffects()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getBankLimit()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getRoleLimitsAsView())
        );
    }

    public static DefaultUpgradeLevel getInstance() {
        return INSTANCE;
    }

}
