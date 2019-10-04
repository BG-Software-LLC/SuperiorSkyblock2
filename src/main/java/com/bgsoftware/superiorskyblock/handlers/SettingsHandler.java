package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.config.ConfigComments;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    public final int maxIslandSize;
    public final int defaultIslandSize;
    public final KeyMap<Integer> defaultBlockLimits;
    public final int defaultWarpsLimit;
    public final int defaultTeamLimit;
    public final int defaultCropGrowth;
    public final int defaultSpawnerRates;
    public final int defaultMobDrops;
    public final int defaultIslandHeight;
    public final boolean worldBordersEnabled;
    public final boolean stackedBlocksEnabled;
    public final KeySet whitelistedStackedBlocks;
    public final List<String> stackedBlocksDisabledWorlds;
    public final String stackedBlocksName;
    public final String islandLevelFormula;
    public final String islandTopOrder;
    public final ConfigurationSection islandRolesSection;
    public final long saveInterval;
    public final long calcInterval;
    public final String signWarpLine;
    public final List<String> signWarp;
    public final String welcomeWarpLine;
    public final int bankWorthRate;
    public final String islandWorld;
    public final String spawnLocation;
    public final boolean spawnProtection;
    public final boolean spawnPvp;
    public final boolean voidTeleport;
    public final List<String> interactables;
    public final boolean visitorsDamage;
    public final int disbandCount;
    public final boolean islandTopIncludeLeader;
    public final Map<String, String> defaultPlaceholders;
    public final boolean disbandConfirm;
    public final String spawnersProvider;
    public final boolean disbandInventoryClear;
    public final boolean islandNamesRequiredForCreation;
    public final int islandNamesMaxLength;
    public final int islandNamesMinLength;
    public final List<String> filteredIslandNames;
    public final boolean islandNamesColorSupport;
    public final boolean islandNamesIslandTop;
    public final boolean teleportOnJoin;
    public final boolean clearOnJoin;
    public final boolean rateOwnIsland;
    public final boolean bonusAffectLevel;

    public SettingsHandler(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(ConfigComments.class, file);
        convertData(cfg);
        cfg.resetYamlFile(plugin, "config.yml");

        saveInterval = cfg.getLong("save-interval", 6000);
        calcInterval = cfg.getLong("calc-interval", 6000);
        maxIslandSize = cfg.getInt("max-island-size", 200);
        defaultIslandSize = cfg.getInt("default-island-size", 20);
        defaultBlockLimits = new KeyMap<>();
        for(String line : cfg.getStringList("default-limits")){
            String[] sections = line.split(":");
            String key = sections.length == 2 ? sections[0] : sections[0] + sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultBlockLimits.put(Key.of(key), Integer.parseInt(limit));
        }
        defaultTeamLimit = cfg.getInt("default-team-limit", 4);
        defaultWarpsLimit = cfg.getInt("default-warps-limit", 3);
        defaultCropGrowth = cfg.getInt("default-crop-growth", 1);
        defaultSpawnerRates = cfg.getInt("default-spawner-rates", 1);
        defaultMobDrops = cfg.getInt("default-mob-drops", 1);
        defaultIslandHeight = cfg.getInt("default-island-height", 100);
        worldBordersEnabled = cfg.getBoolean("world-borders", true);
        stackedBlocksEnabled = cfg.getBoolean("stacked-blocks.enabled", true);
        stackedBlocksDisabledWorlds = cfg.getStringList("stacked-blocks.disabled-worlds");
        whitelistedStackedBlocks = new KeySet(cfg.getStringList("stacked-blocks.whitelisted"));
        stackedBlocksName = ChatColor.translateAlternateColorCodes('&', cfg.getString("stacked-blocks.custom-name"));
        islandLevelFormula = cfg.getString("island-level-formula", "{} / 2");
        islandTopOrder = cfg.getString("island-top-order", "WORTH");
        islandRolesSection = cfg.getConfigurationSection("island-roles");
        signWarpLine = cfg.getString("sign-warp-line", "[IslandWarp]");
        signWarp = colorize(cfg.getStringList("sign-warp"));
        welcomeWarpLine = cfg.getString("welcome-sign-line", "[Welcome]");
        bankWorthRate = cfg.getInt("bank-worth-rate", 1000);
        islandWorld = cfg.getString("island-world", "SuperiorWorld");
        spawnLocation = cfg.getString("spawn-location", "SuperiorWorld, 0, 100, 0, 0, 0");
        spawnProtection = cfg.getBoolean("spawn-protection", true);
        spawnPvp = cfg.getBoolean("spawn-pvp", false);
        voidTeleport = cfg.getBoolean("void-teleport", true);
        interactables = cfg.getStringList("interactables");
        visitorsDamage = cfg.getBoolean("visitors-damage", false);
        disbandCount = cfg.getInt("disband-count", 5);
        islandTopIncludeLeader = cfg.getBoolean("island-top-include-leader", true);
        defaultPlaceholders = cfg.getStringList("default-placeholders").stream().collect(Collectors.toMap(
                line -> line.split(":")[0].replace("superior_", "").toLowerCase(),
                line -> line.split(":")[1]
        ));
        disbandConfirm = cfg.getBoolean("disband-confirm");
        spawnersProvider = cfg.getString("spawners-provider", "AUTO");
        disbandInventoryClear = cfg.getBoolean("disband-inventory-clear", true);
        islandNamesRequiredForCreation = cfg.getBoolean("island-names.required-for-creation", true);
        islandNamesMaxLength = cfg.getInt("island-names.max-length", 16);
        islandNamesMinLength = cfg.getInt("island-names.min-length", 3);
        filteredIslandNames = cfg.getStringList("island-names.filtered-names");
        islandNamesColorSupport = cfg.getBoolean("island-names.color-support", true);
        islandNamesIslandTop = cfg.getBoolean("island-names.island-top", true);
        teleportOnJoin = cfg.getBoolean("teleport-on-join", false);
        clearOnJoin = cfg.getBoolean("clear-on-join", false);
        rateOwnIsland = cfg.getBoolean("rate-own-island", false);
        bonusAffectLevel = cfg.getBoolean("bonus-affect-level", true);
    }

    public void updateValue(String path, Object value){
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(ConfigComments.class, file);

        cfg.resetYamlFile(plugin, "config.yml");

        cfg.set(path, value);

        cfg.save(file);

        try{
            Field field = SuperiorSkyblockPlugin.class.getDeclaredField("settingsHandler");
            field.setAccessible(true);
            field.set(plugin, new SettingsHandler(plugin));
        }catch(NoSuchFieldException | IllegalAccessException ex){
            ex.printStackTrace();
        }
    }

    private List<String> colorize(List<String> list){
        List<String> newList = new ArrayList<>();

        for(String line : list)
            newList.add(ChatColor.translateAlternateColorCodes('&', line));

        return newList;
    }

    private void convertData(YamlConfiguration cfg){
        if(cfg.contains("default-hoppers-limit")){
            cfg.set("default-limits", Collections.singletonList("HOPPER:" + cfg.getInt("default-hoppers-limit")));
            cfg.set("default-hoppers-limit", null);
        }
        if(cfg.contains("default-permissions")){
            cfg.set("island-roles.guest.name", "Guest");
            cfg.set("island-roles.guest.permissions", cfg.getStringList("default-permissions.guest"));
            cfg.set("island-roles.ladder.member.name", "Member");
            cfg.set("island-roles.ladder.member.weight", 0);
            cfg.set("island-roles.ladder.member.permissions", cfg.getStringList("default-permissions.member"));
            cfg.set("island-roles.ladder.mod.name", "Moderator");
            cfg.set("island-roles.ladder.mod.weight", 1);
            cfg.set("island-roles.ladder.mod.permissions", cfg.getStringList("default-permissions.mod"));
            cfg.set("island-roles.ladder.admin.name", "Admin");
            cfg.set("island-roles.ladder.admin.weight", 2);
            cfg.set("island-roles.ladder.admin.permissions", cfg.getStringList("default-permissions.admin"));
            cfg.set("island-roles.ladder.leader.name", "Leader");
            cfg.set("island-roles.ladder.leader.weight", 3);
            cfg.set("island-roles.ladder.leader.permissions", cfg.getStringList("default-permissions.leader"));
        }
    }

}
