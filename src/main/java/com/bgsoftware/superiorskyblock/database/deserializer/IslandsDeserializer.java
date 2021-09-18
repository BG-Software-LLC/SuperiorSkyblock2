package com.bgsoftware.superiorskyblock.database.deserializer;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.*;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeValue;
import com.google.gson.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public final class IslandsDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    static final Gson gson = new GsonBuilder().create();

    private IslandsDeserializer() {

    }

    public static void deserializeMembers(Island island, SyncedObject<? extends Collection<SuperiorPlayer>> membersSetSync) {
        membersSetSync.write(membersSet -> loadObject(island, "islands_members", membersRow -> {
            UUID playerUUID = UUID.fromString((String) membersRow.get("player"));
            PlayerRole playerRole = SPlayerRole.fromId((int) membersRow.get("role"));

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID);
            superiorPlayer.setIslandLeader(island.getOwner());
            superiorPlayer.setPlayerRole(playerRole);

            membersSet.add(superiorPlayer);
        }));
    }

    public static void deserializeBanned(Island island, Collection<SuperiorPlayer> bansSet) {
        loadObject(island, "islands_bans", bansRow -> {
            UUID playerUUID = UUID.fromString((String) bansRow.get("player"));
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID);
            bansSet.add(superiorPlayer);
        });
    }

    public static void deserializeVisitors(Island island, SyncedObject<? extends Collection<Pair<SuperiorPlayer, Long>>> visitorsSetSync) {
        visitorsSetSync.write(visitorsSet -> loadObject(island, "islands_visitors", visitorsRow -> {
            UUID uuid = UUID.fromString((String) visitorsRow.get("player"));
            long visitTime = (long) visitorsRow.get("visit_time");
            visitorsSet.add(new Pair<>(plugin.getPlayers().getSuperiorPlayer(uuid), visitTime));
        }));
    }

    public static void deserializePlayerPermissions(Island island, Map<SuperiorPlayer, PlayerPermissionNode> playerPermissions){
        loadObject(island, "islands_player_permissions", playerPermissionRow -> {
            UUID playerUUID = UUID.fromString((String) playerPermissionRow.get("player"));
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID);

            try {
                IslandPrivilege islandPrivilege = IslandPrivilege.getByName((String) playerPermissionRow.get("permission"));
                playerPermissions.computeIfAbsent(superiorPlayer, s -> new PlayerPermissionNode(superiorPlayer, island))
                        .loadPrivilege(islandPrivilege, getAsByte(playerPermissionRow.get("status")));
            }catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading player permissions:");
                error.printStackTrace();
            }
        });
    }

    public static void deserializeRolePermissions(Island island, Map<IslandPrivilege, PlayerRole> rolePermissions){
        loadObject(island, "islands_role_permissions", rolePermissionRow -> {
            try {
                PlayerRole playerRole = SPlayerRole.fromId((int) rolePermissionRow.get("role"));
                IslandPrivilege islandPrivilege = IslandPrivilege.getByName((String) rolePermissionRow.get("permission"));
                rolePermissions.put(islandPrivilege, playerRole);
            }catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading role permissions:");
                error.printStackTrace();
            }
        });
    }

    public static void deserializeUpgrades(Island island, Map<String, Integer> upgradesMap) {
        loadObject(island, "islands_upgrades", upgradeRow -> {
            String upgradeName = (String) upgradeRow.get("upgrade");
            int level = (int) upgradeRow.get("level");
            upgradesMap.put(upgradeName, level);
        });
    }

    public static void deserializeWarps(Island island){
        loadObject(island, "islands_warps", islandWarpRow -> {
            String name = IslandUtils.getWarpName((String) islandWarpRow.get("name"));

            if(name.isEmpty())
                return;

            if(!IslandUtils.isWarpNameLengthValid(name))
                name = name.substring(0, IslandUtils.getMaxWarpNameLength());

            WarpCategory warpCategory = null;
            if (!((String) islandWarpRow.getOrDefault("category", "")).isEmpty())
                warpCategory = island.createWarpCategory((String) islandWarpRow.get("category"));

            Location location = FileUtils.toLocation((String) islandWarpRow.get("location"));
            boolean privateWarp = getAsByte(islandWarpRow.get("private")) == 1;

            IslandWarp islandWarp = island.createWarp(name, location, warpCategory);
            islandWarp.setPrivateFlag(privateWarp);

            if (!((String) islandWarpRow.getOrDefault("icon", "")).isEmpty())
                islandWarp.setIcon(ItemUtils.deserializeItem((String) islandWarpRow.get("icon")));
        });
    }

    public static void deserializeBlockCounts(String blocks, Island island) {
        if(blocks == null || blocks.isEmpty())
            return;

        JsonArray blockCountsArray = gson.fromJson(blocks, JsonArray.class);
        blockCountsArray.forEach(blockCountElement -> {
            JsonObject blockCountObject = blockCountElement.getAsJsonObject();
            Key blockKey = Key.of(blockCountObject.get("id").getAsString());
            BigInteger amount = new BigInteger(blockCountObject.get("amount").getAsString());
            island.handleBlockPlace(blockKey, amount, false, false);
        });
    }

    public static void deserializeBlockLimits(Island island, KeyMap<UpgradeValue<Integer>> blockLimits) {
        loadObject(island, "islands_block_limits", blockLimitsRow -> {
            Key blockKey = Key.of((String) blockLimitsRow.get("block"));
            int limit = (int) blockLimitsRow.get("limit");
            blockLimits.put(blockKey, new UpgradeValue<>(limit, i -> i < 0));
        });
    }

    public static void deserializeEntityLimits(Island island, KeyMap<UpgradeValue<Integer>> entityLimits) {
        loadObject(island, "islands_entity_limits", entityLimitsRow -> {
            Key entity = Key.of((String) entityLimitsRow.get("entity"));
            int limit = (int) entityLimitsRow.get("limit");
            entityLimits.put(entity, new UpgradeValue<>(limit, i -> i < 0));
        });
    }

    public static void deserializeRatings(Island island, Map<UUID, Rating> ratingsMap) {
        loadObject(island, "islands_ratings", ratingsRow -> {
            UUID uuid = UUID.fromString((String) ratingsRow.get("player"));
            Rating rating = Rating.valueOf((int) ratingsRow.get("rating"));
            ratingsMap.put(uuid, rating);
        });
    }

    public static void deserializeMissions(Island island, Map<Mission<?>, Integer> completedMissions) {
        loadObject(island, "islands_missions", missionsRow -> {
            String name = (String) missionsRow.get("name");
            int finishCount = (int) missionsRow.get("finish_count");

            Mission<?> mission = plugin.getMissions().getMission(name);

            if (mission != null)
                completedMissions.put(mission, finishCount);
        });
    }

    public static void deserializeIslandFlags(Island island, Map<IslandFlag, Byte> islandFlags) {
        loadObject(island, "islands_flags", islandFlagRow -> {
            try {
                IslandFlag islandFlag = IslandFlag.getByName((String) islandFlagRow.get("name"));
                byte status = getAsByte(islandFlagRow.get("status"));
                islandFlags.put(islandFlag, status);
            } catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading island flags:");
                error.printStackTrace();
            }
        });
    }

    public static void deserializeGenerators(Island island, KeyMap<UpgradeValue<Integer>>[] cobbleGenerator) {
        loadObject(island, "islands_generators", generatorsRow -> {
            try {
                int environment = World.Environment.valueOf((String) generatorsRow.get("environment")).ordinal();
                Key blockKey = Key.of((String) generatorsRow.get("block"));
                int rate = (int) generatorsRow.get("rate");
                (cobbleGenerator[environment] = new KeyMap<>()).put(blockKey, new UpgradeValue<>(rate, n -> n < 0));
            }catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading generators:");
                error.printStackTrace();
            }
        });
    }

    public static void deserializeIslandHomes(Island island, SyncedObject<Location[]> locationsSync) {
        locationsSync.write(locations -> loadObject(island, "islands_homes", teleportLocationRow -> {
            int environment = World.Environment.valueOf((String) teleportLocationRow.get("environment")).ordinal();
            locations[environment] = FileUtils.toLocation((String) teleportLocationRow.get("location"));
        }));
    }

    public static void deserializeVisitorHomes(Island island, SyncedObject<Location[]> locationsSync) {
        locationsSync.write(locations -> loadObject(island, "islands_visitor_homes", teleportLocationRow -> {
            int environment = World.Environment.valueOf((String) teleportLocationRow.get("environment")).ordinal();
            locations[environment] = FileUtils.toLocation((String) teleportLocationRow.get("location"));
        }));
    }

    public static void deserializeEffects(Island island, Map<PotionEffectType, UpgradeValue<Integer>> islandEffects) {
        loadObject(island, "islands_effects", islandEffectRow -> {
            PotionEffectType potionEffectType = PotionEffectType.getByName((String) islandEffectRow.get("effect_type"));
            if (potionEffectType != null) {
                int level = (int) islandEffectRow.get("level");
                islandEffects.put(potionEffectType, new UpgradeValue<>(level, i -> i < 0));
            }
        });
    }

    public static void deserializeIslandChest(Island island, SyncedObject<IslandChest[]> islandChestsSync) {
        List<IslandChest> islandChestList = new ArrayList<>();

        loadObject(island, "islands_chests", islandChestRow -> {
            int index = (int) islandChestRow.get("index");
            String contents = (String) islandChestRow.get("contents");

            while (index > islandChestList.size()){
                IslandChest newIslandChest = new SIslandChest(island, islandChestList.size());
                newIslandChest.setRows(plugin.getSettings().getIslandChests().getDefaultSize());
                islandChestList.add(newIslandChest);
            }

            islandChestList.add(SIslandChest.createChest(island, index, ItemUtils.deserialize(contents)));
        });

        islandChestsSync.set(islandChestList.toArray(new IslandChest[0]));
    }

    public static void deserializeRoleLimits(Island island, Map<PlayerRole, UpgradeValue<Integer>> roleLimits){
        loadObject(island, "islands_role_limits", roleLimitRaw -> {
            PlayerRole playerRole = SPlayerRole.fromId((int) roleLimitRaw.get("role"));
            if (playerRole != null) {
                int limit = (int) roleLimitRaw.get("limit");
                roleLimits.put(playerRole, new UpgradeValue<>(limit, i -> i < 0));
            }
        });
    }

    public static void deserializeWarpCategories(Island island){
        loadObject(island, "islands_warp_categories", warpCategoryRow -> {
            String name = StringUtils.stripColors((String) warpCategoryRow.get("name"));

            WarpCategory warpCategory = island.getWarpCategory(name);

            if (warpCategory != null) {
                if (warpCategory.getWarps().isEmpty()) {
                    island.deleteCategory(warpCategory);
                    return;
                }

                int slot = (int) warpCategoryRow.get("slot");
                warpCategory.setSlot(slot);

                ItemStack icon = ItemUtils.deserializeItem((String) warpCategoryRow.get("icon"));
                if (icon != null)
                    warpCategory.setIcon(icon);
            }
        });
    }

    public static void deserializeIslandBank(Island island){
        loadObject(island, "islands_banks", islandBankRow -> {
            BigDecimal balance = new BigDecimal((String) islandBankRow.get("balance"));
            long lastInterestTime = getAsLong(islandBankRow.get("last_interest_time"));
            island.getIslandBank().setBalance(balance);
            island.setLastInterestTime(lastInterestTime);
        });
    }

    public static void deserializeIslandSettings(Island island, Consumer<Map<String, Object>> islandSettingsConsumer){
        loadObject(island, "islands_settings", islandSettingsConsumer);
    }

    private static void loadObject(Island island, String table, Consumer<Map<String, Object>> resultConsumer){
        island.getDatabaseBridge().loadObject(table,
                new DatabaseFilter(Collections.singletonList(new Pair<>("island", island.getUniqueId().toString()))),
                resultConsumer);
    }

    private static long getAsLong(Object value){
        if(value instanceof Long) {
            return (long) value;
        }
        else if(value instanceof Integer) {
            return (int) value;
        }  else {
            throw new IllegalArgumentException("Cannot cast " + value + " from type " + value.getClass() + " to long.");
        }
    }

    private static byte getAsByte(Object value){
        if(value instanceof Byte) {
            return (byte) value;
        }
        else if(value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : 0;
        }
        else if(value instanceof Integer) {
            return (byte) (int) value;
        }  else {
            throw new IllegalArgumentException("Cannot cast " + value + " from type " + value.getClass() + " to byte.");
        }
    }

}
