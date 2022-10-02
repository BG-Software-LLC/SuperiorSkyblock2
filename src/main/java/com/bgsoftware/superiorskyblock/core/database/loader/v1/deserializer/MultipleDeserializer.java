package com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class MultipleDeserializer implements IDeserializer {

    private final List<IDeserializer> deserializers;

    public MultipleDeserializer(IDeserializer... deserializers) {
        this.deserializers = Arrays.asList(deserializers);
    }

    private <T> T runDeserializers(Function<IDeserializer, T> function) {
        for (IDeserializer deserializer : deserializers) {
            try {
                return function.apply(deserializer);
            } catch (Exception ignored) {
            }
        }

        throw new RuntimeException("No valid deserializer found.");
    }

    @Override
    public Map<String, Integer> deserializeMissions(String missions) {
        return runDeserializers(deserializer -> deserializer.deserializeMissions(missions));
    }

    @Override
    public String[] deserializeHomes(String locationParam) {
        return runDeserializers(deserializer -> deserializer.deserializeHomes(locationParam));
    }

    @Override
    public List<PlayerAttributes> deserializePlayers(String players) {
        return runDeserializers(deserializer -> deserializer.deserializePlayers(players));
    }

    @Override
    public Map<UUID, PlayerPrivilegeNode> deserializePlayerPerms(String permissionNodes) {
        return runDeserializers(deserializer -> deserializer.deserializePlayerPerms(permissionNodes));
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes) {
        return runDeserializers(deserializer -> deserializer.deserializeRolePerms(permissionNodes));
    }

    @Override
    public Map<String, Integer> deserializeUpgrades(String upgrades) {
        return runDeserializers(deserializer -> deserializer.deserializeUpgrades(upgrades));
    }

    @Override
    public List<IslandWarpAttributes> deserializeWarps(String islandWarps) {
        return runDeserializers(deserializer -> deserializer.deserializeWarps(islandWarps));
    }

    @Override
    public KeyMap<Integer> deserializeBlockLimits(String blocks) {
        return runDeserializers(deserializer -> deserializer.deserializeBlockLimits(blocks));
    }

    @Override
    public Map<UUID, Rating> deserializeRatings(String ratings) {
        return runDeserializers(deserializer -> deserializer.deserializeRatings(ratings));
    }

    @Override
    public Map<IslandFlag, Byte> deserializeIslandFlags(String settings) {
        return runDeserializers(deserializer -> deserializer.deserializeIslandFlags(settings));
    }

    @Override
    public KeyMap<Integer>[] deserializeGenerators(String generator) {
        return runDeserializers(deserializer -> deserializer.deserializeGenerators(generator));
    }

    @Override
    public List<Pair<UUID, Long>> deserializeVisitors(String visitors) {
        return runDeserializers(deserializer -> deserializer.deserializeVisitors(visitors));
    }

    @Override
    public KeyMap<Integer> deserializeEntityLimits(String entities) {
        return runDeserializers(deserializer -> deserializer.deserializeEntityLimits(entities));
    }

    @Override
    public Map<PotionEffectType, Integer> deserializeEffects(String effects) {
        return runDeserializers(deserializer -> deserializer.deserializeEffects(effects));
    }

    @Override
    public List<IslandChestAttributes> deserializeIslandChests(String islandChest) {
        return runDeserializers(deserializer -> deserializer.deserializeIslandChests(islandChest));
    }

    @Override
    public Map<PlayerRole, Integer> deserializeRoleLimits(String roles) {
        return runDeserializers(deserializer -> deserializer.deserializeRoleLimits(roles));
    }

    @Override
    public List<WarpCategoryAttributes> deserializeWarpCategories(String categories) {
        return runDeserializers(deserializer -> deserializer.deserializeWarpCategories(categories));
    }

    @Override
    public String deserializeBlockCounts(String blockCountsParam) {
        return runDeserializers(deserializer -> deserializer.deserializeBlockCounts(blockCountsParam));
    }

    @Override
    public String deserializeDirtyChunks(String dirtyChunksParam) {
        return runDeserializers(deserializer -> deserializer.deserializeDirtyChunks(dirtyChunksParam));
    }

}
