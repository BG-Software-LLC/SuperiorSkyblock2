package com.bgsoftware.superiorskyblock.nms.v1_18_R1.spawners;

import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

public class MobSpawnerAbstractNotifier extends MobSpawnerAbstract {

    private final MobSpawnerAbstract mobSpawnerAbstract;
    private final IntFunction<Integer> delayChangeCallback;

    public MobSpawnerAbstractNotifier(MobSpawnerAbstract mobSpawnerAbstract, IntFunction<Integer> delayChangeCallback) {
        this.mobSpawnerAbstract = mobSpawnerAbstract;
        this.delayChangeCallback = delayChangeCallback;
    }

    @Override
    public void a(EntityTypes<?> type) {
        mobSpawnerAbstract.a(type);
    }

    @Override
    public boolean b(World world, BlockPosition pos) {
        return mobSpawnerAbstract.b(world, pos);
    }

    @Override
    public void a(World world, BlockPosition pos) {
        mobSpawnerAbstract.a(world, pos);
    }

    @Override
    public void a(WorldServer world, BlockPosition pos) {
        int startDelay = mobSpawnerAbstract.c;
        try {
            mobSpawnerAbstract.a(world, pos);
        } finally {
            int newDelay = mobSpawnerAbstract.c;
            if (newDelay > startDelay)
                updateDelay();
        }
    }

    @Override
    public void c(World world, BlockPosition pos) {
        mobSpawnerAbstract.c(world, pos);
    }

    @Override
    public void a(@Nullable World world, BlockPosition pos, NBTTagCompound nbt) {
        mobSpawnerAbstract.a(world, pos, nbt);
    }

    @Override
    public NBTTagCompound a(NBTTagCompound nbt) {
        return mobSpawnerAbstract.a(nbt);
    }

    @Nullable
    @Override
    public Entity a(World world) {
        return mobSpawnerAbstract.a(world);
    }

    @Override
    public boolean a(World world, int status) {
        return mobSpawnerAbstract.a(world, status);
    }

    @Override
    public void a(@Nullable World world, BlockPosition pos, MobSpawnerData spawnEntry) {
        mobSpawnerAbstract.a(world, pos, spawnEntry);
    }

    @Override
    public void a(World world, BlockPosition blockPosition, int i) {
        mobSpawnerAbstract.a(world, blockPosition, i);
    }

    @Override
    public double a() {
        return mobSpawnerAbstract.a();
    }

    @Override
    public double b() {
        return mobSpawnerAbstract.b();
    }

    public void updateDelay() {
        mobSpawnerAbstract.c = delayChangeCallback.apply(mobSpawnerAbstract.c);
    }

}
