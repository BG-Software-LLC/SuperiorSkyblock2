package com.bgsoftware.superiorskyblock.upgrade;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class UpgradeValue<T> {

    public static final UpgradeValue<Integer> ZERO = new UpgradeValue<>(0, false);

    public static final UpgradeValue<Integer> NEGATIVE = new UpgradeValue<>(-1, true);
    public static final UpgradeValue<Double> NEGATIVE_DOUBLE = new UpgradeValue<>(-1D, true);
    public static final UpgradeValue<BigDecimal> NEGATIVE_BIG_DECIMAL = new UpgradeValue<>(new BigDecimal(-1), true);

    private final boolean upgradeValue;
    private final T value;

    public UpgradeValue(T value, Predicate<T> shouldBeSync){
        this(value, shouldBeSync.test(value));
    }

    public UpgradeValue(T value, boolean upgradeValue){
        this.upgradeValue = upgradeValue;
        this.value = value;
    }

    public T get(){
        return value;
    }

    public boolean isSynced(){
        return upgradeValue;
    }

}
