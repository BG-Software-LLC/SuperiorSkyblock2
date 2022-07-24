package com.bgsoftware.superiorskyblock.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.formatting.impl.DateFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.NumberFormatter;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.key.KeySetImpl;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.values.BlockValuesManagerImpl;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsContainer {

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
    public boolean coopMembers;
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
    public final String normalBiome;
    public final boolean netherWorldEnabled;
    public final boolean netherWorldUnlocked;
    public final String netherWorldName;
    public final boolean netherSchematicOffset;
    public final String netherBiome;
    public final boolean endWorldEnabled;
    public final boolean endWorldUnlocked;
    public final String endWorldName;
    public final boolean endSchematicOffset;
    public final String endBiome;
    public final boolean endDragonFightEnabled;
    public final BlockOffset endDragonFightPortalOffset;
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
    public final KeySet safeBlocks;
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
    public final boolean obsidianToLava;
    public final BlockValuesManagerImpl.SyncWorthStatus syncWorth;
    public final boolean negativeWorth;
    public final boolean negativeLevel;
    public final List<String> disabledEvents;
    public final List<String> disabledCommands;
    public final List<String> disabledHooks;
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
    public final boolean autoLanguageDetection;
    public final boolean autoUncoopWhenAlone;
    public final TopIslandMembersSorting islandTopMembersSorting;
    public final int bossBarLimit;
    public final boolean deleteUnsafeWarps;

    public SettingsContainer(SuperiorSkyblockPlugin plugin, YamlConfiguration config) throws ManagerLoadException {
        databaseType = config.getString("database.type").toUpperCase(Locale.ENGLISH);
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
        defaultBlockLimits = KeyMapImpl.createHashMap();
        for (String line : config.getStringList("default-values.block-limits")) {
            String[] sections = line.split(":");

            if (sections.length < 2) {
                SuperiorSkyblockPlugin.log("&cCouldn't parse block limit '" + line + "', skipping...");
                continue;
            }

            String gloablKey = sections[0];
            String subKey = sections.length == 2 ? "" : sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            Key key = KeyImpl.of(gloablKey, subKey);
            defaultBlockLimits.put(key, Integer.parseInt(limit));
            plugin.getBlockValues().addCustomBlockKey(key);
        }
        defaultEntityLimits = KeyMapImpl.createHashMap();
        for (String line : config.getStringList("default-values.entity-limits")) {
            String[] sections = line.split(":");

            if (sections.length < 2) {
                SuperiorSkyblockPlugin.log("&cCouldn't parse entity limit '" + line + "', skipping...");
                continue;
            }

            String gloablKey = sections[0];
            String subKey = sections.length == 2 ? "" : sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultEntityLimits.put(KeyImpl.of(gloablKey, subKey), Integer.parseInt(limit));
        }
        defaultTeamLimit = config.getInt("default-values.team-limit", 4);
        defaultWarpsLimit = config.getInt("default-values.warps-limit", 3);
        defaultCoopLimit = config.getInt("default-values.coop-limit", 8);
        defaultCropGrowth = config.getInt("default-values.crop-growth", 1);
        defaultSpawnerRates = config.getDouble("default-values.spawner-rates", 1D);
        defaultMobDrops = config.getDouble("default-values.mob-drops", 1D);
        defaultBankLimit = new BigDecimal(config.getString("default-values.bank-limit", "-1"));
        defaultRoleLimits = new HashMap<>();
        for (String line : config.getStringList("default-values.role-limits")) {
            String[] sections = line.split(":");
            try {
                defaultRoleLimits.put(Integer.parseInt(sections[0]), Integer.parseInt(sections[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        islandsHeight = config.getInt("islands-height", 100);
        worldBordersEnabled = config.getBoolean("world-borders", true);
        stackedBlocksEnabled = config.getBoolean("stacked-blocks.enabled", true);
        stackedBlocksDisabledWorlds = config.getStringList("stacked-blocks.disabled-worlds");
        whitelistedStackedBlocks = KeySetImpl.createHashSet(config.getStringList("stacked-blocks.whitelisted")
                .stream().map(KeyImpl::of).collect(Collectors.toList()));
        stackedBlocksName = Formatters.COLOR_FORMATTER.format(config.getString("stacked-blocks.custom-name"));
        stackedBlocksLimits = KeyMapImpl.createHashMap();
        config.getStringList("stacked-blocks.limits").forEach(line -> {
            String[] sections = line.split(":");
            try {
                if (sections.length == 2)
                    stackedBlocksLimits.put(KeyImpl.of(sections[0], ""), Integer.parseInt(sections[1]));
                else if (sections.length == 3)
                    stackedBlocksLimits.put(KeyImpl.of(sections[0], sections[1]), Integer.parseInt(sections[2]));
            } catch (Exception ex) {
                PluginDebugger.debug(ex);
            }
        });
        stackedBlocksAutoPickup = config.getBoolean("stacked-blocks.auto-collect", false);
        stackedBlocksMenuEnabled = config.getBoolean("stacked-blocks.deposit-menu.enabled", true);
        stackedBlocksMenuTitle = Formatters.COLOR_FORMATTER.format(config.getString("stacked-blocks.deposit-menu.title", "&lDeposit Blocks"));
        islandLevelFormula = config.getString("island-level-formula", "{} / 2");
        roundedIslandLevel = config.getBoolean("rounded-island-level", false);
        islandTopOrder = config.getString("island-top-order", "WORTH");
        coopMembers = config.getBoolean("coop-members", true);
        islandRolesSection = config.getConfigurationSection("island-roles");
        signWarpLine = config.getString("sign-warp-line", "[IslandWarp]");
        signWarp = Formatters.formatList(config.getStringList("sign-warp"), Formatters.COLOR_FORMATTER);
        while (signWarp.size() < 4)
            signWarp.add("");
        visitorsSignLine = config.getString("visitors-sign.line", "[Welcome]");
        visitorsSignActive = Formatters.COLOR_FORMATTER.format(config.getString("visitors-sign.active", "&a[Welcome]"));
        visitorsSignInactive = Formatters.COLOR_FORMATTER.format(config.getString("visitors-sign.inactive", "&c[Welcome]"));
        islandWorldName = config.getString("worlds.world-name", "SuperiorWorld");
        normalWorldEnabled = config.getBoolean("worlds.normal.enabled", true);
        normalWorldUnlocked = config.getBoolean("worlds.normal.unlock", true);
        normalSchematicOffset = config.getBoolean("worlds.normal.schematic-offset", true);
        normalBiome = config.getString("worlds.normal.biome", "PLAINS");
        netherWorldEnabled = config.getBoolean("worlds.nether.enabled", false);
        netherWorldUnlocked = config.getBoolean("worlds.nether.unlock", true);
        String netherWorldName = config.getString("worlds.nether.name", "");
        this.netherWorldName = netherWorldName.isEmpty() ? islandWorldName + "_nether" : netherWorldName;
        netherSchematicOffset = config.getBoolean("worlds.nether.schematic-offset", true);
        netherBiome = config.getString("worlds.nether.biome", "NETHER_WASTES").toUpperCase(Locale.ENGLISH);
        endWorldEnabled = config.getBoolean("worlds.end.enabled", false);
        endWorldUnlocked = config.getBoolean("worlds.end.unlock", false);
        String endWorldName = config.getString("worlds.end.name", "");
        this.endWorldName = endWorldName.isEmpty() ? islandWorldName + "_the_end" : endWorldName;
        endSchematicOffset = config.getBoolean("worlds.end.schematic-offset", true);
        endBiome = config.getString("worlds.end.biome", "THE_END");
        endDragonFightEnabled = endWorldEnabled && config.getBoolean("worlds.end.dragon-fight.enabled", false) && ServerVersion.isAtLeast(ServerVersion.v1_9);
        BlockOffset endDragonFightPortalOffset = null;
        if (endDragonFightEnabled) {
            endDragonFightPortalOffset = Serializers.OFFSET_SPACED_SERIALIZER.deserialize(config.getString("worlds.end.dragon-fight.portal-offset"));
            if (endDragonFightPortalOffset == null) {
                SuperiorSkyblockPlugin.log("&c[config.yml] Cannot parse portal-offset to a valid offset, skipping...");
            }
        }
        this.endDragonFightPortalOffset = endDragonFightPortalOffset == null ? SBlockOffset.ZERO : endDragonFightPortalOffset;
        String defaultWorldEnvironment = config.getString("worlds.default-world");
        if (defaultWorldEnvironment.equalsIgnoreCase("normal") && normalWorldEnabled) {
            this.defaultWorldEnvironment = World.Environment.NORMAL;
            this.defaultWorldName = this.islandWorldName;
        } else if (defaultWorldEnvironment.equalsIgnoreCase("nether") && netherWorldEnabled) {
            this.defaultWorldEnvironment = World.Environment.NETHER;
            this.defaultWorldName = this.netherWorldName;
        } else if (defaultWorldEnvironment.equalsIgnoreCase("the_end") && endWorldEnabled) {
            this.defaultWorldEnvironment = World.Environment.THE_END;
            this.defaultWorldName = this.endWorldName;
        } else {
            throw new ManagerLoadException("Cannot find a default islands world.", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }
        worldsDifficulty = config.getString("worlds.difficulty", "EASY").toUpperCase(Locale.ENGLISH);
        spawnLocation = config.getString("spawn.location", "SuperiorWorld, 0, 100, 0, 0, 0");
        spawnProtection = config.getBoolean("spawn.protection", true);
        spawnSettings = config.getStringList("spawn.settings")
                .stream().map(str -> str.toUpperCase(Locale.ENGLISH)).collect(Collectors.toList());
        spawnPermissions = config.getStringList("spawn.permissions")
                .stream().map(str -> str.toUpperCase(Locale.ENGLISH)).collect(Collectors.toList());
        spawnWorldBorder = config.getBoolean("spawn.world-border", false);
        spawnSize = config.getInt("spawn.size", 200);
        spawnDamage = config.getBoolean("spawn.players-damage", false);
        voidTeleportMembers = config.getBoolean("void-teleport.members", true);
        voidTeleportVisitors = config.getBoolean("void-teleport.visitors", true);
        interactables = loadInteractables(plugin);
        safeBlocks = loadSafeBlocks(plugin);
        visitorsDamage = config.getBoolean("visitors-damage", false);
        coopDamage = config.getBoolean("coop-damage", true);
        disbandCount = config.getInt("disband-count", 5);
        islandTopIncludeLeader = config.getBoolean("island-top-include-leader", true);
        defaultPlaceholders = config.getStringList("default-placeholders").stream().collect(Collectors.toMap(
                line -> line.split(":")[0].replace("superior_", "").toLowerCase(Locale.ENGLISH),
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
        filteredIslandNames = config.getStringList("island-names.filtered-names").stream()
                .map(str -> str.toLowerCase(Locale.ENGLISH))
                .collect(Collectors.toList());
        islandNamesColorSupport = config.getBoolean("island-names.color-support", true);
        islandNamesIslandTop = config.getBoolean("island-names.island-top", true);
        islandNamesPreventPlayerNames = config.getBoolean("island-names.prevent-player-names", true);
        teleportOnJoin = config.getBoolean("teleport-on-join", false);
        teleportOnKick = config.getBoolean("teleport-on-kick", false);
        clearOnJoin = config.getBoolean("clear-on-join", false);
        rateOwnIsland = config.getBoolean("rate-own-island", false);
        defaultSettings = config.getStringList("default-settings")
                .stream().map(str -> str.toUpperCase(Locale.ENGLISH)).collect(Collectors.toList());
        defaultGenerator = new KeyMap[World.Environment.values().length];
        if (config.isConfigurationSection("default-values.generator")) {
            for (String env : config.getConfigurationSection("default-values.generator").getKeys(false)) {
                try {
                    World.Environment environment = World.Environment.valueOf(env.toUpperCase(Locale.ENGLISH));
                    loadGenerator(config.getStringList("default-values.generator." + env), environment.ordinal());
                } catch (Exception ex) {
                    PluginDebugger.debug(ex);
                }
            }
        } else {
            loadGenerator(config.getStringList("default-values.generator"), 0);
        }
        disableRedstoneOffline = config.getBoolean("disable-redstone-offline", true);
        disableRedstoneAFK = config.getBoolean("afk-integrations.disable-redstone", false);
        disableSpawningAFK = config.getBoolean("afk-integrations.disable-spawning", true);
        commandsCooldown = new HashMap<>();
        for (String subCommand : config.getConfigurationSection("commands-cooldown").getKeys(false)) {
            int cooldown = config.getInt("commands-cooldown." + subCommand + ".cooldown");
            String permission = config.getString("commands-cooldown." + subCommand + ".bypass-permission");
            commandsCooldown.put(subCommand, new Pair<>(cooldown, permission));
        }
        upgradeCooldown = config.getLong("upgrade-cooldown", -1L);
        numberFormat = config.getString("number-format", "en-US");
        NumberFormatter.setNumberFormatter(numberFormat);
        dateFormat = config.getString("date-format", "dd/MM/yyyy HH:mm:ss");
        DateFormatter.setDateFormatter(plugin, dateFormat);
        skipOneItemMenus = config.getBoolean("skip-one-item-menus", false);
        teleportOnPVPEnable = config.getBoolean("teleport-on-pvp-enable", true);
        immuneToPVPWhenTeleport = config.getBoolean("immune-to-pvp-when-teleport", true);
        blockedVisitorsCommands = config.getStringList("blocked-visitors-commands");
        defaultContainersEnabled = config.getBoolean("default-containers.enabled", false);
        defaultContainersContents = new HashMap<>();
        if (config.contains("default-containers.containers")) {
            for (String container : config.getConfigurationSection("default-containers.containers").getKeys(false)) {
                try {
                    InventoryType containerType = InventoryType.valueOf(container.toUpperCase(Locale.ENGLISH));
                    ListTag items = new ListTag(CompoundTag.class, Collections.emptyList());
                    defaultContainersContents.put(containerType, items);

                    ConfigurationSection containerSection = config.getConfigurationSection("default-containers.containers." + container);
                    for (String slot : containerSection.getKeys(false)) {
                        try {
                            // Reading the item from the config
                            TemplateItem templateItem = MenuParser.getItemStack("config.yml", containerSection.getConfigurationSection(slot));

                            if (templateItem == null)
                                continue;

                            ItemStack itemStack = templateItem.build();
                            itemStack.setAmount(containerSection.getInt(slot + ".amount", 1));

                            // Parsing it into compound tag
                            CompoundTag itemCompound = plugin.getNMSTags().convertToNBT(itemStack);
                            itemCompound.setByte("Slot", Byte.parseByte(slot));

                            items.addTag(itemCompound);
                        } catch (Exception ex) {
                            PluginDebugger.debug(ex);
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    SuperiorSkyblockPlugin.log("&cInvalid container type: " + container + ".");
                    PluginDebugger.debug(ex);
                }
            }
        }
        defaultSignLines = config.getStringList("default-signs");
        eventCommands = new HashMap<>();
        if (config.contains("event-commands")) {
            for (String eventName : config.getConfigurationSection("event-commands").getKeys(false)) {
                eventCommands.put(eventName.toLowerCase(Locale.ENGLISH), config.getStringList("event-commands." + eventName));
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
        obsidianToLava = config.getBoolean("obsidian-to-lava", false);
        syncWorth = BlockValuesManagerImpl.SyncWorthStatus.of(config.getString("sync-worth", "NONE"));
        negativeWorth = config.getBoolean("negative-worth", true);
        negativeLevel = config.getBoolean("negative-level", true);
        disabledEvents = config.getStringList("disabled-events")
                .stream().map(str -> str.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
        disabledCommands = config.getStringList("disabled-commands")
                .stream().map(str -> str.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
        disabledHooks = config.getStringList("disabled-hooks")
                .stream().map(str -> str.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
        schematicNameArgument = config.getBoolean("schematic-name-argument", true);
        islandChestTitle = Formatters.COLOR_FORMATTER.format(config.getString("island-chests.chest-title", "&4Island Chest"));
        islandChestsDefaultPage = config.getInt("island-chests.default-pages", 0);
        islandChestsDefaultSize = Math.min(6, Math.max(1, config.getInt("island-chests.default-size", 3)));
        commandAliases = new HashMap<>();
        if (config.isConfigurationSection("command-aliases")) {
            for (String label : config.getConfigurationSection("command-aliases").getKeys(false)) {
                commandAliases.put(label.toLowerCase(Locale.ENGLISH), config.getStringList("command-aliases." + label));
            }
        }
        valuableBlocks = KeySetImpl.createHashSet(config.getStringList("valuable-blocks").stream()
                .map(KeyImpl::of).collect(Collectors.toSet()));
        islandPreviewLocations = new HashMap<>();
        if (config.isConfigurationSection("preview-islands")) {
            for (String schematic : config.getConfigurationSection("preview-islands").getKeys(false))
                islandPreviewLocations.put(schematic.toLowerCase(Locale.ENGLISH), Serializers.LOCATION_SERIALIZER
                        .deserialize(config.getString("preview-islands." + schematic)));
        }
        tabCompleteHideVanished = config.getBoolean("tab-complete-hide-vanished", true);
        dropsUpgradePlayersMultiply = config.getBoolean("drops-upgrade-players-multiply", false);
        protectedMessageDelay = config.getLong("protected-message-delay", 60L);
        warpCategories = config.getBoolean("warp-categories", true);
        physicsListener = config.getBoolean("physics-listener", true);
        chargeOnWarp = config.getDouble("charge-on-warp", 0D);
        publicWarps = config.getBoolean("public-warps");
        recalcTaskTimeout = config.getLong("recalc-task-timeout");
        autoLanguageDetection = config.getBoolean("auto-language-detection", true);
        autoUncoopWhenAlone = config.getBoolean("auto-uncoop-when-alone", false);
        TopIslandMembersSorting islandTopMembersSorting;
        try {
            islandTopMembersSorting = TopIslandMembersSorting.valueOf(config.getString("island-top-members-sorting").toUpperCase());
        } catch (IllegalArgumentException error) {
            islandTopMembersSorting = TopIslandMembersSorting.NAMES;
        }
        this.islandTopMembersSorting = islandTopMembersSorting;
        bossBarLimit = config.getInt("bossbar-limit", 1);
        deleteUnsafeWarps = config.getBoolean("delete-unsafe-warps", true);
    }

    private List<String> loadInteractables(SuperiorSkyblockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if (!file.exists())
            plugin.saveResource("interactables.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        return cfg.getStringList("interactables");
    }

    private KeySet loadSafeBlocks(SuperiorSkyblockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "safe_blocks.yml");

        if (!file.exists())
            Resources.saveResource("safe_blocks.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        List<String> safeBlocks = cfg.getStringList("safe-blocks");

        if (safeBlocks.isEmpty()) {
            SuperiorSkyblockPlugin.log("&c[safe_blocks.yml] There are no valid safe blocks! Generating default ones...");
            safeBlocks.addAll(Arrays.stream(Material.values())
                    .filter(Material::isSolid)
                    .map(Material::name)
                    .sorted()
                    .collect(Collectors.toList()));

            try {
                cfg.set("safe-blocks", safeBlocks);
                cfg.save(file);
            } catch (IOException error) {
                PluginDebugger.debug(error);
            }
        }

        return KeySetImpl.createHashSet(safeBlocks.stream()
                .map(KeyImpl::of)
                .collect(Collectors.toSet()));
    }

    private void loadGenerator(List<String> lines, int index) {
        defaultGenerator[index] = KeyMapImpl.createHashMap();
        for (String line : lines) {
            String[] sections = line.toUpperCase(Locale.ENGLISH).split(":");
            String globalKey = sections[0];
            String subKey = sections.length == 2 ? "" : sections[1];
            String percentage = sections.length == 2 ? sections[1] : sections[2];
            defaultGenerator[index].put(KeyImpl.of(globalKey, subKey), Integer.parseInt(percentage));
        }
    }

}
