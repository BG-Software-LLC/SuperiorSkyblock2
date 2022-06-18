package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

import java.math.BigDecimal;
import java.util.Map;

public class DefaultValuesSection implements SettingsManager.DefaultValues {

    private final SettingsContainer container;

    public DefaultValuesSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public int getIslandSize() {
        return this.container.defaultIslandSize;
    }

    @Override
    public Map<Key, Integer> getBlockLimits() {
        return this.container.defaultBlockLimits;
    }

    @Override
    public Map<Key, Integer> getEntityLimits() {
        return this.container.defaultEntityLimits;
    }

    @Override
    public int getWarpsLimit() {
        return this.container.defaultWarpsLimit;
    }

    @Override
    public int getTeamLimit() {
        return this.container.defaultTeamLimit;
    }

    @Override
    public int getCoopLimit() {
        return this.container.defaultCoopLimit;
    }

    @Override
    public double getCropGrowth() {
        return this.container.defaultCropGrowth;
    }

    @Override
    public double getSpawnerRates() {
        return this.container.defaultSpawnerRates;
    }

    @Override
    public double getMobDrops() {
        return this.container.defaultMobDrops;
    }

    @Override
    public BigDecimal getBankLimit() {
        return this.container.defaultBankLimit;
    }

    @Override
    public Map<Key, Integer>[] getGenerators() {
        return this.container.defaultGenerator;
    }

    @Override
    public Map<Integer, Integer> getRoleLimits() {
        return this.container.defaultRoleLimits;
    }

}
