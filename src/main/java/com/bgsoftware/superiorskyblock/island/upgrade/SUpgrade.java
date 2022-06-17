package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.EmptyUpgradeCost;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import org.bukkit.World;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class SUpgrade implements Upgrade {

    private static final SUpgradeLevel NULL_LEVEL = new SUpgradeLevel(0,
            EmptyUpgradeCost.getInstance(),
            Collections.emptyList(),
            "",
            Collections.emptySet(),
            Value.syncedFixed(-1D),
            Value.syncedFixed(-1D),
            Value.syncedFixed(-1D),
            Value.syncedFixed(-1),
            Value.syncedFixed(-1),
            Value.syncedFixed(-1),
            Value.syncedFixed(-1),
            KeyMapImpl.createEmptyMap(),
            KeyMapImpl.createEmptyMap(),
            new KeyMap[World.Environment.values().length],
            Collections.emptyMap(),
            Value.syncedFixed(new BigDecimal(-2)),
            Collections.emptyMap());

    private final String name;

    private SUpgradeLevel[] upgradeLevels = new SUpgradeLevel[0];
    private int slot = -1;

    public SUpgrade(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SUpgradeLevel getUpgradeLevel(int level) {
        return level <= 0 || level > upgradeLevels.length ? NULL_LEVEL : upgradeLevels[level - 1];
    }

    @Override
    public int getMaxUpgradeLevel() {
        return upgradeLevels.length;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void addUpgradeLevel(int level, SUpgradeLevel upgradeLevel) {
        if (level > upgradeLevels.length)
            upgradeLevels = Arrays.copyOf(upgradeLevels, level);

        upgradeLevels[level - 1] = upgradeLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SUpgrade upgrade = (SUpgrade) o;
        return name.equals(upgrade.name);
    }

}
