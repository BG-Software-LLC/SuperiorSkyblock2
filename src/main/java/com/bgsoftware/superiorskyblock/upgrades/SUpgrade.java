package com.bgsoftware.superiorskyblock.upgrades;

import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.upgrades.cost.EmptyUpgradeCost;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class SUpgrade implements Upgrade {

    private static final SUpgradeLevel NULL_LEVEL = new SUpgradeLevel(0, EmptyUpgradeCost.getInstance(), new ArrayList<>(), "",
            new HashSet<>(), UpgradeValue.NEGATIVE_DOUBLE, UpgradeValue.NEGATIVE_DOUBLE, UpgradeValue.NEGATIVE_DOUBLE,
            UpgradeValue.NEGATIVE, UpgradeValue.NEGATIVE, UpgradeValue.NEGATIVE, UpgradeValue.NEGATIVE,
            new KeyMap<>(), new KeyMap<>(), new KeyMap[3], new HashMap<>(), UpgradeValue.NEGATIVE_BIG_DECIMAL,
            new HashMap<>());

    private final String name;

    private SUpgradeLevel[] upgradeLevels = new SUpgradeLevel[0];
    private int slot = -1;

    public SUpgrade(String name){
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

    public int getMenuSlot() {
        return slot;
    }

    public void setMenuSlot(int slot){
        this.slot = slot;
    }

    public void addUpgradeLevel(int level, SUpgradeLevel upgradeLevel){
        if(level > upgradeLevels.length)
            upgradeLevels = Arrays.copyOf(upgradeLevels, level);

        upgradeLevels[level - 1] = upgradeLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SUpgrade upgrade = (SUpgrade) o;
        return name.equals(upgrade.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
