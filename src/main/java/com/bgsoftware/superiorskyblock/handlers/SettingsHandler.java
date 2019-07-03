package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.config.ConfigComments;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    public final int maxIslandSize;
    public final int defaultIslandSize;
    public final int defaultHoppersLimit;
    public final int defaultWarpsLimit;
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
    public final String signWarpLine;
    public final List<String> signWarp;
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

    public SettingsHandler(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(ConfigComments.class, file);
        cfg.resetYamlFile(plugin, "config.yml");

        saveInterval = cfg.getLong("save-interval", 6000);
        calcInterval = cfg.getLong("calc-interval", 6000);
        maxIslandSize = cfg.getInt("max-island-size", 200);
        defaultIslandSize = cfg.getInt("default-island-size", 20);
        defaultHoppersLimit = cfg.getInt("default-hoppers-limit", 8);
        defaultTeamLimit = cfg.getInt("default-team-limit", 4);
        defaultWarpsLimit = cfg.getInt("default-warps-limit", 3);
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
        signWarpLine = cfg.getString("sign-warp-line", "[IslandWarp]");
        signWarp = colorize(cfg.getStringList("sign-warp"));
        bankWorthRate = cfg.getInt("bank-worth-rate", 1000);
        islandWorld = cfg.getString("island-world", "SuperiorWorld");
        spawnLocation = cfg.getString("spawn-location", "SuperiorWorld, 0, 100, 0");
        spawnProtection = cfg.getBoolean("spawn-protection", true);
        spawnPvp = cfg.getBoolean("spawn-pvp", false);
        voidTeleport = cfg.getBoolean("void-teleport", true);
        interactables = cfg.getStringList("interactables");
        visitorsDamage = cfg.getBoolean("visitors-damage", false);
        disbandCount = cfg.getInt("disband-counts", 5);
        islandTopIncludeLeader = cfg.getBoolean("island-top-include-leader", true);
        defaultPlaceholders = cfg.getStringList("default-placeholders").stream().collect(Collectors.toMap(
                line -> line.split(":")[0].replace("superior_", "").toLowerCase(),
                line -> line.split(":")[1]
        ));
        disbandConfirm = cfg.getBoolean("disband-confirm");
        spawnersProvider = cfg.getString("spawners-provider", "AUTO");
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

    public Location getSpawnAsBukkitLocation() {
        String[] split = spawnLocation.split(", ");

        World world = Bukkit.getWorld(split[0]);
        double x = Double.valueOf(split[1]);
        double y = Double.valueOf(split[2]);
        double z = Double.valueOf(split[3]);
        double yaw = 0;
        double pitch = 0;
        if (split.length == 6) {
            yaw = Double.valueOf(split[4]);
            pitch = Double.valueOf(split[5]);
        }

        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }

}
