package com.bgsoftware.superiorskyblock.nms.v1_12_R1.spawners;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_12_R1.MobSpawnerData;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.World;

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
    public void setMobName(@Nullable MinecraftKey minecraftkey) {
        mobSpawnerAbstract.setMobName(minecraftkey);
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
    public void a(MobSpawnerData mobspawnerdata) {
        mobSpawnerAbstract.a(mobspawnerdata);
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
