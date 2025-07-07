package com.bgsoftware.superiorskyblock.nms.v1_21_7.trial;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class IslandPlayerDetector implements PlayerDetector {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Island island;
    private final PlayerDetector original;
    private final IslandPrivilege requiredPrivilege;

    public static IslandPlayerDetector trialVaultPlayerDetector(Island island, PlayerDetector original) {
        return new IslandPlayerDetector(island, original, IslandPrivileges.USE);
    }

    public static IslandPlayerDetector trialSpawnerPlayerDetector(Island island, PlayerDetector original) {
        return new IslandPlayerDetector(island, original, IslandPrivileges.MONSTER_DAMAGE);
    }

    private IslandPlayerDetector(Island island, PlayerDetector original, IslandPrivilege requiredPrivilege) {
        this.island = island;
        this.original = original;
        this.requiredPrivilege = requiredPrivilege;
    }

    @Override
    public List<UUID> detect(ServerLevel serverLevel, EntitySelector entitySelector, BlockPos blockPos, double maxDistance, boolean requireLineOfSight) {
        List<UUID> players = this.original.detect(serverLevel, entitySelector, blockPos, maxDistance, requireLineOfSight);
        if (!players.isEmpty()) {
            players = new LinkedList<>(players);
            players.removeIf(uuid -> {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(uuid);
                return !island.hasPermission(superiorPlayer, this.requiredPrivilege);
            });
            players = Collections.unmodifiableList(players);
        }
        return players;
    }

}
