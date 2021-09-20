package com.bgsoftware.superiorskyblock.config;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.key.dataset.KeySet;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.bgsoftware.superiorskyblock.values.BlockValuesHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SettingsContainer {

    public final String databaseType;
    public final String databaseMySQLAddress;
    public final int databaseMySQLPort;
    public final String databaseMySQLDBName;
    public final String databaseMySQLUsername;
    public final String databaseMySQLPassword;
    public final String databaseMySQLPrefix;
    public final boolean databaseMySQLSSL;
    public final boolean databaseMySQLPublicKeyRetrieval;
    public final long databaseMySQLWaitTimeout;
    public final long databaseMySQLMaxLifetime;
    public final int maxIslandSize;
    public final String islandCommand;
    public final int defaultIslandSize;
    public final KeyMap<Integer> defaultBlockLimits;
    public final KeyMap<Integer> defaultEntityLimits;
    public final KeyMap<Integer>[] defaultGenerator;
    public final int defaultWarpsLimit;
    public final int defaultTeamLimit;
    public final int defaultCoopLimit;
    public final int defaultCropGrowth;
    public final double defaultSpawnerRates;
    public final double defaultMobDrops;
    public final BigDecimal defaultBankLimit;
    public final Map<Integer, Integer> defaultRoleLimits;
    public final int islandsHeight;
    public final boolean worldBordersEnabled;
    public final boolean stackedBlocksEnabled;
    public final KeySet whitelistedStackedBlocks;
    public final List<String> stackedBlocksDisabledWorlds;
    public final String stackedBlocksName;
    public final KeyMap<Integer> stackedBlocksLimits;
    public final boolean stackedBlocksAutoPickup;
    public final boolean stackedBlocksMenuEnabled;
    public final String stackedBlocksMenuTitle;
    public final String islandLevelFormula;
    public final boolean roundedIslandLevel;
    public final String islandTopOrder;
    public final ConfigurationSection islandRolesSection;
    public final long calcInterval;
    public final String signWarpLine;
    public final List<String> signWarp;
    public final String visitorsSignLine;
    public final String visitorsSignActive;
    public final String visitorsSignInactive;
    public final World.Environment defaultWorldEnvironment;
    public final String defaultWorldName;
    public final String islandWorldName;
    public final boolean normalWorldEnabled;
    public final boolean normalWorldUnlocked;
    public final boolean normalSchematicOffset;
    public final boolean netherWorldEnabled;
    public final boolean netherWorldUnlocked;
    public final String netherWorldName;
    public final boolean netherSchematicOffset;
    public final boolean endWorldEnabled;
    public final boolean endWorldUnlocked;
    public final String endWorldName;
    public final boolean endSchematicOffset;
    public final boolean endDragonFight;
    public final String worldsDifficulty;
    public final String spawnLocation;
    public final boolean spawnProtection;
    public final List<String> spawnSettings;
    public final List<String> spawnPermissions;
    public final boolean spawnWorldBorder;
    public final int spawnSize;
    public final boolean spawnDamage;
    public final boolean voidTeleportMembers;
    public final boolean voidTeleportVisitors;
    public final List<String> interactables;
    public final boolean visitorsDamage;
    public final boolean coopDamage;
    public final int disbandCount;
    public final boolean islandTopIncludeLeader;
    public final Map<String, String> defaultPlaceholders;
    public final boolean banConfirm;
    public final boolean disbandConfirm;
    public final boolean kickConfirm;
    public final boolean leaveConfirm;
    public final String spawnersProvider;
    public final String stackedBlocksProvider;
    public final boolean disbandInventoryClear;
    public final boolean islandNamesRequiredForCreation;
    public final int islandNamesMaxLength;
    public final int islandNamesMinLength;
    public final List<String> filteredIslandNames;
    public final boolean islandNamesColorSupport;
    public final boolean islandNamesIslandTop;
    public final boolean islandNamesPreventPlayerNames;
    public final boolean teleportOnJoin;
    public final boolean teleportOnKick;
    public final boolean clearOnJoin;
    public final boolean rateOwnIsland;
    public final List<String> defaultSettings;
    public final boolean disableRedstoneOffline;
    public final boolean disableRedstoneAFK;
    public final boolean disableSpawningAFK;
    public final Map<String, Pair<Integer, String>> commandsCooldown;
    public final long upgradeCooldown;
    public final String numberFormat;
    public final String dateFormat;
    public final boolean skipOneItemMenus;
    public final boolean teleportOnPVPEnable;
    public final boolean immuneToPVPWhenTeleport;
    public final List<String> blockedVisitorsCommands;
    public final boolean defaultContainersEnabled;
    public final Map<InventoryType, ListTag> defaultContainersContents;
    public final List<String> defaultSignLines;
    public final Map<String, List<String>> eventCommands;
    public final long warpsWarmup;
    public final long homeWarmup;
    public final boolean liquidUpdate;
    public final boolean lightsUpdate;
    public final List<String> pvpWorlds;
    public final boolean stopLeaving;
    public final boolean valuesMenu;
    public final List<String> cropsToGrow;
    public final int cropsInterval;
    public final boolean onlyBackButton;
    public final boolean buildOutsideIsland;
    public final String defaultLanguage;
    public final boolean defaultWorldBorder;
    public final boolean defaultBlocksStacker;
    public final boolean defaultToggledPanel;
    public final boolean defaultIslandFly;
    public final String defaultBorderColor;
    public final boolean generators;
    public final boolean obsidianToLava;
    public final BlockValuesHandler.SyncWorthStatus syncWorth;
    public final boolean negativeWorth;
    public final boolean negativeLevel;
    public final List<String> disabledEvents;
    public final boolean schematicNameArgument;
    public final String islandChestTitle;
    public final int islandChestsDefaultPage;
    public final int islandChestsDefaultSize;
    public final Map<String, List<String>> commandAliases;
    public final KeySet valuableBlocks;
    public final Map<String, Location> islandPreviewLocations;
    public final boolean tabCompleteHideVanished;
    public final boolean dropsUpgradePlayersMultiply;
    public final long protectedMessageDelay;
    public final boolean warpCategories;
    public final boolean physicsListener;
    public final double chargeOnWarp;
    public final boolean publicWarps;
    public final long recalcTaskTimeout;

    public SettingsContainer(SuperiorSkyblockPlugin plugin, YamlConfiguration config) throws HandlerLoadException {
        databaseType = config.getString("database.type");
        databaseMySQLAddress = config.getString("database.address");
        databaseMySQLPort = config.getInt("database.port");
        databaseMySQLDBName = config.getString("database.db-name");
        databaseMySQLUsername = config.getString("database.user-name");
        databaseMySQLPassword = config.getString("database.password");
        databaseMySQLPrefix = config.getString("database.prefix");
        databaseMySQLSSL = config.getBoolean("database.useSSL");
        databaseMySQLPublicKeyRetrieval = config.getBoolean("database.allowPublicKeyRetrieval");
        databaseMySQLWaitTimeout = config.getLong("database.waitTimeout");
        databaseMySQLMaxLifetime = config.getLong("database.maxLifetime");

        calcInterval = config.getLong("calc-interval", 6000);
        islandCommand = config.getString("island-command", "island,is,islands");
        maxIslandSize = config.getInt("max-island-size", 200);
        defaultIslandSize = config.getInt("default-values.island-size", 20);
        defaultBlockLimits = new KeyMap<>();
        for(String line : config.getStringList("default-values.block-limits")){
            String[] sections = line.split(":");

            if(sections.length < 2){
                SuperiorSkyblockPlugin.log("&cCouldn't parse block limit '" + line + "', skipping...");
                continue;
            }

            String gloablKey = sections[0];
            String subKey = sections.length == 2 ? "" : sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultBlockLimits.put(Key.of(gloablKey, subKey), Integer.parseInt(limit));
        }
        defaultEntityLimits = new KeyMap<>();
        for(String line : config.getStringList("default-values.entity-limits")){
            String[] sections = line.split(":");

            if(sections.length < 2){
                SuperiorSkyblockPlugin.log("&cCouldn't parse entity limit '" + line + "', skipping...");
                continue;
            }

            String gloablKey = sections[0];
            String subKey = sections.length == 2 ? "" : sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultEntityLimits.put(Key.of(gloablKey, subKey), Integer.parseInt(limit));
        }
        defaultTeamLimit = config.getInt("default-values.team-limit", 4);
        defaultWarpsLimit = config.getInt("default-values.warps-limit", 3);
        defaultCoopLimit = config.getInt("default-values.coop-limit", 8);
        defaultCropGrowth = config.getInt("default-values.crop-growth", 1);
        defaultSpawnerRates = config.getDouble("default-values.spawner-rates", 1D);
        defaultMobDrops = config.getDouble("default-values.mob-drops", 1D);
        defaultBankLimit = new BigDecimal(config.getString("default-values.bank-limit", "-1"));
        defaultRoleLimits = new HashMap<>();
        for(String line : config.getStringList("default-values.role-limits")){
            String[] sections = line.split(":");
            try {
                defaultRoleLimits.put(Integer.parseInt(sections[0]), Integer.parseInt(sections[1]));
            }catch (NumberFormatException ignored){}
        }
        islandsHeight = config.getInt("islands-height", 100);
        worldBordersEnabled = config.getBoolean("world-borders", true);
        stackedBlocksEnabled = config.getBoolean("stacked-blocks.enabled", true);
        stackedBlocksDisabledWorlds = config.getStringList("stacked-blocks.disabled-worlds");
        whitelistedStackedBlocks = new KeySet(config.getStringList("stacked-blocks.whitelisted"));
        stackedBlocksName = StringUtils.translateColors(config.getString("stacked-blocks.custom-name"));
        stackedBlocksLimits = new KeyMap<>();
        config.getStringList("stacked-blocks.limits").forEach(line -> {
            String[] sections = line.split(":");
            try {
                if (sections.length == 2)
                    stackedBlocksLimits.put(Key.of(sections[0], ""), Integer.parseInt(sections[1]));
                else if (sections.length == 3)
                    stackedBlocksLimits.put(Key.of(sections[0], sections[1]), Integer.parseInt(sections[2]));
            }catch(Exception ignored){}
        });
        stackedBlocksAutoPickup = config.getBoolean("stacked-blocks.auto-collect", false);
        stackedBlocksMenuEnabled = config.getBoolean("stacked-blocks.deposit-menu.enabled", true);
        stackedBlocksMenuTitle = StringUtils.translateColors(config.getString("stacked-blocks.deposit-menu.title", "&lDeposit Blocks"));
        islandLevelFormula = config.getString("island-level-formula", "{} / 2");
        roundedIslandLevel = config.getBoolean("rounded-island-level", false);
        islandTopOrder = config.getString("island-top-order", "WORTH");
        islandRolesSection = config.getConfigurationSection("island-roles");
        signWarpLine = config.getString("sign-warp-line", "[IslandWarp]");
        signWarp = StringUtils.translateColors(config.getStringList("sign-warp"));
        while(signWarp.size() < 4)
            signWarp.add("");
        visitorsSignLine = config.getString("visitors-sign.line", "[Welcome]");
        visitorsSignActive = StringUtils.translateColors(config.getString("visitors-sign.active", "&a[Welcome]"));
        visitorsSignInactive = StringUtils.translateColors(config.getString("visitors-sign.inactive", "&c[Welcome]"));
        islandWorldName = config.getString("worlds.world-name", "SuperiorWorld");
        normalWorldEnabled = config.getBoolean("worlds.normal.enabled", true);
        normalWorldUnlocked = config.getBoolean("worlds.normal.unlock", true);
        normalSchematicOffset = config.getBoolean("worlds.normal.schematic-offset", true);
        netherWorldEnabled = config.getBoolean("worlds.nether.enabled", false);
        netherWorldUnlocked = config.getBoolean("worlds.nether.unlock", true);
        String netherWorldName = config.getString("worlds.nether.name", "");
        this.netherWorldName = netherWorldName.isEmpty() ? islandWorldName + "_nether" : netherWorldName;
        netherSchematicOffset = config.getBoolean("worlds.nether.schematic-offset", true);
        endWorldEnabled = config.getBoolean("worlds.end.enabled", false);
        endWorldUnlocked = config.getBoolean("worlds.end.unlock", false);
        String endWorldName = config.getString("worlds.end.name", "");
        this.endWorldName = endWorldName.isEmpty() ? islandWorldName + "_the_end" : endWorldName;
        endSchematicOffset = config.getBoolean("worlds.end.schematic-offset", true);
        endDragonFight = endWorldEnabled && config.getBoolean("worlds.end.dragon-fight", false) && ServerVersion.isAtLeast(ServerVersion.v1_9);
        String defaultWorldEnvironment = config.getString("worlds.default-world");
        if(defaultWorldEnvironment.equalsIgnoreCase("normal") && normalWorldEnabled){
            this.defaultWorldEnvironment = World.Environment.NORMAL;
            this.defaultWorldName = this.islandWorldName;
        }
        else if(defaultWorldEnvironment.equalsIgnoreCase("nether") && netherWorldEnabled){
            this.defaultWorldEnvironment = World.Environment.NETHER;
            this.defaultWorldName = this.netherWorldName;
        }
        else if(defaultWorldEnvironment.equalsIgnoreCase("the_end") && endWorldEnabled){
            this.defaultWorldEnvironment = World.Environment.THE_END;
            this.defaultWorldName = this.endWorldName;
        }
        else{
            throw new HandlerLoadException("Cannot find a default islands world.", HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }
        worldsDifficulty = config.getString("worlds.difficulty", "EASY");
        spawnLocation = config.getString("spawn.location", "SuperiorWorld, 0, 100, 0, 0, 0");
        spawnProtection = config.getBoolean("spawn.protection", true);
        spawnSettings = config.getStringList("spawn.settings");
        spawnPermissions = config.getStringList("spawn.permissions");
        spawnWorldBorder = config.getBoolean("spawn.world-border", false);
        spawnSize = config.getInt("spawn.size", 200);
        spawnDamage = config.getBoolean("spawn.players-damage", false);
        voidTeleportMembers = config.getBoolean("void-teleport.members", true);
        voidTeleportVisitors = config.getBoolean("void-teleport.visitors", true);
        interactables = loadInteractables(plugin);
        visitorsDamage = config.getBoolean("visitors-damage", false);
        coopDamage = config.getBoolean("coop-damage", true);
        disbandCount = config.getInt("disband-count", 5);
        islandTopIncludeLeader = config.getBoolean("island-top-include-leader", true);
        defaultPlaceholders = config.getStringList("default-placeholders").stream().collect(Collectors.toMap(
                line -> line.split(":")[0].replace("superior_", "").toLowerCase(),
                line -> line.split(":")[1]
        ));
        banConfirm = config.getBoolean("ban-confirm");
        disbandConfirm = config.getBoolean("disband-confirm");
        kickConfirm = config.getBoolean("kick-confirm");
        leaveConfirm = config.getBoolean("leave-confirm");
        spawnersProvider = config.getString("spawners-provider", "AUTO");
        stackedBlocksProvider = config.getString("stacked-blocks-provider", "AUTO");
        disbandInventoryClear = config.getBoolean("disband-inventory-clear", true);
        islandNamesRequiredForCreation = config.getBoolean("island-names.required-for-creation", true);
        islandNamesMaxLength = config.getInt("island-names.max-length", 16);
        islandNamesMinLength = config.getInt("island-names.min-length", 3);
        filteredIslandNames = config.getStringList("island-names.filtered-names");
        islandNamesColorSupport = config.getBoolean("island-names.color-support", true);
        islandNamesIslandTop = config.getBoolean("island-names.island-top", true);
        islandNamesPreventPlayerNames = config.getBoolean("island-names.prevent-player-names", true);
        teleportOnJoin = config.getBoolean("teleport-on-join", false);
        teleportOnKick = config.getBoolean("teleport-on-kick", false);
        clearOnJoin = config.getBoolean("clear-on-join", false);
        rateOwnIsland = config.getBoolean("rate-own-island", false);
        defaultSettings = config.getStringList("default-settings");
        defaultGenerator = new KeyMap[World.Environment.values().length];
        if(config.isConfigurationSection("default-values.generator")){
            for(String env : config.getConfigurationSection("default-values.generator").getKeys(false)){
                try{
                    World.Environment environment = World.Environment.valueOf(env.toUpperCase());
                    loadGenerator(config.getStringList("default-values.generator." + env), environment.ordinal());
                }catch (Exception ignored){}
            }
        }
        else {
            loadGenerator(config.getStringList("default-values.generator"), 0);
        }
        disableRedstoneOffline = config.getBoolean("disable-redstone-offline", true);
        disableRedstoneAFK = config.getBoolean("afk-integrations.disable-redstone", false);
        disableSpawningAFK = config.getBoolean("afk-integrations.disable-spawning", true);
        commandsCooldown = new HashMap<>();
        for(String subCommand : config.getConfigurationSection("commands-cooldown").getKeys(false)){
            int cooldown = config.getInt("commands-cooldown." + subCommand + ".cooldown");
            String permission = config.getString("commands-cooldown." + subCommand + ".bypass-permission");
            commandsCooldown.put(subCommand, new Pair<>(cooldown, permission));
        }
        upgradeCooldown = config.getLong("upgrade-cooldown", -1L);
        numberFormat = config.getString("number-format", "en-US");
        StringUtils.setNumberFormatter(numberFormat);
        dateFormat = config.getString("date-format", "dd/MM/yyyy HH:mm:ss");
        StringUtils.setDateFormatter(dateFormat);
        skipOneItemMenus = config.getBoolean("skip-one-item-menus", false);
        teleportOnPVPEnable = config.getBoolean("teleport-on-pvp-enable", true);
        immuneToPVPWhenTeleport = config.getBoolean("immune-to-pvp-when-teleport", true);
        blockedVisitorsCommands = config.getStringList("blocked-visitors-commands");
        defaultContainersEnabled = config.getBoolean("default-containers.enabled", false);
        defaultContainersContents = new HashMap<>();
        if(config.contains("default-containers.containers")) {
            for (String container : config.getConfigurationSection("default-containers.containers").getKeys(false)) {
                try {
                    InventoryType containerType = InventoryType.valueOf(container.toUpperCase());
                    ListTag items = new ListTag(CompoundTag.class, new ArrayList<>());
                    defaultContainersContents.put(containerType, items);

                    ConfigurationSection containerSection = config.getConfigurationSection("default-containers.containers." + container);
                    for (String slot : containerSection.getKeys(false)) {
                        try {
                            // Reading the item from the config
                            ItemStack itemStack = FileUtils.getItemStack("config.yml", containerSection.getConfigurationSection(slot)).build();
                            itemStack.setAmount(containerSection.getInt(slot + ".amount", 1));

                            // Parsing it into compound tag
                            CompoundTag itemCompound = plugin.getNMSTags().convertToNBT(itemStack);
                            itemCompound.setByte("Slot", Byte.parseByte(slot));

                            items.addTag(itemCompound);
                        } catch (Exception ignored) {}
                    }
                } catch (IllegalArgumentException ex) {
                    SuperiorSkyblockPlugin.log("&cInvalid container type: " + container + ".");
                }
            }
        }
        defaultSignLines = config.getStringList("default-signs");
        eventCommands = new HashMap<>();
        if(config.contains("event-commands")) {
            for (String eventName : config.getConfigurationSection("event-commands").getKeys(false)) {
                eventCommands.put(eventName.toLowerCase(), config.getStringList("event-commands." + eventName));
            }
        }
        warpsWarmup = config.getLong("warps-warmup", 0);
        homeWarmup = config.getLong("home-warmup", 0);
        liquidUpdate = config.getBoolean("liquid-update", false);
        lightsUpdate = config.getBoolean("lights-update", false);
        pvpWorlds = config.getStringList("pvp-worlds");
        stopLeaving = config.getBoolean("stop-leaving", false);
        valuesMenu = config.getBoolean("values-menu", true);
        cropsToGrow = config.getStringList("crops-to-grow");
        cropsInterval = config.getInt("crops-interval", 5);
        onlyBackButton = config.getBoolean("only-back-button", false);
        buildOutsideIsland = config.getBoolean("build-outside-island", false);
        defaultLanguage = config.getString("default-language", "en-US");
        defaultWorldBorder = config.getBoolean("default-world-border", true);
        defaultBlocksStacker = config.getBoolean("default-blocks-stacker", true);
        defaultToggledPanel = config.getBoolean("default-toggled-panel", false);
        defaultIslandFly = config.getBoolean("default-island-fly", false);
        defaultBorderColor = config.getString("default-border-color", "BLUE");
        generators = config.getBoolean("generators", true);
        obsidianToLava = config.getBoolean("obsidian-to-lava", false);
        syncWorth = BlockValuesHandler.SyncWorthStatus.of(config.getString("sync-worth", "NONE"));
        negativeWorth = config.getBoolean("negative-worth", true);
        negativeLevel = config.getBoolean("negative-level", true);
        disabledEvents = config.getStringList("disabled-events").stream().map(String::toLowerCase).collect(Collectors.toList());
        schematicNameArgument = config.getBoolean("schematic-name-argument", true);
        islandChestTitle = StringUtils.translateColors(config.getString("island-chests.chest-title", "&4Island Chest"));
        islandChestsDefaultPage = config.getInt("island-chests.default-pages", 0);
        islandChestsDefaultSize = config.getInt("island-chests.default-size", 3);
        commandAliases = new HashMap<>();
        if(config.isConfigurationSection("command-aliases")){
            for(String label : config.getConfigurationSection("command-aliases").getKeys(false)){
                commandAliases.put(label.toLowerCase(), config.getStringList("command-aliases." + label));
            }
        }
        valuableBlocks = new KeySet(config.getStringList("valuable-blocks"));
        islandPreviewLocations = new HashMap<>();
        if(config.isConfigurationSection("preview-islands")){
            for(String schematic : config.getConfigurationSection("preview-islands").getKeys(false))
                islandPreviewLocations.put(schematic.toLowerCase(), LocationUtils.getLocation(config.getString("preview-islands." + schematic)));
        }
        tabCompleteHideVanished = config.getBoolean("tab-complete-hide-vanished", true);
        dropsUpgradePlayersMultiply = config.getBoolean("drops-upgrade-players-multiply", false);
        protectedMessageDelay = config.getLong("protected-message-delay", 60L);
        warpCategories = config.getBoolean("warp-categories", true);
        physicsListener = config.getBoolean("physics-listener", true);
        chargeOnWarp = config.getDouble("charge-on-warp", 0D);
        publicWarps = config.getBoolean("public-warps");
        recalcTaskTimeout = config.getLong("recalc-task-timeout");
    }

    private List<String> loadInteractables(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if(!file.exists())
            plugin.saveResource("interactables.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        return cfg.getStringList("interactables");
    }

    private void loadGenerator(List<String> lines, int index){
        defaultGenerator[index] = new KeyMap<>();
        for(String line : lines){
            String[] sections = line.split(":");
            String globalKey = sections[0];
            String subKey = sections.length == 2 ? "" : sections[1];
            String percentage = sections.length == 2 ? sections[1] : sections[2];
            defaultGenerator[index].put(globalKey, subKey, Integer.parseInt(percentage));
        }
    }

}
