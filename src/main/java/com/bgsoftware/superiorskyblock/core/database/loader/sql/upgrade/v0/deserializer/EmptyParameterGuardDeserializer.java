package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.deserializer;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.common.collections.Maps;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmptyParameterGuardDeserializer implements IDeserializer {

    private static final EmptyParameterGuardDeserializer INSTANCE = new EmptyParameterGuardDeserializer();

    public static EmptyParameterGuardDeserializer getInstance() {
        return INSTANCE;
    }

    private EmptyParameterGuardDeserializer() {

    }

    private static <T> T checkParam(String param, T emptyValue) {
        if (Text.isBlank(param))
            return emptyValue;

        throw new RuntimeException(); // Will continue to other deserializers
    }

    @Override
    public Map<String, Integer> deserializeMissions(String missions) {
        return checkParam(missions, Maps.emptyMap());
    }

    @Override
    public String[] deserializeHomes(String locationParam) {
        return checkParam(locationParam, new String[World.Environment.values().length]);
    }

    @Override
    public List<PlayerAttributes> deserializePlayers(String players) {
        return checkParam(players, Lists.emptyList());
    }

    @Override
    public Map<UUID, PlayerPrivilegeNode> deserializePlayerPerms(String permissionNodes) {
        return checkParam(permissionNodes, Maps.emptyMap());
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes) {
        return checkParam(permissionNodes, Maps.emptyMap());
    }

    @Override
    public Map<String, Integer> deserializeUpgrades(String upgrades) {
        return checkParam(upgrades, Maps.emptyMap());
    }

    @Override
    public List<IslandWarpAttributes> deserializeWarps(String islandWarps) {
        return checkParam(islandWarps, Lists.emptyList());
    }

    @Override
    public KeyMap<Integer> deserializeBlockLimits(String blocks) {
        return checkParam(blocks, KeyMaps.createEmptyMap());
    }

    @Override
    public Map<UUID, Rating> deserializeRatings(String ratings) {
        return checkParam(ratings, Maps.emptyMap());
    }

    @Override
    public Map<IslandFlag, Byte> deserializeIslandFlags(String settings) {
        return checkParam(settings, Maps.emptyMap());
    }

    @Override
    public KeyMap<Integer>[] deserializeGenerators(String generator) {
        return checkParam(generator, new KeyMap[World.Environment.values().length]);
    }

    @Override
    public List<Pair<UUID, Long>> deserializeVisitors(String visitors) {
        return checkParam(visitors, Lists.emptyList());
    }

    @Override
    public KeyMap<Integer> deserializeEntityLimits(String entities) {
        return checkParam(entities, KeyMaps.createEmptyMap());
    }

    @Override
    public Map<PotionEffectType, Integer> deserializeEffects(String effects) {
        return checkParam(effects, Maps.emptyMap());
    }

    @Override
    public List<IslandChestAttributes> deserializeIslandChests(String islandChest) {
        return checkParam(islandChest, Lists.emptyList());
    }

    @Override
    public Map<PlayerRole, Integer> deserializeRoleLimits(String roles) {
        return checkParam(roles, Maps.emptyMap());
    }

    @Override
    public List<WarpCategoryAttributes> deserializeWarpCategories(String categories) {
        return checkParam(categories, Lists.emptyList());
    }

    @Override
    public String deserializeBlockCounts(String blockCountsParam) {
        return checkParam(blockCountsParam, "[]");
    }

    @Override
    public String deserializeDirtyChunks(String dirtyChunksParam) {
        return checkParam(dirtyChunksParam, "[]");
    }

}
