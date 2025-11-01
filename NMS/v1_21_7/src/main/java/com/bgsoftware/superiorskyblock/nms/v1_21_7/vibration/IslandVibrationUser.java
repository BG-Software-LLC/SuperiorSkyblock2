package com.bgsoftware.superiorskyblock.nms.v1_21_7.vibration;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

import javax.annotation.Nullable;

public class IslandVibrationUser implements VibrationSystem.User {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Island island;
    private final VibrationSystem.User original;

    public IslandVibrationUser(Island island, SculkSensorBlockEntity sculkSensorBlockEntity) {
        this.island = island;
        this.original = sculkSensorBlockEntity.getVibrationUser();
    }

    @Override
    public int getListenerRadius() {
        return this.original.getListenerRadius();
    }

    @Override
    public PositionSource getPositionSource() {
        return this.original.getPositionSource();
    }

    @Override
    public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, GameEvent.Context context) {
        if (!this.original.canReceiveVibration(serverLevel, blockPos, holder, context))
            return false;

        if (context.sourceEntity() instanceof Player player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getBukkitEntity());
            return island.hasPermission(superiorPlayer, IslandPrivileges.SCULK_SENSOR);
        }

        return true;
    }

    @Override
    public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity1, float v) {
        this.original.onReceiveVibration(serverLevel, blockPos, holder, entity, entity1, v);
    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return this.original.getListenableEvents();
    }

    @Override
    public boolean canTriggerAvoidVibration() {
        return this.original.canTriggerAvoidVibration();
    }

    @Override
    public boolean requiresAdjacentChunksToBeTicking() {
        return this.original.requiresAdjacentChunksToBeTicking();
    }

    @Override
    public int calculateTravelTimeInTicks(float distance) {
        return this.original.calculateTravelTimeInTicks(distance);
    }

    @Override
    public boolean isValidVibration(Holder<GameEvent> gameEvent, GameEvent.Context context) {
        return this.original.isValidVibration(gameEvent, context);
    }

    @Override
    public void onDataChanged() {
        this.original.onDataChanged();
    }

}
