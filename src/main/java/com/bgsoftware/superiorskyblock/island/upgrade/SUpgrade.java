package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.EmptyUpgradeCost;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
            KeyMaps.createEmptyMap(),
            KeyMaps.createEmptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            Value.syncedFixed(new BigDecimal(-2)),
            Collections.emptyMap());

    private final String name;

    private SUpgradeLevel[] upgradeLevels = new SUpgradeLevel[0];
    private final Set<Integer> slots = new LinkedHashSet<>();

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
        return getSlots().get(0);
    }

    @Override
    public List<Integer> getSlots() {
        return Collections.unmodifiableList(new LinkedList<>(this.slots));
    }

    @Override
    public boolean isSlot(int slot) {
        return this.slots.contains(slot);
    }

    @Override
    public void setSlot(int slot) {
        this.slots.add(slot);
    }

    @Override
    public void setSlots(@Nullable List<Integer> slots) {
        if (slots == null) {
            this.slots.clear();
        } else {
            this.slots.addAll(slots);
        }
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
