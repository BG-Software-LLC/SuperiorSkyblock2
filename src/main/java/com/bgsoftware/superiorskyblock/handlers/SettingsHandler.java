package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.config.ConfigComments;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    public final int maxIslandSize;
    public final int defaultIslandSize;
    public final int defaultHoppersLimit;
    public final int defaultTeamLimit;
    public final int defaultCropGrowth;
    public final int defaultSpawnerRates;
    public final int defaultMobDrops;
    public final boolean worldBordersEnabled;
    public final boolean stackedBlocksEnabled;
    public final KeySet whitelistedStackedBlocks;
    public final List<String> stackedBlocksDisabledWorlds;
    public final String stackedBlocksName;
    public final String islandLevelFormula;
    public final String islandTopOrder;
    public final long saveInterval;
    public final long calcInterval;
    public final List<String> guestPermissions, memberPermissions, modPermissions, adminPermission, leaderPermissions;
    public final List<String> signWarp;
    public final int bankWorthRate;
    public final String islandWorld;
    public final String spawnLocation;
    public final boolean spawnProtection;
    public final boolean spawnPvp;
    public final boolean voidTeleport;

    public SettingsHandler(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(ConfigComments.class);
        cfg.load(file);

        cfg.resetYamlFile(plugin, "config.yml");

        saveInterval = cfg.getLong("save-interval", 6000);
        calcInterval = cfg.getLong("calc-interval", 6000);
        maxIslandSize = cfg.getInt("max-island-size", 200);
        defaultIslandSize = cfg.getInt("default-island-size", 20);
        defaultHoppersLimit = cfg.getInt("default-hoppers-limit", 8);
        defaultTeamLimit = cfg.getInt("default-team-limit", 4);
        defaultCropGrowth = cfg.getInt("default-crop-growth", 1);
        defaultSpawnerRates = cfg.getInt("default-spawner-rates", 1);
        defaultMobDrops = cfg.getInt("default-mob-drops", 1);
        worldBordersEnabled = cfg.getBoolean("world-borders", true);
        stackedBlocksEnabled = cfg.getBoolean("stacked-blocks.enabled", true);
        stackedBlocksDisabledWorlds = cfg.getStringList("stacked-blocks.disabled-worlds");
        whitelistedStackedBlocks = new KeySet(cfg.getStringList("stacked-blocks.whitelisted"));
        stackedBlocksName = ChatColor.translateAlternateColorCodes('&', cfg.getString("stacked-blocks.custom-name"));
        islandLevelFormula = cfg.getString("island-level-formula", "{} / 2");
        islandTopOrder = cfg.getString("island-top-order", "WORTH");
        guestPermissions = cfg.getStringList("default-permissions.guest");
        memberPermissions = cfg.getStringList("default-permissions.member");
        modPermissions = cfg.getStringList("default-permissions.mod");
        adminPermission = cfg.getStringList("default-permissions.admin");
        leaderPermissions = cfg.getStringList("default-permissions.leader");
        signWarp = colorize(cfg.getStringList("sign-warp"));
        bankWorthRate = cfg.getInt("bank-worth-rate", 1000);
        islandWorld = cfg.getString("island-world", "SuperiorWorld");
        spawnLocation = cfg.getString("spawn-location", "SuperiorWorld, 0, 100, 0");
        spawnProtection = cfg.getBoolean("spawn-protection", true);
        spawnPvp = cfg.getBoolean("spawn-pvp", false);
        voidTeleport = cfg.getBoolean("void-teleport", true);
    }

    private List<String> colorize(List<String> list){
        List<String> newList = new ArrayList<>();

        for(String line : list)
            newList.add(ChatColor.translateAlternateColorCodes('&', line));

        return newList;
    }

}
