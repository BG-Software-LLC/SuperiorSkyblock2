package com.bgsoftware.superiorskyblock.utils.upgrades;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class UpgradeValue<T> {

    private final SyncedObject<Pair<T, T>> value;
    private final Predicate<T> syncValue;
    private final BiPredicate<T, T> isGreater;

    private UpgradeValue(T value, T upgradeValue, Predicate<T> syncValue, BiPredicate<T, T> isGreater){
        this.value = SyncedObject.of(new Pair<>(value, upgradeValue));
        this.syncValue = syncValue;
        this.isGreater = isGreater;
    }

    public T get(){
        Pair<T, T> pair = value.get();
        return syncValue.test(pair.getKey()) ? pair.getValue() : pair.getKey();
    }

    public T getValue(){
        return value.get().getKey();
    }

    public void set(T value){
        this.value.write(pair -> pair.setKey(value));
    }

    public void setIfSync(T value){
        Pair<T, T> pair = this.value.get();
        if(syncValue.test(pair.getKey()) && isGreater.test(value, pair.getValue())){
            this.value.write(_pair -> _pair.setValue(value));
        }
    }

    public static UpgradeValue<Double> createDouble(){
        return new UpgradeValue<>(-1D, 0D, v -> v < 0, (v1, v2) -> v1 > v2);
    }

    public static UpgradeValue<Integer> createInteger(){
        return new UpgradeValue<>(-1, 0, v -> v < 0, (v1, v2) -> v1 > v2);
    }

}
