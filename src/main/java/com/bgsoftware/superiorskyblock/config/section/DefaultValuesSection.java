package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class DefaultValuesSection extends SettingsContainerHolder implements SettingsManager.DefaultValues {

    @Override
    public int getIslandSize() {
        return getContainer().defaultIslandSize;
    }

    @Override
    public Map<Key, Integer> getBlockLimits() {
        return getContainer().defaultBlockLimits;
    }

    @Override
    public Map<Key, Integer> getEntityLimits() {
        return getContainer().defaultEntityLimits;
    }

    @Override
    public int getWarpsLimit() {
        return getContainer().defaultWarpsLimit;
    }

    @Override
    public int getTeamLimit() {
        return getContainer().defaultTeamLimit;
    }

    @Override
    public int getCoopLimit() {
        return getContainer().defaultCoopLimit;
    }

    @Override
    public double getCropGrowth() {
        return getContainer().defaultCropGrowth;
    }

    @Override
    public double getSpawnerRates() {
        return getContainer().defaultSpawnerRates;
    }

    @Override
    public double getMobDrops() {
        return getContainer().defaultMobDrops;
    }

    @Override
    public BigDecimal getBankLimit() {
        return getContainer().defaultBankLimit;
    }

    @Override
    public Map<Key, Integer>[] getGenerators() {
        Map<Key, Integer>[] generators = new Map[Dimension.values().size()];

        if (!getContainer().defaultGenerator.isEmpty()) {
            for (Dimension dimension : Dimension.values()) {
                Map<Key, Integer> dimensionGeneratorRates = getContainer().defaultGenerator.get(dimension);
                if (dimensionGeneratorRates != null)
                    generators[dimension.ordinal()] = dimensionGeneratorRates;
            }
        }

        return generators;
    }

    @Override
    public Map<Dimension, Map<Key, Integer>> getGeneratorsMap() {
        return getContainer().defaultGenerator.collect(Dimension.values());
    }

    public EnumerateMap<Dimension, Map<Key, Integer>> getRealGeneratorsMap() {
        return getContainer().defaultGenerator;
    }

    @Override
    public Map<Integer, Integer> getRoleLimits() {
        return Collections.unmodifiableMap(getContainer().defaultRoleLimits.asMap());
    }

    public Int2IntMapView getRoleLimitsAsView() {
        return getContainer().defaultRoleLimits;
    }

    @Override
    public Map<PotionEffectType, Integer> getIslandEffects() {
        return getContainer().defaultIslandEffects;
    }
}
