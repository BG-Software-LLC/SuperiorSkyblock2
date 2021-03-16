package com.bgsoftware.superiorskyblock.upgrades;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.upgrades.cost.EmptyUpgradeCost;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

public final class DefaultUpgradeLevel extends SUpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final DefaultUpgradeLevel INSTANCE = new DefaultUpgradeLevel();

    private DefaultUpgradeLevel(){
        super(-1, EmptyUpgradeCost.getInstance(), new ArrayList<>(), "", new HashSet<>(),
                newSyncedDoubleValue(v -> (double) plugin.getSettings().defaultCropGrowth),
                newSyncedDoubleValue(v -> plugin.getSettings().defaultSpawnerRates),
                newSyncedDoubleValue(v -> plugin.getSettings().defaultMobDrops),
                newSyncedIntegerValue(v -> plugin.getSettings().defaultTeamLimit),
                newSyncedIntegerValue(v -> plugin.getSettings().defaultWarpsLimit),
                newSyncedIntegerValue(v -> plugin.getSettings().defaultCoopLimit),
                newSyncedIntegerValue(v -> plugin.getSettings().defaultIslandSize),
                plugin.getSettings().defaultBlockLimits,
                plugin.getSettings().defaultEntityLimits,
                plugin.getSettings().defaultGenerator,
                new HashMap<>(),
                newSyncedBigDecimalValue(v -> plugin.getSettings().defaultBankLimit),
                plugin.getSettings().defaultRoleLimits
        );
    }

    public static DefaultUpgradeLevel getInstance(){
        return INSTANCE;
    }

    private static UpgradeValue<Double> newSyncedDoubleValue(Function<Object, Double> function){
        return new UpgradeValue<Double>(0D, true) {

            @Override
            public Double get() {
                return function.apply(null);
            }
        };
    }

    private static UpgradeValue<Integer> newSyncedIntegerValue(Function<Object, Integer> function){
        return new UpgradeValue<Integer>(0, true) {

            @Override
            public Integer get() {
                return function.apply(null);
            }
        };
    }

    private static UpgradeValue<BigDecimal> newSyncedBigDecimalValue(Function<Object, BigDecimal> function){
        return new UpgradeValue<BigDecimal>(BigDecimal.ZERO, true) {

            @Override
            public BigDecimal get() {
                return function.apply(null);
            }
        };
    }

}
