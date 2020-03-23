package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.UUID;

public final class IslandDeserializer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void deserializePlayers(String members, SyncedObject<? extends Collection<SuperiorPlayer>> membersSetSync){
        membersSetSync.write(membersSet -> {
            for(String uuid : members.split(",")) {
                try {
                    membersSet.add(SSuperiorPlayer.of(UUID.fromString(uuid)));
                }catch(Exception ignored){}
            }
        });
    }

    public static void deserializePermissions(String permissions, Registry<SuperiorPlayer, PlayerPermissionNode> playerPermissions, Registry<IslandPrivilege, PlayerRole> rolePermissions, Island island){
        for(String entry : permissions.split(",")) {
            try {
                String[] sections = entry.split("=");

                try {
                    PlayerRole playerRole = SPlayerRole.of(sections[0]);
                    if(sections.length != 1){
                        String[] permission = permissions.split(";");
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
                    SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(UUID.fromString(sections[0]));
                    playerPermissions.add(superiorPlayer, new PlayerPermissionNode(superiorPlayer, island, sections.length == 1 ? "" : sections[1]));
                }
            }catch(Exception ignored){}
        }
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
        blockLimitsSync.write(blockLimits -> {
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

    public static void deserializeSettings(String settings, Registry<IslandFlag, Byte> islandSettings){
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

    public static void deserializeGenerators(String generator, SyncedObject<KeyMap<Integer>> cobbleGeneratorSync){
        cobbleGeneratorSync.write(cobbleGenerator -> {
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
