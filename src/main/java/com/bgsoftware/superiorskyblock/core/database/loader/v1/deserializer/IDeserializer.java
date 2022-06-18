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

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IDeserializer {

    Map<String, Integer> deserializeMissions(String missions);

    String[] deserializeHomes(String locationParam);

    List<PlayerAttributes> deserializePlayers(String players);

    Map<UUID, PlayerPrivilegeNode> deserializePlayerPerms(String permissionNodes);

    Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes);

    Map<String, Integer> deserializeUpgrades(String upgrades);

    List<IslandWarpAttributes> deserializeWarps(String islandWarps);

    KeyMap<Integer> deserializeBlockLimits(String blocks);

    Map<UUID, Rating> deserializeRatings(String ratings);

    Map<IslandFlag, Byte> deserializeIslandFlags(String settings);

    KeyMap<Integer>[] deserializeGenerators(String generator);

    List<Pair<UUID, Long>> deserializeVisitors(String visitors);

    KeyMap<Integer> deserializeEntityLimits(String entities);

    Map<PotionEffectType, Integer> deserializeEffects(String effects);

    List<IslandChestAttributes> deserializeIslandChests(String islandChest);

    Map<PlayerRole, Integer> deserializeRoleLimits(String roles);

    List<WarpCategoryAttributes> deserializeWarpCategories(String categories);

    String deserializeBlockCounts(String blockCountsParam);

    String deserializeDirtyChunks(String dirtyChunksParam);

}
