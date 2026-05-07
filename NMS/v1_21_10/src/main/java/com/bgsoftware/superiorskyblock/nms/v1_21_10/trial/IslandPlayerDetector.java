package com.bgsoftware.superiorskyblock.nms.v1_21_10.trial;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.world.entity.BuiltinEntityCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class IslandPlayerDetector implements PlayerDetector {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Island island;
    private final PlayerDetector original;
    private final Supplier<List<IslandPrivilege>> requiredPrivileges;

    public static IslandPlayerDetector trialVaultPlayerDetector(Island island, PlayerDetector original) {
        return new IslandPlayerDetector(island, original, () -> IslandPrivileges.VAULT_INTERACT_PRIVILEGES);
    }

    public static IslandPlayerDetector trialSpawnerPlayerDetector(Island island, PlayerDetector original) {
        return new IslandPlayerDetector(island, original, () ->
                List.of(BuiltinEntityCategory.MONSTER.getEntityCategory().getDamagePrivilege()));
    }

    private IslandPlayerDetector(Island island, PlayerDetector original, Supplier<List<IslandPrivilege>> requiredPrivileges) {
        this.island = island;
        this.original = original;
        this.requiredPrivileges = requiredPrivileges;
    }

    @Override
    public List<UUID> detect(ServerLevel serverLevel, EntitySelector entitySelector, BlockPos blockPos, double maxDistance, boolean requireLineOfSight) {
        List<UUID> players = this.original.detect(serverLevel, entitySelector, blockPos, maxDistance, requireLineOfSight);
        List<IslandPrivilege> privileges = this.requiredPrivileges.get();

        if (!privileges.isEmpty() && !players.isEmpty()) {
            players = new LinkedList<>(players);

            players.removeIf(uuid -> {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(uuid);

                for (IslandPrivilege privilege : privileges) {
                    if (privilege != null && !island.hasPermission(superiorPlayer, privilege)) {
                        return true;
                    }
                }
                return false;
            });

            players = Collections.unmodifiableList(players);
        }

        return players;
    }

}
