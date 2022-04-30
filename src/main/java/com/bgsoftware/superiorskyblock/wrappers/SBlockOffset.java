package com.bgsoftware.superiorskyblock.wrappers;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.Objects;

public final class SBlockOffset implements BlockOffset {

    public static final SBlockOffset ZERO = new SBlockOffset(0, 0, 0);

    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    public static BlockOffset fromOffsets(int offsetX, int offsetY, int offsetZ) {
        return offsetX == 0 && offsetY == 0 && offsetZ == 0 ? ZERO : new SBlockOffset(offsetX, offsetY, offsetZ);
    }

    private SBlockOffset(int offsetX, int offsetY, int offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    public int getOffsetX() {
        return this.offsetX;
    }

    @Override
    public int getOffsetY() {
        return this.offsetY;
    }

    @Override
    public int getOffsetZ() {
        return this.offsetZ;
    }

    @Override
    public BlockOffset negate() {
        return SBlockOffset.fromOffsets(-offsetX, -offsetY, -offsetZ);
    }

    @Override
    public Location applyToLocation(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return location.clone().add(this.offsetX, this.offsetY, this.offsetZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SBlockOffset that = (SBlockOffset) o;
        return offsetX == that.offsetX && offsetY == that.offsetY && offsetZ == that.offsetZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offsetX, offsetY, offsetZ);
    }

}
