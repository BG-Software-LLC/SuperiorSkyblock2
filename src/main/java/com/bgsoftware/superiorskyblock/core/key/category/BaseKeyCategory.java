package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.core.Either;

import java.util.EnumMap;
import java.util.Objects;

public abstract class BaseKeyCategory implements KeyCategory {

    protected final String name;
    protected final EnumMap<Action, Either<IslandPrivilege, IslandFlag>> requirements = new EnumMap<>(Action.class);

    protected BaseKeyCategory(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public abstract boolean isListed(Key key);

    public void setRequirement(Action action, @Nullable IslandPrivilege islandPrivilege) {
        if (islandPrivilege != null) {
            this.requirements.put(action, Either.left(islandPrivilege));
        }
    }

    public void setRequirement(Action action, @Nullable IslandFlag islandFlag) {
        if (islandFlag != null) {
            this.requirements.put(action, Either.right(islandFlag));
        }
    }

    @Nullable
    @Override
    public IslandPrivilege getRequiredPrivilege(Action action) {
        Either<IslandPrivilege, IslandFlag> result = this.requirements.get(action);
        return result != null && result.isLeft() ? result.getLeft() : null;
    }

    @Nullable
    @Override
    public IslandFlag getRequiredFlag(Action action) {
        Either<IslandPrivilege, IslandFlag> result = this.requirements.get(action);
        return result != null && result.isRight() ? result.getRight() : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BaseKeyCategory other = (BaseKeyCategory) obj;

        return Objects.equals(this.name, other.name);
    }

}
