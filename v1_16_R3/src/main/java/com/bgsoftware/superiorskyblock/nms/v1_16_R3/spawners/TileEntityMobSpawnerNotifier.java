package com.bgsoftware.superiorskyblock.nms.v1_16_R3.spawners;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;

import java.lang.reflect.Modifier;
import java.util.function.IntFunction;

public class TileEntityMobSpawnerNotifier extends TileEntityMobSpawner {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private final TileEntityMobSpawner tileEntityMobSpawner;
    private final IntFunction<Integer> delayChangeCallback;

    public TileEntityMobSpawnerNotifier(TileEntityMobSpawner tileEntityMobSpawner, IntFunction<Integer> delayChangeCallback) {
        this.tileEntityMobSpawner = tileEntityMobSpawner;
        this.delayChangeCallback = delayChangeCallback;
        this.position = tileEntityMobSpawner.getPosition();
        this.world = tileEntityMobSpawner.getWorld();
        MOB_SPAWNER_ABSTRACT.set(this, tileEntityMobSpawner.getSpawner());
        updateDelay();
    }

    @Override
    public void tick() {
        MobSpawnerAbstract mobSpawnerAbstract = tileEntityMobSpawner.getSpawner();
        int startDelay = mobSpawnerAbstract.spawnDelay;
        try {
            tileEntityMobSpawner.tick();
        } finally {
            int newDelay = mobSpawnerAbstract.spawnDelay;
            if (newDelay > startDelay)
                updateDelay();
        }
    }

    public void updateDelay() {
        MobSpawnerAbstract mobSpawnerAbstract = tileEntityMobSpawner.getSpawner();
        mobSpawnerAbstract.spawnDelay = delayChangeCallback.apply(mobSpawnerAbstract.spawnDelay);
    }

}
