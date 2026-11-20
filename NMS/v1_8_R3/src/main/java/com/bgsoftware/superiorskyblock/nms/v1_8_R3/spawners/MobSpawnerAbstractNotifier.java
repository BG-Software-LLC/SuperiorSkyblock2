package com.bgsoftware.superiorskyblock.nms.v1_8_R3.spawners;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

import java.lang.ref.WeakReference;

public class MobSpawnerAbstractNotifier extends MobSpawnerAbstract {

    private final MobSpawnerAbstract mobSpawnerAbstract;
    private final WeakReference<Island> island;
    private final NMSWorld.DelayChangeCallback delayChangeCallback;

    public MobSpawnerAbstractNotifier(MobSpawnerAbstract mobSpawnerAbstract, Island island, NMSWorld.DelayChangeCallback delayChangeCallback) {
        this.mobSpawnerAbstract = mobSpawnerAbstract;
        this.island = new WeakReference<>(island);
        this.delayChangeCallback = delayChangeCallback;

        // Copy data from original spawner to this
        NBTTagCompound spawnerCompound = new NBTTagCompound();
        mobSpawnerAbstract.b(spawnerCompound);
        this.a(spawnerCompound);
    }

    @Nullable
    @Override
    public String getMobName() {
        return mobSpawnerAbstract.getMobName();
    }

    @Override
    public void setMobName(String name) {
        mobSpawnerAbstract.setMobName(name);
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
    public void b(NBTTagCompound nbttagcompound) {
        mobSpawnerAbstract.b(nbttagcompound);
    }

    @Override
    public boolean b(int i) {
        return mobSpawnerAbstract.b(i);
    }

    @Override
    public void a(a mobspawnerabstract_a) {
        mobSpawnerAbstract.a(mobspawnerabstract_a);
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
        Island island = this.island.get();
        if (island != null)
            mobSpawnerAbstract.spawnDelay = delayChangeCallback.apply(island, mobSpawnerAbstract.spawnDelay);
    }

}
