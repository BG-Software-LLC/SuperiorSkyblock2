package com.bgsoftware.superiorskyblock.island.flag;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class IslandFlagConfigBuilderImpl implements IslandFlag.Config.Builder {

    @Nullable
    private Consumer<Island> onDisableCallback;
    @Nullable
    private Consumer<Island> onEnableCallback;
    @Nullable
    private List<String> conflictingFlagsNames;

    @Override
    public IslandFlagConfigBuilderImpl setDisableCallback(Consumer<Island> callback) {
        this.onDisableCallback = callback;
        return this;
    }

    @Override
    public IslandFlagConfigBuilderImpl setEnableCallback(Consumer<Island> callback) {
        this.onEnableCallback = callback;
        return this;
    }

    @Override
    public IslandFlagConfigBuilderImpl setConflictingIslandFlags(String... names) {
        if (names.length != 0) {
            if (this.conflictingFlagsNames == null)
                this.conflictingFlagsNames = new LinkedList<>();
            Collections.addAll(this.conflictingFlagsNames, names);
        }

        return this;
    }

    @Override
    public IslandFlag.Config build() {
        return new IslandFlagConfigImpl(this.onDisableCallback, this.onEnableCallback, this.conflictingFlagsNames);
    }
}
