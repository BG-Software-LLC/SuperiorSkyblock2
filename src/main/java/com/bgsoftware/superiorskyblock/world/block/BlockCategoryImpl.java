package com.bgsoftware.superiorskyblock.world.block;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.block.BlockCategory;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;

public class BlockCategoryImpl implements BlockCategory {

    private final String name;
    private final KeySet blocks;
    @Nullable
    private final IslandPrivilege placePrivilege;
    @Nullable
    private final IslandPrivilege breakPrivilege;
    @Nullable
    private final IslandPrivilege interactPrivilege;

    public BlockCategoryImpl(String name, KeySet blocks, @Nullable IslandPrivilege placePrivilege,
                      @Nullable IslandPrivilege breakPrivilege, @Nullable IslandPrivilege interactPrivilege) {
        this.name = name;
        this.blocks = blocks;
        this.placePrivilege = placePrivilege;
        this.breakPrivilege = breakPrivilege;
        this.interactPrivilege = interactPrivilege;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public KeySet getBlocks() {
        return this.blocks;
    }

    @Override
    public IslandPrivilege getPlacePrivilege() {
        return this.placePrivilege;
    }

    @Override
    public IslandPrivilege getBreakPrivilege() {
        return this.breakPrivilege;
    }

    @Override
    public IslandPrivilege getInteractPrivilege() {
        return this.interactPrivilege;
    }

}
