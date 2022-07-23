package com.bgsoftware.superiorskyblock.nms.v1_16_R3.spawners;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.MobSpawnerData;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.World;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

public class MobSpawnerAbstractNotifier extends MobSpawnerAbstract {

    private final MobSpawnerAbstract mobSpawnerAbstract;
    private final IntFunction<Integer> delayChangeCallback;

    public MobSpawnerAbstractNotifier(MobSpawnerAbstract mobSpawnerAbstract, IntFunction<Integer> delayChangeCallback) {
        this.mobSpawnerAbstract = mobSpawnerAbstract;
        this.delayChangeCallback = delayChangeCallback;
    }

    @Nullable
    @Override
    public MinecraftKey getMobName() {
        return mobSpawnerAbstract.getMobName();
    }

    @Override
    public void setMobName(EntityTypes<?> type) {
        mobSpawnerAbstract.setMobName(type);
    }

    public boolean isActivated() {
        return mobSpawnerAbstract.isActivated();
    }

    @Override
    public void c() {
        int startDelay = mobSpawnerAbstract.spawnDelay;
        try {
            mobSpawnerAbstract.c();
        } finally {
            int newDelay = mobSpawnerAbstract.spawnDelay;
            if (newDelay > startDelay)
                updateDelay();
        }
    }

    public void resetTimer() {
        mobSpawnerAbstract.resetTimer();
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        mobSpawnerAbstract.a(nbttagcompound);
    }

    @Override
    public NBTTagCompound b(NBTTagCompound nbttagcompound) {
        return mobSpawnerAbstract.b(nbttagcompound);
    }

    @Override
    public boolean b(int i) {
        return mobSpawnerAbstract.b(i);
    }

    @Override
    public void setSpawnData(MobSpawnerData mobspawnerdata) {
        mobSpawnerAbstract.setSpawnData(mobspawnerdata);
    }

    @Override
    public void a(int i) {
        mobSpawnerAbstract.a(i);
    }

    @Override
    public World a() {
        return mobSpawnerAbstract.a();
    }

    @Override
    public BlockPosition b() {
        return mobSpawnerAbstract.b();
    }

    public void updateDelay() {
        mobSpawnerAbstract.spawnDelay = delayChangeCallback.apply(mobSpawnerAbstract.spawnDelay);
    }

}
