package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.value.Value;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.EmptyUpgradeCost;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class DefaultUpgradeLevel extends SUpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final DefaultUpgradeLevel INSTANCE = new DefaultUpgradeLevel();

    private DefaultUpgradeLevel() {
        super(-1, EmptyUpgradeCost.getInstance(), Collections.emptyList(), "", Collections.emptySet(),
                Value.syncedSupplied(() -> OptionalDouble.of(plugin.getSettings().getDefaultValues().getCropGrowth())),
                Value.syncedSupplied(() -> OptionalDouble.of(plugin.getSettings().getDefaultValues().getSpawnerRates())),
                Value.syncedSupplied(() -> OptionalDouble.of(plugin.getSettings().getDefaultValues().getMobDrops())),
                Value.syncedSupplied(() -> OptionalInt.of(plugin.getSettings().getDefaultValues().getTeamLimit())),
                Value.syncedSupplied(() -> OptionalInt.of(plugin.getSettings().getDefaultValues().getWarpsLimit())),
                Value.syncedSupplied(() -> OptionalInt.of(plugin.getSettings().getDefaultValues().getCoopLimit())),
                Value.syncedSupplied(() -> OptionalInt.of(plugin.getSettings().getDefaultValues().getIslandSize())),
                Value.syncedSupplied(() -> (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getBlockLimits()),
                Value.syncedSupplied(() -> (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getEntityLimits()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getRealGeneratorsMap()),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getIslandEffects()),
                Value.syncedSupplied(() -> Optional.of(plugin.getSettings().getDefaultValues().getBankLimit())),
                Value.syncedSupplied(() -> plugin.getSettings().getDefaultValues().getRoleLimitsAsView())
        );
    }

    public static DefaultUpgradeLevel getInstance() {
        return INSTANCE;
    }

}
