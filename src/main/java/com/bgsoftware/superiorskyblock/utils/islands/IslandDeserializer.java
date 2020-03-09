package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPermissionNode;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class IslandDeserializer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void deserializePlayers(String members, SyncedObject<? extends Collection<SuperiorPlayer>> membersSetSync){
        membersSetSync.run(membersSet -> {
            for(String uuid : members.split(",")) {
                try {
                    membersSet.add(SSuperiorPlayer.of(UUID.fromString(uuid)));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializePermissions(String permissions, SyncedObject<Map<Object, SPermissionNode>> permissionNodesSync){
        permissionNodesSync.run(permissionNodes -> {
            Map<PlayerRole, String> permissionsMap = new HashMap<>();

            for(String entry : permissions.split(",")) {
                try {
                    String[] sections = entry.split("=");
                    try {
                        permissionsMap.put(SPlayerRole.of(sections[0]), sections.length == 1 ? "" : sections[1]);
                    }catch(Exception ex){
                        permissionNodes.put(UUID.fromString(sections[0]), new SPermissionNode(sections.length == 1 ? "" : sections[1], null));
                    }
                }catch(Exception ignored){}
            }

            for(PlayerRole playerRole : plugin.getPlayers().getRoles()){
                PlayerRole previousRole = SPlayerRole.of(playerRole.getWeight() - 1);
                permissionNodes.put(playerRole, new SPermissionNode(permissionsMap.getOrDefault(playerRole, ""), permissionNodes.get(previousRole)));
            }
        });
    }

    public static void deserializeUpgrades(String upgrades, SyncedObject<Map<String, Integer>> upgradesMapSync){
        upgradesMapSync.run(upgradesMap -> {
            for(String entry : upgrades.split(",")) {
                try {
                    String[] sections = entry.split("=");
                    upgradesMap.put(sections[0], Integer.parseInt(sections[1]));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializeWarps(String warps, SyncedObject<Map<String, SIsland.WarpData>> warpsMapSync){
        warpsMapSync.run(warpsMap -> {
            for(String entry : warps.split(";")) {
                try {
                    String[] sections = entry.split("=");
                    boolean privateFlag = sections.length == 3 && Boolean.parseBoolean(sections[2]);
                    warpsMap.put(sections[0], new SIsland.WarpData(FileUtils.toLocation(sections[1]), privateFlag));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializeBlockCounts(String blocks, Island island){
        for(String entry : blocks.split(";")){
            try{
                String[] sections = entry.split("=");
                island.handleBlockPlace(Key.of(sections[0]), Integer.parseInt(sections[1]), false);
            }catch(Exception ignored){}
        }
    }

    public static void deserializeBlockLimits(String blocks, SyncedObject<KeyMap<Integer>> blockLimitsSync){
        blockLimitsSync.run(blockLimits -> {
            for(String limit : blocks.split(",")){
                try {
                    String[] sections = limit.split("=");
                    blockLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializeRatings(String ratings, SyncedObject<Map<UUID, Rating>> ratingsMapSync){
        ratingsMapSync.run(ratingsMap -> {
            for(String entry : ratings.split(";")){
                try{
                    String[] sections = entry.split("=");
                    ratingsMap.put(UUID.fromString(sections[0]), Rating.valueOf(Integer.parseInt(sections[1])));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializeMissions(String missions, SyncedObject<Map<Mission, Integer>> completedMissionsSync){
        completedMissionsSync.run(completedMissions -> {
            deserializeMissions(missions, completedMissions);
        });
    }

    public static void deserializeMissions(String missions, Map<Mission, Integer> completedMissions){
        for(String mission : missions.split(";")){
            String[] missionSections = mission.split("=");
            int completeAmount = missionSections.length > 1 ? Integer.parseInt(missionSections[1]) : 1;
            Mission _mission = plugin.getMissions().getMission(missionSections[0]);
            if(_mission != null)
                completedMissions.put(_mission, completeAmount);
        }
    }

    public static void deserializeSettings(String settings, SyncedObject<Set<IslandFlag>> islandSettingsSync){
        islandSettingsSync.run(islandSettings -> {
            islandSettings.addAll(Arrays.stream(settings.split(";")).filter(setting -> {
                try{
                    IslandFlag.getByName(setting);
                    return true;
                }catch(Exception ex){
                    return false;
                }
            }).map(IslandFlag::getByName).collect(Collectors.toList()));
        });
    }

    public static void deserializeGenerators(String generator, SyncedObject<KeyMap<Integer>> cobbleGeneratorSync){
        cobbleGeneratorSync.run(cobbleGenerator -> {
            for(String limit : generator.split(",")){
                try {
                    String[] sections = limit.split("=");
                    cobbleGenerator.put(sections[0], Integer.parseInt(sections[1]));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializeLocations(String locationParam, SyncedObject<Map<World.Environment, Location>> locationsSync){
        locationsSync.run(locations -> {
            String location = locationParam;

            if(!location.contains("=")){
                location = "normal=" + location;
            }

            for(String worldSection : location.split(";")){
                try {
                    String[] locationSection = worldSection.split("=");
                    String environment = locationSection[0].toUpperCase();
                    locations.put(World.Environment.valueOf(environment), LocationUtils.getLocation(locationSection[1]));
                }catch(Exception ignored){}
            }
        });
    }

}
