package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

import java.util.List;

public class SpawnSection implements SettingsManager.Spawn {

    private final SettingsContainer container;

    public SpawnSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public String getLocation() {
        return this.container.spawnLocation;
    }

    @Override
    public boolean isProtected() {
        return this.container.spawnProtection;
    }

    @Override
    public List<String> getSettings() {
        return this.container.spawnSettings;
    }

    @Override
    public List<String> getPermissions() {
        return this.container.spawnPermissions;
    }

    @Override
    public boolean isWorldBorder() {
        return this.container.spawnWorldBorder;
    }

    @Override
    public int getSize() {
        return this.container.spawnSize;
    }

    @Override
    public boolean isPlayersDamage() {
        return this.container.spawnDamage;
    }

}
