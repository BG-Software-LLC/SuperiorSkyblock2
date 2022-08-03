package com.bgsoftware.superiorskyblock.nms.v1_19_R1.spawners;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
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

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "setEntityId",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a(EntityTypes<?> type) {
        mobSpawnerAbstract.a(type);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "isNearPlayer",
            type = Remap.Type.METHOD,
            remappedName = "b")
    @Override
    public boolean b(World world, BlockPosition pos) {
        return mobSpawnerAbstract.b(world, pos);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "clientTick",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a(World world, BlockPosition pos) {
        mobSpawnerAbstract.a(world, pos);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "serverTick",
            type = Remap.Type.METHOD,
            remappedName = "a")
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

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "delay",
            type = Remap.Type.METHOD,
            remappedName = "c")
    @Override
    public void c(World world, BlockPosition pos) {
        mobSpawnerAbstract.c(world, pos);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "load",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a(@Nullable World world, BlockPosition pos, NBTTagCompound nbt) {
        mobSpawnerAbstract.a(world, pos, nbt);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "save",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public NBTTagCompound a(NBTTagCompound nbt) {
        return mobSpawnerAbstract.a(nbt);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "getOrCreateDisplayEntity",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Nullable
    @Override
    public Entity a(World world) {
        return mobSpawnerAbstract.a(world);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "onEventTriggered",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public boolean a(World world, int status) {
        return mobSpawnerAbstract.a(world, status);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "setNextSpawnData",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a(@Nullable World world, BlockPosition pos, MobSpawnerData spawnEntry) {
        mobSpawnerAbstract.a(world, pos, spawnEntry);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "broadcastEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a(World world, BlockPosition blockPosition, int i) {
        mobSpawnerAbstract.a(world, blockPosition, i);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "getSpin",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public double a() {
        return mobSpawnerAbstract.a();
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "getoSpin",
            type = Remap.Type.METHOD,
            remappedName = "b")
    @Override
    public double b() {
        return mobSpawnerAbstract.b();
    }

    public void updateDelay() {
        mobSpawnerAbstract.c = delayChangeCallback.apply(mobSpawnerAbstract.c);
    }

}
