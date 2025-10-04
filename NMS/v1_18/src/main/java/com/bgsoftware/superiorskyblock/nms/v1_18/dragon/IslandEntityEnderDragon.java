package com.bgsoftware.superiorskyblock.nms.v1_18.dragon;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class IslandEntityEnderDragon extends com.bgsoftware.superiorskyblock.nms.v1_18.dragon.AbstractIslandEntityEnderDragon {

    public IslandEntityEnderDragon(Level level, BlockPos islandBlockPos) {
        super(level, islandBlockPos);
    }

    public IslandEntityEnderDragon(Level level) {
        super(level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,
                                        DifficultyInstance difficulty,
                                        MobSpawnType spawnReason,
                                        @Nullable SpawnGroupData entityData,
                                        @Nullable CompoundTag entityNbt) {
        if (this.islandBlockPos == null)
            finalizeIslandEnderDragon();

        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        // loadData
        super.readAdditionalSaveData(compoundTag);
        finalizeIslandEnderDragon();
    }

}
