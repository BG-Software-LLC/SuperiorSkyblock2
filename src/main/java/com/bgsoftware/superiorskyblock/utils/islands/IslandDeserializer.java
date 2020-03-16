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
import com.bgsoftware.superiorskyblock.island.permissions.PermissionNodeAbstract;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.island.permissions.RolePermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;
import java.util.Collection;
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

    public static void deserializePermissions(String permissions, Registry<Object, PermissionNodeAbstract> permissionNodes, Island island){
        Registry<PlayerRole, String> permissionsMap = Registry.createRegistry();

        boolean shouldSaveAgain = false;

        for(String entry : permissions.split(",")) {
            try {
                String[] sections = entry.split("=");

                if(!shouldSaveAgain && sections.length == 2 && !sections[1].contains(":"))
                    shouldSaveAgain = true;

                try {
                    permissionsMap.add(SPlayerRole.of(sections[0]), sections.length == 1 ? "" : sections[1]);
                }catch(Exception ex){
                    SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(UUID.fromString(sections[0]));
                    permissionNodes.add(superiorPlayer, new PlayerPermissionNode(superiorPlayer, island, sections.length == 1 ? "" : sections[1]));
                }
            }catch(Exception ignored){}
        }

        for(PlayerRole playerRole : plugin.getPlayers().getRoles()){
            PlayerRole previousRole = SPlayerRole.of(playerRole.getWeight() - 1);
            permissionNodes.add(playerRole, new RolePermissionNode(playerRole, permissionNodes.get(previousRole), permissionsMap.get(playerRole, "")));
        }

        if(shouldSaveAgain)
            ((SIsland) island).savePermissionNodes();

        permissionsMap.delete();
    }

    public static void deserializeUpgrades(String upgrades, Registry<String, Integer> upgradesMap){
        for(String entry : upgrades.split(",")) {
            try {
                String[] sections = entry.split("=");
                upgradesMap.add(sections[0], Integer.parseInt(sections[1]));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeWarps(String warps, Registry<String, SIsland.WarpData> warpsMap){
        for(String entry : warps.split(";")) {
            try {
                String[] sections = entry.split("=");
                boolean privateFlag = sections.length == 3 && Boolean.parseBoolean(sections[2]);
                warpsMap.add(sections[0], new SIsland.WarpData(FileUtils.toLocation(sections[1]), privateFlag));
            }catch(Exception ignored){}
        }
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

    public static void deserializeRatings(String ratings, Registry<UUID, Rating> ratingsMap){
        for(String entry : ratings.split(";")){
            try{
                String[] sections = entry.split("=");
                ratingsMap.add(UUID.fromString(sections[0]), Rating.valueOf(Integer.parseInt(sections[1])));
            }catch(Exception ignored){}
        }
    }

    public static void deserializeMissions(String missions, Registry<Mission, Integer> completedMissions){
        for(String mission : missions.split(";")){
            String[] missionSections = mission.split("=");
            int completeAmount = missionSections.length > 1 ? Integer.parseInt(missionSections[1]) : 1;
            Mission _mission = plugin.getMissions().getMission(missionSections[0]);
            if(_mission != null)
                completedMissions.add(_mission, completeAmount);
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

    public static void deserializeLocations(String locationParam, Registry<World.Environment, Location> locations){
        if(!locationParam.contains("=")){
            locationParam = "normal=" + locationParam;
        }

        for(String worldSection : locationParam.split(";")){
            try {
                String[] locationSection = worldSection.split("=");
                String environment = locationSection[0].toUpperCase();
                locations.add(World.Environment.valueOf(environment), LocationUtils.getLocation(locationSection[1]));
            }catch(Exception ignored){}
        }
    }

}
