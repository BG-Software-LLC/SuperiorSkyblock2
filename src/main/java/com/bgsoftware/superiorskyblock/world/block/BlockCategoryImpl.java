package com.bgsoftware.superiorskyblock.world.block;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.block.BlockCategory;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;

import java.util.Objects;

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
        this.blocks = KeySets.unmodifiableKeySet(blocks);
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

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BlockCategoryImpl blockCategory = (BlockCategoryImpl) obj;

        return Objects.equals(this.name, blockCategory.name);
    }

}
