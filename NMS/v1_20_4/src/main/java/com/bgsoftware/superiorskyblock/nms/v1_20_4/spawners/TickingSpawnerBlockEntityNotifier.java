package com.bgsoftware.superiorskyblock.nms.v1_20_4.spawners;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.function.IntFunction;

public class TickingSpawnerBlockEntityNotifier implements TickingBlockEntity {

    private final SpawnerBlockEntity spawnerBlockEntity;
    private final TickingBlockEntity tickingBlockEntity;
    private final IntFunction<Integer> delayChangeCallback;

    public TickingSpawnerBlockEntityNotifier(SpawnerBlockEntity spawnerBlockEntity, TickingBlockEntity tickingBlockEntity,
                                             IntFunction<Integer> delayChangeCallback) {
        this.spawnerBlockEntity = spawnerBlockEntity;
        this.tickingBlockEntity = tickingBlockEntity;
        this.delayChangeCallback = delayChangeCallback;
        updateDelay();
    }

    @Override
    public void tick() {
        BaseSpawner baseSpawner = this.spawnerBlockEntity.getSpawner();
        int startDelay = baseSpawner.spawnDelay;
        try {
            tickingBlockEntity.tick();
        } finally {
            int newDelay = baseSpawner.spawnDelay;
            if (newDelay > startDelay)
                updateDelay();
        }
    }

    @Override
    public boolean isRemoved() {
        return tickingBlockEntity.isRemoved();
    }

    @Override
    public BlockPos getPos() {
        return tickingBlockEntity.getPos();
    }

    @Override
    public String getType() {
        return tickingBlockEntity.getType();
    }

    public void updateDelay() {
        BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
        baseSpawner.spawnDelay = delayChangeCallback.apply(baseSpawner.spawnDelay);
    }

}
