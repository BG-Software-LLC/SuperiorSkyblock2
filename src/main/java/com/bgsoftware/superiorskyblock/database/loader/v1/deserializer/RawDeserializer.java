package com.bgsoftware.superiorskyblock.database.loader.v1.deserializer;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.database.loader.v1.DatabaseLoader_V1;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RawDeserializer implements IDeserializer {

    private final DatabaseLoader_V1 databaseLoader;
    private final SuperiorSkyblockPlugin plugin;

    public RawDeserializer(DatabaseLoader_V1 databaseLoader, SuperiorSkyblockPlugin plugin){
        this.databaseLoader = databaseLoader;
        this.plugin = plugin;
    }

    @Override
    public Map<String, Integer> deserializeMissions(String missions) {
        Map<String, Integer> completedMissions = new HashMap<>();

        if(missions != null) {
            for (String mission : missions.split(";")) {
                String[] missionSections = mission.split("=");
                int completeAmount = missionSections.length > 1 ? Integer.parseInt(missionSections[1]) : 1;
                completedMissions.put(missionSections[0], completeAmount);
            }
        }

        return completedMissions;
    }

    @Override
    public String[] deserializeHomes(String locationParam) {
        String[] islandHomes = new String[World.Environment.values().length];

        if(locationParam == null)
            return islandHomes;

        String _locationParam = locationParam.contains("=") ? locationParam : "normal=" + locationParam;

        for (String worldSection : _locationParam.split(";")) {
            try {
                String[] locationSection = worldSection.split("=");
                String environment = locationSection[0].toUpperCase();
                islandHomes[World.Environment.valueOf(environment).ordinal()] = locationSection[1];
            } catch (Exception ignored) {
            }
        }

        return islandHomes;
    }

    @Override
    public List<PlayerAttributes> deserializePlayers(String players) {
        List<PlayerAttributes> playerAttributesList = new ArrayList<>();

        if(players != null) {

            for (String uuid : players.split(",")) {
                try {
                    playerAttributesList.add(databaseLoader.getPlayerAttributes(uuid));
                } catch (Exception ignored) {}
            }
        }

        return playerAttributesList;
    }

    @Override
    public Map<UUID, PlayerPermissionNode> deserializePlayerPerms(String permissionNodes) {
        Map<UUID, PlayerPermissionNode> playerPermissions = new HashMap<>();

        if(permissionNodes == null)
            return playerPermissions;

        for(String entry : permissionNodes.split(",")) {
            try {
                String[] sections = entry.split("=");

                try {
                    try{
                        int id = Integer.parseInt(sections[0]);
                        SPlayerRole.fromId(id);
                    }catch (Exception ex){
                        SPlayerRole.of(sections[0]);
                    }
                }catch(Exception ex){
                    playerPermissions.put(UUID.fromString(sections[0]), new PlayerPermissionNode(null,
                            null, sections.length == 1 ? "" : sections[1]));
                }
            }catch(Exception ignored){}
        }

        return playerPermissions;
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes) {
        Map<IslandPrivilege, PlayerRole> rolePermissions = new HashMap<>();

        if(permissionNodes == null)
            return rolePermissions;

        for(String entry : permissionNodes.split(",")) {
            try {
                String[] sections = entry.split("=");

                PlayerRole playerRole;

                try{
                    int id = Integer.parseInt(sections[0]);
                    playerRole = SPlayerRole.fromId(id);
                }catch (Exception ex){
                    playerRole = SPlayerRole.of(sections[0]);
                }

                if(sections.length != 1){
                    String[] permission = sections[1].split(";");
                    for (String perm : permission) {
                        String[] permissionSections = perm.split(":");
                        try {
                            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permissionSections[0]);
                            if (permissionSections.length == 2 && permissionSections[1].equals("1")) {
                                rolePermissions.put(islandPrivilege, playerRole);
                            }
                        }catch(Exception ignored){}
                    }
                }
            }catch(Exception ignored){}
        }

        return rolePermissions;
    }

    @Override
    public Map<String, Integer> deserializeUpgrades(String upgrades) {
        Map<String, Integer> upgradesMap = new HashMap<>();

        if(upgrades != null) {
            for (String entry : upgrades.split(",")) {
                try {
                    String[] sections = entry.split("=");
                    upgradesMap.put(sections[0], Integer.parseInt(sections[1]));
                } catch (Exception ignored) {
                }
            }
        }

        return upgradesMap;
    }

    @Override
    public List<IslandWarpAttributes> deserializeWarps(String islandWarps) {
        List<IslandWarpAttributes> warpAttributes = new ArrayList<>();

        if(islandWarps == null)
            return warpAttributes;

        for(String entry : islandWarps.split(";")) {
            try {
                String[] sections = entry.split("=");
                String name = StringUtils.stripColors(sections[0].trim());
                String category = "";
                boolean privateFlag = sections.length == 3 && Boolean.parseBoolean(sections[2]);

                if(name.contains("-")){
                    String[] nameSections = name.split("-");
                    category = IslandUtils.getWarpName(nameSections[0]);
                    name = nameSections[1];
                }

                name = IslandUtils.getWarpName(name);

                if(name.isEmpty())
                    continue;

                if(!IslandUtils.isWarpNameLengthValid(name))
                    name = name.substring(0, IslandUtils.getMaxWarpNameLength());

                if(!IslandUtils.isWarpNameLengthValid(category))
                    category = category.substring(0, IslandUtils.getMaxWarpNameLength());

                warpAttributes.add(new IslandWarpAttributes()
                        .setValue(IslandWarpAttributes.Field.NAME, name)
                        .setValue(IslandWarpAttributes.Field.CATEGORY, category)
                        .setValue(IslandWarpAttributes.Field.LOCATION, sections[1])
                        .setValue(IslandWarpAttributes.Field.PRIVATE_STATUS, privateFlag)
                        .setValue(IslandWarpAttributes.Field.ICON, sections[3]));
            }catch(Exception ignored){}
        }

        return warpAttributes;
    }

    @Override
    public KeyMap<Integer> deserializeBlockLimits(String blocks) {
        KeyMap<Integer> blockLimits = new KeyMap<>();

        if(blocks != null) {
            for (String limit : blocks.split(",")) {
                try {
                    String[] sections = limit.split("=");
                    blockLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                } catch (Exception ignored) {
                }
            }
        }

        return blockLimits;
    }

    @Override
    public Map<UUID, Rating> deserializeRatings(String ratings) {
        Map<UUID, Rating> ratingsMap = new HashMap<>();

        if(ratings != null) {
            for (String entry : ratings.split(";")) {
                try {
                    String[] sections = entry.split("=");
                    ratingsMap.put(UUID.fromString(sections[0]), Rating.valueOf(Integer.parseInt(sections[1])));
                } catch (Exception ignored) {
                }
            }
        }

        return ratingsMap;
    }

    @Override
    public Map<IslandFlag, Byte> deserializeIslandFlags(String settings) {
        Map<IslandFlag, Byte> islandSettings = new HashMap<>();

        if(settings != null) {
            for (String setting : settings.split(";")) {
                try {
                    if (setting.contains("=")) {
                        String[] settingSections = setting.split("=");
                        islandSettings.put(IslandFlag.getByName(settingSections[0]), Byte.valueOf(settingSections[1]));
                    } else {
                        if (!plugin.getSettings().getDefaultSettings().contains(setting))
                            islandSettings.put(IslandFlag.getByName(setting), (byte) 1);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return islandSettings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public KeyMap<Integer>[] deserializeGenerators(String generator) {
        KeyMap<Integer>[] cobbleGenerator = new KeyMap[World.Environment.values().length];

        if(generator == null)
            return cobbleGenerator;

        if(generator.contains(";")){
            for(String env : generator.split(";")){
                String[] sections = env.split(":");
                try{
                    World.Environment environment = World.Environment.valueOf(sections[0]);
                    deserializeGenerators(sections[1], cobbleGenerator[environment.ordinal()] = new KeyMap<>());
                }catch (Exception ignored){}
            }
        }
        else {
            deserializeGenerators(generator, cobbleGenerator[0] = new KeyMap<>());
        }

        return cobbleGenerator;
    }

    private void deserializeGenerators(String generator, KeyMap<Integer> cobbleGenerator) {
        for (String limit : generator.split(",")) {
            try {
                String[] sections = limit.split("=");
                cobbleGenerator.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public List<Pair<UUID, Long>> deserializeVisitors(String visitorsRaw) {
        List<Pair<UUID, Long>> visitors = new ArrayList<>();

        if(visitorsRaw != null) {
            for (String visitor : visitorsRaw.split(",")) {
                try {
                    String[] visitorSections = visitor.split(";");
                    long lastTimeJoined = visitorSections.length == 2 ? Long.parseLong(visitorSections[1]) : System.currentTimeMillis();
                    visitors.add(new Pair<>(UUID.fromString(visitorSections[0]), lastTimeJoined));
                } catch (Exception ignored) {
                }
            }
        }

        return visitors;
    }

    @Override
    public KeyMap<Integer> deserializeEntityLimits(String entities) {
        KeyMap<Integer> entityLimits = new KeyMap<>();

        if(entities != null) {
            for (String limit : entities.split(",")) {
                try {
                    String[] sections = limit.split("=");
                    entityLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                } catch (Exception ignored) {
                }
            }
        }

        return entityLimits;
    }

    @Override
    public Map<PotionEffectType, Integer> deserializeEffects(String effects) {
        Map<PotionEffectType, Integer> islandEffects = new HashMap<>();

        if(effects != null) {
            for (String effect : effects.split(",")) {
                String[] sections = effect.split("=");
                PotionEffectType potionEffectType = PotionEffectType.getByName(sections[0]);
                if (potionEffectType != null)
                    islandEffects.put(potionEffectType, Integer.parseInt(sections[1]));
            }
        }

        return islandEffects;
    }

    @Override
    public List<IslandChestAttributes> deserializeIslandChests(String islandChest) {
        List<IslandChestAttributes> islandChestAttributes = new ArrayList<>();

        if(islandChest == null || islandChest.isEmpty())
            return islandChestAttributes;

        String[] islandChestsSections = islandChest.split("\n");

        for(int i = 0; i < islandChestsSections.length; i++){
            islandChestAttributes.add(new IslandChestAttributes()
                    .setValue(IslandChestAttributes.Field.INDEX, i)
                    .setValue(IslandChestAttributes.Field.CONTENTS, islandChestsSections[i]));
        }

        return islandChestAttributes;
    }

    @Override
    public Map<PlayerRole, Integer> deserializeRoleLimits(String roles) {
        Map<PlayerRole, Integer> roleLimits = new HashMap<>();

        if(roles != null) {
            for (String limit : roles.split(",")) {
                try {
                    String[] sections = limit.split("=");
                    PlayerRole playerRole = SPlayerRole.fromId(Integer.parseInt(sections[0]));
                    if (playerRole != null)
                        roleLimits.put(playerRole, Integer.parseInt(sections[1]));
                } catch (Exception ignored) {
                }
            }
        }

        return roleLimits;
    }

    @Override
    public List<WarpCategoryAttributes> deserializeWarpCategories(String categories) {
        List<WarpCategoryAttributes> warpCategoryAttributes = new ArrayList<>();

        if(categories == null)
            return warpCategoryAttributes;

        for(String entry : categories.split(";")) {
            try {
                String[] sections = entry.split("=");
                String name = StringUtils.stripColors(sections[0].trim());
                int slot = Integer.parseInt(sections[1]);
                String icon = sections[2];

                warpCategoryAttributes.add(new WarpCategoryAttributes()
                        .setValue(WarpCategoryAttributes.Field.NAME, name)
                        .setValue(WarpCategoryAttributes.Field.SLOT, slot)
                        .setValue(WarpCategoryAttributes.Field.ICON, icon));
            }catch(Exception ignored){}
        }

        return warpCategoryAttributes;
    }

}
