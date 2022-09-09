package com.bgsoftware.superiorskyblock.nms.v1192.spawners;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

public class BaseSpawnerNotifier extends BaseSpawner {

    private final BaseSpawner baseSpawner;
    private final IntFunction<Integer> delayChangeCallback;

    public BaseSpawnerNotifier(BaseSpawner baseSpawner, IntFunction<Integer> delayChangeCallback) {
        this.baseSpawner = baseSpawner;
        this.delayChangeCallback = delayChangeCallback;

        // Copy data from original spawner to this
        this.spawnPotentials = baseSpawner.spawnPotentials;
        this.nextSpawnData = baseSpawner.nextSpawnData;
        this.minSpawnDelay = baseSpawner.minSpawnDelay;
        this.maxSpawnDelay = baseSpawner.maxSpawnDelay;
        this.spawnCount = baseSpawner.spawnCount;
        this.maxNearbyEntities = baseSpawner.maxNearbyEntities;
        this.requiredPlayerRange = baseSpawner.requiredPlayerRange;
        this.spawnRange = baseSpawner.spawnRange;
    }

    @Override
    public void setEntityId(EntityType<?> type) {
        baseSpawner.setEntityId(type);
    }

    public boolean isNearPlayer(Level level, BlockPos blockPos) {
        return baseSpawner.isNearPlayer(level, blockPos);
    }

    @Override
    public void clientTick(Level level, BlockPos blockPos) {
        baseSpawner.clientTick(level, blockPos);
    }

    @Override
    public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
        int startDelay = baseSpawner.spawnDelay;
        try {
            baseSpawner.serverTick(serverLevel, blockPos);
        } finally {
            int newDelay = baseSpawner.spawnDelay;
            if (newDelay > startDelay)
                updateDelay();
        }
    }

    public void delay(Level level, BlockPos blockPos) {
        baseSpawner.delay(level, blockPos);
    }

    @Override
    public void load(@Nullable Level level, BlockPos blockPos, CompoundTag compoundTag) {
        baseSpawner.load(level, blockPos, compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return baseSpawner.save(compoundTag);
    }

    @Nullable
    @Override
    public Entity getOrCreateDisplayEntity(Level level) {
        return baseSpawner.getOrCreateDisplayEntity(level);
    }

    @Override
    public boolean onEventTriggered(Level level, int status) {
        return baseSpawner.onEventTriggered(level, status);
    }

    @Override
    public void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
        baseSpawner.setNextSpawnData(level, blockPos, spawnData);
    }

    @Override
    public void broadcastEvent(Level level, BlockPos blockPos, int i) {
        baseSpawner.broadcastEvent(level, blockPos, i);
    }

    @Override
    public double getSpin() {
        return baseSpawner.getSpin();
    }

    @Override
    public double getoSpin() {
        return baseSpawner.getoSpin();
    }

    public void updateDelay() {
        baseSpawner.spawnDelay = delayChangeCallback.apply(baseSpawner.spawnDelay);
    }

}
