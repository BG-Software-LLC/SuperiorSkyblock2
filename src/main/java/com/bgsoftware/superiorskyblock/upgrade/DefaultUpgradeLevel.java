package com.bgsoftware.superiorskyblock.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.upgrade.cost.EmptyUpgradeCost;

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
                newSyncedDoubleValue(v -> plugin.getSettings().getDefaultValues().getCropGrowth()),
                newSyncedDoubleValue(v -> plugin.getSettings().getDefaultValues().getSpawnerRates()),
                newSyncedDoubleValue(v -> plugin.getSettings().getDefaultValues().getMobDrops()),
                newSyncedIntegerValue(v -> plugin.getSettings().getDefaultValues().getTeamLimit()),
                newSyncedIntegerValue(v -> plugin.getSettings().getDefaultValues().getWarpsLimit()),
                newSyncedIntegerValue(v -> plugin.getSettings().getDefaultValues().getCoopLimit()),
                newSyncedIntegerValue(v -> plugin.getSettings().getDefaultValues().getIslandSize()),
                (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getBlockLimits(),
                (KeyMap<Integer>) plugin.getSettings().getDefaultValues().getEntityLimits(),
                (KeyMap<Integer>[]) plugin.getSettings().getDefaultValues().getGenerators(),
                new HashMap<>(),
                newSyncedBigDecimalValue(v -> plugin.getSettings().getDefaultValues().getBankLimit()),
                plugin.getSettings().getDefaultValues().getRoleLimits()
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
