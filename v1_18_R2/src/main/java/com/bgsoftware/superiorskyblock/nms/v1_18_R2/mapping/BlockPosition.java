package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.phys.Vec3D;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class BlockPosition extends MappedObject<net.minecraft.core.BlockPosition> {

    public static final BlockPosition ZERO = new BlockPosition(0, 100, 0);

    public BlockPosition(int x, int y, int z) {
        this(new net.minecraft.core.BlockPosition(x, y, z));
    }

    public BlockPosition(double x, double y, double z) {
        this(new net.minecraft.core.BlockPosition(x, y, z));
    }

    public BlockPosition(net.minecraft.core.BaseBlockPosition handle) {
        this(new net.minecraft.core.BlockPosition(handle));
    }

    public BlockPosition(net.minecraft.core.BlockPosition handle) {
        super(handle);
    }

    @Nullable
    public static BlockPosition ofNullable(net.minecraft.core.BlockPosition handle) {
        return handle == null ? null : new BlockPosition(handle);
    }

    public int getX() {
        return handle.u();
    }

    public int getY() {
        return handle.v();
    }

    public int getZ() {
        return handle.w();
    }

    public long asLong() {
        return handle.a();
    }

    public BlockPosition down() {
        return new BlockPosition(handle.a(EnumDirection.a));
    }

    public BlockPosition up(int i) {
        return new BlockPosition(handle.a(EnumDirection.b, i));
    }

    public double distSqr(Vec3D vec3D) {
        return handle.a(vec3D);
    }

    public BlockPosition offset(int a, int b, int c) {
        return new BlockPosition(handle.c(a, b, c));
    }

    public boolean closerThan(Vec3D position, double maxDistance) {
        return handle.a(position, maxDistance);
    }

    public BlockPosition copy() {
        return new BlockPosition(this.getX(), this.getY(), this.getZ());
    }

    public static Iterable<BlockPosition> allBlocksBetween(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Iterator<net.minecraft.core.BlockPosition> iterable = net.minecraft.core.BlockPosition.b(minX, minY, minZ, maxX, maxY, maxZ).iterator();
        return () -> {
            return new AbstractIterator<>() {
                private BlockPosition iterablePosition = null;

                @Override
                protected BlockPosition computeNext() {
                    if (!iterable.hasNext())
                        return this.endOfData();

                    if (iterablePosition == null) {
                        iterablePosition = new BlockPosition(iterable.next());
                    } else {
                        iterablePosition.setHandle(iterable.next());
                    }

                    return iterablePosition;
                }
            };
        };
    }

}
