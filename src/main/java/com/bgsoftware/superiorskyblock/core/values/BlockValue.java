package com.bgsoftware.superiorskyblock.core.values;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class BlockValue {

    public static final BlockValue ZERO = new BlockValue(BigDecimal.ZERO, BigDecimal.ZERO);

    private final BigDecimal worth;
    private final BigDecimal level;

    private static boolean isZero(@Nullable BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    public static BlockValue ofWorth(@Nullable BigDecimal worth) {
        return ofWorthAndLevel(worth, null);
    }

    public static BlockValue ofLevel(@Nullable BigDecimal level) {
        return ofWorthAndLevel(null, level);
    }

    public static BlockValue ofWorthAndLevel(@Nullable BigDecimal worth, @Nullable BigDecimal level) {
        return isZero(worth) && isZero(level) ? ZERO : new BlockValue(worth, level);
    }

    private BlockValue(@Nullable BigDecimal worth, @Nullable BigDecimal level) {
        this.worth = worth == null ? BigDecimal.ZERO : worth;
        this.level = level == null ? BigDecimal.ZERO : level;
    }

    public BigDecimal getWorth() {
        return this.worth;
    }

    public BlockValue setWorth(@Nullable BigDecimal worth) {
        return ofWorthAndLevel(worth, this.level);
    }

    public BigDecimal getLevel() {
        return this.level;
    }

    public BlockValue setLevel(@Nullable BigDecimal level) {
        return ofWorthAndLevel(this.worth, level);
    }

    @Override
    public String toString() {
        return "BlockValue{" +
                "worth=" + worth +
                ", level=" + level +
                '}';
    }
}
