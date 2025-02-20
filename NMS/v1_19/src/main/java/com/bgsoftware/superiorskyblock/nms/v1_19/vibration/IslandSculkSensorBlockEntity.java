package com.bgsoftware.superiorskyblock.nms.v1_19.vibration;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;

import javax.annotation.Nullable;

public class IslandSculkSensorBlockEntity extends SculkSensorBlockEntity {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Island island;

    public IslandSculkSensorBlockEntity(Island island, SculkSensorBlockEntity sculkSensorBlockEntity) {
        super(sculkSensorBlockEntity.getBlockPos(), sculkSensorBlockEntity.getBlockState());
        this.island = island;
    }

    @Override
    public boolean shouldListen(ServerLevel world, GameEventListener listener, BlockPos pos, GameEvent event, @Nullable GameEvent.Context context) {
        if (!super.shouldListen(world, listener, pos, event, context))
            return false;

        if (context.sourceEntity() instanceof Player player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getBukkitEntity());
            return island.hasPermission(superiorPlayer, IslandPrivileges.SCULK_SENSOR);
        }

        return true;
    }
}
