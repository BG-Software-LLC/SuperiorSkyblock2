package com.bgsoftware.superiorskyblock.nms.v1_21_5.dragon;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class IslandEntityEnderDragon extends com.bgsoftware.superiorskyblock.nms.v1_21_5.dragon.AbstractIslandEntityEnderDragon {

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
                                        EntitySpawnReason spawnReason,
                                        @Nullable SpawnGroupData entityData) {
        if (this.islandBlockPos == null)
            finalizeIslandEnderDragon();

        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        // loadData
        super.readAdditionalSaveData(compoundTag);
        finalizeIslandEnderDragon();
    }

}
