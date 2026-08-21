package com.bgsoftware.superiorskyblock.island.flag;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.LazyReference;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class IslandFlagConfigImpl implements IslandFlag.Config {

    @Nullable
    private final Consumer<Island> onDisableCallback;
    @Nullable
    private final Consumer<Island> onEnableCallback;
    @Nullable
    private final List<String> conflictingFlagsNames;

    private final LazyReference<List<IslandFlag>> conflictingFlags = new LazyReference<List<IslandFlag>>() {
        @Override
        protected List<IslandFlag> create() {
            if (conflictingFlagsNames == null || conflictingFlagsNames.isEmpty())
                return Collections.emptyList();
            Set<IslandFlag> conflictingFlags = new LinkedHashSet<>();
            conflictingFlagsNames.forEach(islandFlagName -> {
                try {
                    conflictingFlags.add(IslandFlag.getByName(islandFlagName.toUpperCase(Locale.ENGLISH)));
                } catch (NullPointerException ignored) {
                }
            });
            return conflictingFlags.isEmpty() ? Collections.emptyList() : new LinkedList<>(conflictingFlags);
        }
    };

    public IslandFlagConfigImpl(@Nullable Consumer<Island> onDisableCallback,
                                @Nullable Consumer<Island> onEnableCallback,
                                @Nullable List<String> conflictingFlagsNames) {
        this.onDisableCallback = onDisableCallback;
        this.onEnableCallback = onEnableCallback;
        this.conflictingFlagsNames = conflictingFlagsNames;
    }

    @Override
    public void onDisable(Island island) {
        if (this.onDisableCallback != null)
            this.onDisableCallback.accept(island);
    }

    @Override
    public void onEnable(Island island) {
        if (this.onEnableCallback != null)
            this.onEnableCallback.accept(island);
    }

    @Override
    public boolean hasConflictingFlags() {
        return this.conflictingFlagsNames != null && !this.conflictingFlagsNames.isEmpty();
    }

    @Override
    public void forEachConflicting(Consumer<IslandFlag> consumer) {
        this.conflictingFlags.get().forEach(consumer);
    }

}
