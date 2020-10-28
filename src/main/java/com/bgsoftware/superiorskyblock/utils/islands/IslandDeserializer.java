package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class IslandDeserializer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandDeserializer(){

    }

    public static void deserializePlayers(String members, SyncedObject<? extends Collection<SuperiorPlayer>> membersSetSync){
        if(members == null)
            return;

        membersSetSync.write(membersSet -> {
            for(String uuid : members.split(",")) {
                try {
                    membersSet.add(plugin.getPlayers().getSuperiorPlayer(UUID.fromString(uuid)));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializePlayersWithTimes(String members, SyncedObject<? extends Collection<Pair<SuperiorPlayer, Long>>> membersSetSync){
        if(members == null)
            return;

        membersSetSync.write(membersSet -> {
            for(String member : members.split(",")) {
                try {
                    String[] memberSections = member.split(";");
                    long lastTimeJoined = memberSections.length == 2 ? Long.parseLong(memberSections[1]) : System.currentTimeMillis();
                    membersSet.add(new Pair<>(plugin.getPlayers().getSuperiorPlayer(UUID.fromString(memberSections[0])), lastTimeJoined));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializePermissions(String permissions, Registry<SuperiorPlayer, PlayerPermissionNode> playerPermissions, Registry<IslandPrivilege, PlayerRole> rolePermissions, Island island){
        if(permissions == null)
            return;

        for(String entry : permissions.split(",")) {
            try {
                String[] sections = entry.split("=");

                try {
                    PlayerRole playerRole = SPlayerRole.of(sections[0]);
                    if(sections.length != 1){
                        String[] permission = sections[1].split(";");
                        for (String perm : permission) {
                            String[] permissionSections = perm.split(":");
                            try {
                                IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permissionSections[0]);
                                if (permissionSections.length == 2 && permissionSections[1].equals("1")) {
                                    rolePermissions.add(islandPrivilege, playerRole);
                                }
                            }catch(Exception ignored){}
                        }
                    }
                }catch(Exception ex){
                    SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(UUID.fromString(sections[0]));
                    playerPermissions.add(superiorPlayer, new PlayerPermissionNode(superiorPlayer, island, sections.length == 1 ? "" : sections[1]));
                }
            }catch(Exception ignored){}
        }
    }

    public static void deserializeUpgrades(String upgrades, Registry<String, Integer> upgradesMap){
        if(upgrades == null)
            return;

        for(String entry : upgrades.split(",")) {
            try {
                String[] sections = entry.split("=");
                if(plugin.getUpgrades().getUpgrade(sections[0]) != null)
                    upgradesMap.add(sections[0], Integer.parseInt(sections[1]));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeWarps(String warps, Registry<String, SIslandWarp> warpsMap){
        if(warps == null)
            return;

        for(String entry : warps.split(";")) {
            try {
                String[] sections = entry.split("=");
                boolean privateFlag = sections.length == 3 && Boolean.parseBoolean(sections[2]);
                warpsMap.add(StringUtils.stripColors(sections[0].trim()), new SIslandWarp(FileUtils.toLocation(sections[1]), privateFlag));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeBlockCounts(String blocks, Island island){
        if(blocks == null)
            return;

        for(String entry : blocks.split(";")){
            try{
                String[] sections = entry.split("=");
                island.handleBlockPlace(Key.of(sections[0]), new BigInteger(sections[1]), false);
            }catch(Exception ignored){}
        }
    }

    public static void deserializeBlockLimits(String blocks, SyncedObject<KeyMap<UpgradeValue<Integer>>> blockLimits){
        blockLimits.write(_blockLimits -> deserializeBlockLimits(blocks, _blockLimits));
    }

    public static void deserializeBlockLimits(String blocks, KeyMap<UpgradeValue<Integer>> blockLimits){
        if(blocks == null)
            return;

        for(String limit : blocks.split(",")){
            try {
                String[] sections = limit.split("=");
                blockLimits.put(Key.of(sections[0]), new UpgradeValue<>(Integer.parseInt(sections[1]), i -> i < 0));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeEntityLimits(String entities, SyncedObject<KeyMap<UpgradeValue<Integer>>> entityLimits){
        entityLimits.write(_entityLimits -> deserializeEntityLimits(entities, _entityLimits));
    }

    public static void deserializeEntityLimits(String entities, KeyMap<UpgradeValue<Integer>> entityLimits){
        if(entities == null)
            return;

        for(String limit : entities.split(",")){
            try {
                String[] sections = limit.split("=");
                entityLimits.put(Key.of(sections[0]), new UpgradeValue<>(Integer.parseInt(sections[1]), i -> i < 0));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeRatings(String ratings, Registry<UUID, Rating> ratingsMap){
        if(ratings == null)
            return;

        for(String entry : ratings.split(";")){
            try{
                String[] sections = entry.split("=");
                ratingsMap.add(UUID.fromString(sections[0]), Rating.valueOf(Integer.parseInt(sections[1])));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeMissions(String missions, Registry<Mission<?>, Integer> completedMissions){
        if(missions == null)
            return;

        for(String mission : missions.split(";")){
            String[] missionSections = mission.split("=");
            int completeAmount = missionSections.length > 1 ? Integer.parseInt(missionSections[1]) : 1;
            Mission<?> _mission = plugin.getMissions().getMission(missionSections[0]);
            if(_mission != null)
                completedMissions.add(_mission, completeAmount);
        }
    }

    public static void deserializeSettings(String settings, Registry<IslandFlag, Byte> islandSettings){
        if(settings == null)
            return;

        for(String setting : settings.split(";")){
            try {
                if (setting.contains("=")) {
                    String[] settingSections = setting.split("=");
                    islandSettings.add(IslandFlag.getByName(settingSections[0]), Byte.valueOf(settingSections[1]));
                } else {
                    if(!plugin.getSettings().defaultSettings.contains(setting))
                        islandSettings.add(IslandFlag.getByName(setting), (byte) 1);
                }
            }catch(Exception ignored){}
        }
    }

    public static void deserializeGenerators(String generator, SyncedObject<KeyMap<UpgradeValue<Integer>>>[] cobbleGenerator){
        if(generator == null)
            return;

        if(generator.contains(";")){
            for(String env : generator.split(";")){
                String[] sections = env.split(":");
                try{
                    World.Environment environment = World.Environment.valueOf(sections[0]);
                    deserializeGenerators(sections[1], cobbleGenerator[environment.ordinal()] = SyncedObject.of(new KeyMap<>()));
                }catch (Exception ignored){}
            }
        }
        else {
            deserializeGenerators(generator, cobbleGenerator[0] = SyncedObject.of(new KeyMap<>()));
        }
    }

    public static void deserializeGenerators(String generator, SyncedObject<KeyMap<UpgradeValue<Integer>>> cobbleGenerator){
        cobbleGenerator.write(_cobbleGenerator -> {
            for (String limit : generator.split(",")) {
                try {
                    String[] sections = limit.split("=");
                    _cobbleGenerator.put(Key.of(sections[0]), new UpgradeValue<>(Integer.parseInt(sections[1]), i -> i < 0));
                } catch (Exception ignored) {}
            }
        });
    }

    public static void deserializeLocations(String locationParam, SyncedObject<Location[]> locations){
        locations.write(_locations -> deserializeLocations(locationParam, _locations));
    }

    public static void deserializeLocations(String locationParam, Location[] locations){
        if(locationParam == null)
            return;
        
        if(!locationParam.contains("=")){
            locationParam = "normal=" + locationParam;
        }

        for(String worldSection : locationParam.split(";")){
            try {
                String[] locationSection = worldSection.split("=");
                String environment = locationSection[0].toUpperCase();
                locations[World.Environment.valueOf(environment).ordinal()] = LocationUtils.getLocation(locationSection[1]);
            }catch(Exception ignored){}
        }
    }

    public static void deserializeEffects(String effects, SyncedObject<Map<PotionEffectType, UpgradeValue<Integer>>> islandEffects){
        islandEffects.write(_islandEffects -> deserializeEffects(effects, _islandEffects));
    }

    public static void deserializeEffects(String effects, Map<PotionEffectType, UpgradeValue<Integer>> islandEffects){
        if(effects == null)
            return;

        for(String effect : effects.split(",")){
            String[] sections = effect.split("=");
            PotionEffectType potionEffectType = PotionEffectType.getByName(sections[0]);
            if(potionEffectType != null)
                islandEffects.put(potionEffectType, new UpgradeValue<>(Integer.parseInt(sections[1]), i -> i < 0));
        }
    }

    public static void deserializeIslandChest(Island island, String islandChest, SyncedObject<IslandChest[]> islandChestSync){
        if(islandChest == null || islandChest.isEmpty())
            return;

        String[] islandChestsSections = islandChest.split("\n");

        IslandChest[] islandChests = new IslandChest[islandChestsSections.length];

        for(int i = 0; i < islandChestsSections.length; i++){
            islandChests[i] = SIslandChest.createChest(island, i, ItemUtils.deserialize(islandChestsSections[i]));
        }

        islandChestSync.set(islandChests);
    }

}
