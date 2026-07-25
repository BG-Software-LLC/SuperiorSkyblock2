package com.bgsoftware.superiorskyblock.island.upgrade.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class IslandUpgrades {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Set<IslandUpgrades> ALL_UPGRADE_CONTAINERS = Sets.newSetFromMap(new WeakHashMap<>());

    static {

    }

    private final Map<String, Integer> enabledUpgrades = new ConcurrentHashMap<>();
    private final Map<String, Integer> upgrades = new ConcurrentHashMap<>();

    public IslandUpgrades() {
        ALL_UPGRADE_CONTAINERS.add(this);
    }

    public void setUpgradeLevel(Upgrade upgrade, int level) {
        setUpgradeLevelInternal(upgrade.getName(), Math.min(upgrade.getMaxUpgradeLevel(), level));
    }

    public void setUpgradeLevels(Map<String, Integer> upgrades) {
        upgrades.forEach(this::setUpgradeLevelInternal);
    }

    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        return upgrade.getUpgradeLevel(this.enabledUpgrades.getOrDefault(upgrade.getName(), 1));
    }

    public Map<String, Integer> getUpgrades() {
        return Collections.unmodifiableMap(this.enabledUpgrades);
    }

    private void setUpgradeLevelInternal(String upgradeName, int level) {
        this.enabledUpgrades.put(upgradeName, level);
        this.upgrades.put(upgradeName, level);
    }

    public static void onUpgradesUpdate() {
        for (IslandUpgrades islandUpgrades : ALL_UPGRADE_CONTAINERS) {
            islandUpgrades.enabledUpgrades.clear();
            islandUpgrades.upgrades.forEach((upgradeName, level) -> {
                Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);
                if (upgrade != null)
                    islandUpgrades.setUpgradeLevel(upgrade, level);
            });
        }
    }

}
