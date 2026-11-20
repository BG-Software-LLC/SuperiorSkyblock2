package com.bgsoftware.superiorskyblock.nms.v1_16_R3.spawners;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;

import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;

public class TileEntityMobSpawnerNotifier extends TileEntityMobSpawner {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT;

    static {
        // In Airplane, the modifiers of the field is `public final` instead of `private final`
        // as it is in Paper and Spigot. We want to make sure we support all jars.
        ReflectField<MobSpawnerAbstract> mobSpawnerAbstract = new ReflectField<>(
                TileEntityMobSpawner.class, MobSpawnerAbstract.class, Modifier.PRIVATE | Modifier.FINAL, 1);

        if (mobSpawnerAbstract.isValid()) {
            MOB_SPAWNER_ABSTRACT = mobSpawnerAbstract;
        } else {
            MOB_SPAWNER_ABSTRACT = new ReflectField<>(TileEntityMobSpawner.class, MobSpawnerAbstract.class,
                    Modifier.PUBLIC | Modifier.FINAL, 1);
        }

        MOB_SPAWNER_ABSTRACT.removeFinal();
    }

    private final TileEntityMobSpawner tileEntityMobSpawner;
    private final WeakReference<Island> island;
    private final NMSWorld.DelayChangeCallback delayChangeCallback;

    public TileEntityMobSpawnerNotifier(TileEntityMobSpawner tileEntityMobSpawner, Island island, NMSWorld.DelayChangeCallback delayChangeCallback) {
        this.tileEntityMobSpawner = tileEntityMobSpawner;
        this.island = new WeakReference<>(island);
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
        Island island = this.island.get();
        if (island != null) {
            MobSpawnerAbstract mobSpawnerAbstract = tileEntityMobSpawner.getSpawner();
            mobSpawnerAbstract.spawnDelay = delayChangeCallback.apply(island, mobSpawnerAbstract.spawnDelay);
        }
    }

}
