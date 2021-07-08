package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler extends AbstractHandler {

    public final String databaseType;
    public final String databaseMySQLAddress;
    public final int databaseMySQLPort;
    public final String databaseMySQLDBName;
    public final String databaseMySQLUsername;
    public final String databaseMySQLPassword;
    public final String databaseMySQLPrefix;
    public final boolean databaseMySQLSSL;
    public final boolean databaseMySQLPublicKeyRetrieval;
    public final int maxIslandSize;
    public final String islandCommand;
    public final int defaultIslandSize;
    public final KeyMap<UpgradeValue<Integer>> defaultBlockLimits;
    public final KeyMap<UpgradeValue<Integer>> defaultEntityLimits;
    public final KeyMap<UpgradeValue<Integer>>[] defaultGenerator;
    public final int defaultWarpsLimit;
    public final int defaultTeamLimit;
    public final int defaultCoopLimit;
    public final int defaultCropGrowth;
    public final double defaultSpawnerRates;
    public final double defaultMobDrops;
    public final BigDecimal defaultBankLimit;
    public final Map<Integer, UpgradeValue<Integer>> defaultRoleLimits;
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
    public final double bankWorthRate;
    public final String islandWorldName;
    public final boolean netherWorldEnabled;
    public final boolean netherWorldUnlocked;
    public final String netherWorldName;
    public final boolean netherSchematicOffset;
    public final boolean endWorldEnabled;
    public final boolean endWorldUnlocked;
    public final String endWorldName;
    public final boolean endSchematicOffset;
    public final boolean endDragonFight;
    public final boolean optimizeWorlds;
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
    public final Registry<String, String> defaultPlaceholders;
    public final boolean banConfirm;
    public final boolean disbandConfirm;
    public final boolean kickConfirm;
    public final boolean leaveConfirm;
    public final String spawnersProvider;
    public final boolean disbandInventoryClear;
    public final double disbandRefund;
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
    public final Registry<String, Pair<Integer, String>> commandsCooldown;
    public final long upgradeCooldown;
    public final String numberFormat;
    public final String dateFormat;
    public final boolean skipOneItemMenus;
    public final boolean teleportOnPVPEnable;
    public final boolean immuneToPVPWhenTeleport;
    public final List<String> blockedVisitorsCommands;
    public final boolean defaultContainersEnabled;
    public final Registry<InventoryType, ListTag> defaultContainersContents;
    public final List<String> defaultSignLines;
    public final Registry<String, List<String>> eventCommands;
    public final long warpsWarmup;
    public final long homeWarmup;
    public final boolean liquidUpdate;
    public final boolean lightsUpdate;
    public final List<String> pvpWorlds;
    public final boolean stopLeaving;
    public final boolean valuesMenu;
    public final int chunksPerTick;
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
    public final Registry<String, Location> islandPreviewLocations;
    public final boolean bankLogs;
    public final boolean bankInterestEnabled;
    public final int bankInterestInterval;
    public final int bankInterestPercentage;
    public final int bankInterestRecentActive;
    public final boolean tabCompleteHideVanished;
    public final boolean dropsUpgradePlayersMultiply;
    public final long protectedMessageDelay;
    public final boolean warpCategories;
    public final boolean physicsListener;
    public final double chargeOnWarp;
    public final boolean publicWarps;

    public SettingsHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);

        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        convertData(cfg);
        convertInteractables(plugin, cfg);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml",
                    "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases", "preview-islands");
        }catch (Exception ex){
            ex.printStackTrace();
        }

        databaseType = cfg.getString("database.type");
        databaseMySQLAddress = cfg.getString("database.address");
        databaseMySQLPort = cfg.getInt("database.port");
        databaseMySQLDBName = cfg.getString("database.db-name");
        databaseMySQLUsername = cfg.getString("database.user-name");
        databaseMySQLPassword = cfg.getString("database.password");
        databaseMySQLPrefix = cfg.getString("database.prefix");
        databaseMySQLSSL = cfg.getBoolean("database.useSSL");
        databaseMySQLPublicKeyRetrieval = cfg.getBoolean("database.allowPublicKeyRetrieval");

        calcInterval = cfg.getLong("calc-interval", 6000);
        islandCommand = cfg.getString("island-command", "island,is,islands");
        maxIslandSize = cfg.getInt("max-island-size", 200);
        defaultIslandSize = cfg.getInt("default-values.island-size", 20);
        defaultBlockLimits = new KeyMap<>();
        for(String line : cfg.getStringList("default-values.block-limits")){
            String[] sections = line.split(":");

            if(sections.length < 2){
                SuperiorSkyblockPlugin.log("&cCouldn't parse block limit '" + line + "', skipping...");
                continue;
            }

            String key = sections.length == 2 ? sections[0] : sections[0] + ":" + sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultBlockLimits.put(Key.of(key), new UpgradeValue<>(Integer.parseInt(limit), true));
        }
        defaultEntityLimits = new KeyMap<>();
        for(String line : cfg.getStringList("default-values.entity-limits")){
            String[] sections = line.split(":");

            if(sections.length < 2){
                SuperiorSkyblockPlugin.log("&cCouldn't parse entity limit '" + line + "', skipping...");
                continue;
            }

            String key = sections.length == 2 ? sections[0] : sections[0] + ":" + sections[1];
            String limit = sections.length == 2 ? sections[1] : sections[2];
            defaultEntityLimits.put(Key.of(key), new UpgradeValue<>(Integer.parseInt(limit), true));
        }
        defaultTeamLimit = cfg.getInt("default-values.team-limit", 4);
        defaultWarpsLimit = cfg.getInt("default-values.warps-limit", 3);
        defaultCoopLimit = cfg.getInt("default-values.coop-limit", 8);
        defaultCropGrowth = cfg.getInt("default-values.crop-growth", 1);
        defaultSpawnerRates = cfg.getDouble("default-values.spawner-rates", 1D);
        defaultMobDrops = cfg.getDouble("default-values.mob-drops", 1D);
        defaultBankLimit = new BigDecimal(cfg.getString("default-values.bank-limit", "-1"));
        defaultRoleLimits = new HashMap<>();
        for(String line : cfg.getStringList("default-values.role-limits")){
            String[] sections = line.split(":");
            try {
                defaultRoleLimits.put(Integer.parseInt(sections[0]), new UpgradeValue<>(Integer.parseInt(sections[1]), true));
            }catch (NumberFormatException ignored){}
        }
        islandsHeight = cfg.getInt("islands-height", 100);
        worldBordersEnabled = cfg.getBoolean("world-borders", true);
        stackedBlocksEnabled = cfg.getBoolean("stacked-blocks.enabled", true);
        stackedBlocksDisabledWorlds = cfg.getStringList("stacked-blocks.disabled-worlds");
        whitelistedStackedBlocks = new KeySet(cfg.getStringList("stacked-blocks.whitelisted"));
        stackedBlocksName = StringUtils.translateColors(cfg.getString("stacked-blocks.custom-name"));
        stackedBlocksLimits = new KeyMap<>();
        cfg.getStringList("stacked-blocks.limits").forEach(line -> {
            String[] sections = line.split(":");
            try {
                if (sections.length == 2)
                    stackedBlocksLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                else if (sections.length == 3)
                    stackedBlocksLimits.put(Key.of(sections[0] + ":" + sections[1]), Integer.parseInt(sections[2]));
            }catch(Exception ignored){}
        });
        stackedBlocksAutoPickup = cfg.getBoolean("stacked-blocks.auto-collect", false);
        stackedBlocksMenuEnabled = cfg.getBoolean("stacked-blocks.deposit-menu.enabled", true);
        stackedBlocksMenuTitle = StringUtils.translateColors(cfg.getString("stacked-blocks.deposit-menu.title", "&lDeposit Blocks"));
        islandLevelFormula = cfg.getString("island-level-formula", "{} / 2");
        roundedIslandLevel = cfg.getBoolean("rounded-island-level", false);
        islandTopOrder = cfg.getString("island-top-order", "WORTH");
        islandRolesSection = cfg.getConfigurationSection("island-roles");
        signWarpLine = cfg.getString("sign-warp-line", "[IslandWarp]");
        signWarp = StringUtils.translateColors(cfg.getStringList("sign-warp"));
        while(signWarp.size() < 4)
            signWarp.add("");
        visitorsSignLine = cfg.getString("visitors-sign.line", "[Welcome]");
        visitorsSignActive = StringUtils.translateColors(cfg.getString("visitors-sign.active", "&a[Welcome]"));
        visitorsSignInactive = StringUtils.translateColors(cfg.getString("visitors-sign.inactive", "&c[Welcome]"));
        int bankWorthRate = cfg.getInt("bank-worth-rate", 1000);
        this.bankWorthRate = bankWorthRate == 0 ? 0D : 1D / bankWorthRate;
        islandWorldName = cfg.getString("worlds.normal-world", "SuperiorWorld");
        netherWorldEnabled = cfg.getBoolean("worlds.nether.enabled", false);
        netherWorldUnlocked = cfg.getBoolean("worlds.nether.unlock", true);
        String netherWorldName = cfg.getString("worlds.nether.name", "");
        this.netherWorldName = netherWorldName.isEmpty() ? islandWorldName + "_nether" : netherWorldName;
        netherSchematicOffset = cfg.getBoolean("worlds.nether.schematic-offset", true);
        endWorldEnabled = cfg.getBoolean("worlds.end.enabled", false);
        endWorldUnlocked = cfg.getBoolean("worlds.end.unlock", false);
        String endWorldName = cfg.getString("worlds.end.name", "");
        this.endWorldName = endWorldName.isEmpty() ? islandWorldName + "_the_end" : endWorldName;
        endSchematicOffset = cfg.getBoolean("worlds.end.schematic-offset", true);
        endDragonFight = endWorldEnabled && cfg.getBoolean("worlds.end.dragon-fight", false) && ServerVersion.isAtLeast(ServerVersion.v1_9);
        optimizeWorlds = cfg.getBoolean("worlds.optimize", false);
        worldsDifficulty = cfg.getString("worlds.difficulty", "EASY");
        spawnLocation = cfg.getString("spawn.location", "SuperiorWorld, 0, 100, 0, 0, 0");
        spawnProtection = cfg.getBoolean("spawn.protection", true);
        spawnSettings = cfg.getStringList("spawn.settings");
        spawnPermissions = cfg.getStringList("spawn.permissions");
        spawnWorldBorder = cfg.getBoolean("spawn.world-border", false);
        spawnSize = cfg.getInt("spawn.size", 200);
        spawnDamage = cfg.getBoolean("spawn.players-damage", false);
        voidTeleportMembers = cfg.getBoolean("void-teleport.members", true);
        voidTeleportVisitors = cfg.getBoolean("void-teleport.visitors", true);
        interactables = loadInteractables(plugin);
        visitorsDamage = cfg.getBoolean("visitors-damage", false);
        coopDamage = cfg.getBoolean("coop-damage", true);
        disbandCount = cfg.getInt("disband-count", 5);
        islandTopIncludeLeader = cfg.getBoolean("island-top-include-leader", true);
        defaultPlaceholders = Registry.createRegistry(cfg.getStringList("default-placeholders").stream().collect(Collectors.toMap(
                line -> line.split(":")[0].replace("superior_", "").toLowerCase(),
                line -> line.split(":")[1]
        )));
        banConfirm = cfg.getBoolean("ban-confirm");
        disbandConfirm = cfg.getBoolean("disband-confirm");
        kickConfirm = cfg.getBoolean("kick-confirm");
        leaveConfirm = cfg.getBoolean("leave-confirm");
        spawnersProvider = cfg.getString("spawners-provider", "AUTO");
        disbandInventoryClear = cfg.getBoolean("disband-inventory-clear", true);
        disbandRefund = Math.max(0, Math.min(100, cfg.getDouble("disband-refund"))) / 100D;
        islandNamesRequiredForCreation = cfg.getBoolean("island-names.required-for-creation", true);
        islandNamesMaxLength = cfg.getInt("island-names.max-length", 16);
        islandNamesMinLength = cfg.getInt("island-names.min-length", 3);
        filteredIslandNames = cfg.getStringList("island-names.filtered-names");
        islandNamesColorSupport = cfg.getBoolean("island-names.color-support", true);
        islandNamesIslandTop = cfg.getBoolean("island-names.island-top", true);
        islandNamesPreventPlayerNames = cfg.getBoolean("island-names.prevent-player-names", true);
        teleportOnJoin = cfg.getBoolean("teleport-on-join", false);
        teleportOnKick = cfg.getBoolean("teleport-on-kick", false);
        clearOnJoin = cfg.getBoolean("clear-on-join", false);
        rateOwnIsland = cfg.getBoolean("rate-own-island", false);
        defaultSettings = cfg.getStringList("default-settings");
        defaultGenerator = new KeyMap[3];
        if(cfg.isConfigurationSection("default-values.generator")){
            for(String env : cfg.getConfigurationSection("default-values.generator").getKeys(false)){
                try{
                    World.Environment environment = World.Environment.valueOf(env.toUpperCase());
                    loadGenerator(cfg.getStringList("default-values.generator." + env), environment.ordinal());
                }catch (Exception ignored){}
            }
        }
        else {
            loadGenerator(cfg.getStringList("default-values.generator"), 0);
        }
        disableRedstoneOffline = cfg.getBoolean("disable-redstone-offline", true);
        disableRedstoneAFK = cfg.getBoolean("afk-integrations.disable-redstone", false);
        disableSpawningAFK = cfg.getBoolean("afk-integrations.disable-spawning", true);
        commandsCooldown = Registry.createRegistry();
        for(String subCommand : cfg.getConfigurationSection("commands-cooldown").getKeys(false)){
            int cooldown = cfg.getInt("commands-cooldown." + subCommand + ".cooldown");
            String permission = cfg.getString("commands-cooldown." + subCommand + ".bypass-permission");
            commandsCooldown.add(subCommand, new Pair<>(cooldown, permission));
        }
        upgradeCooldown = cfg.getLong("upgrade-cooldown", -1L);
        numberFormat = cfg.getString("number-format", "en-US");
        StringUtils.setNumberFormatter(numberFormat);
        dateFormat = cfg.getString("date-format", "dd/MM/yyyy HH:mm:ss");
        StringUtils.setDateFormatter(dateFormat);
        skipOneItemMenus = cfg.getBoolean("skip-one-item-menus", false);
        teleportOnPVPEnable = cfg.getBoolean("teleport-on-pvp-enable", true);
        immuneToPVPWhenTeleport = cfg.getBoolean("immune-to-pvp-when-teleport", true);
        blockedVisitorsCommands = cfg.getStringList("blocked-visitors-commands");
        defaultContainersEnabled = cfg.getBoolean("default-containers.enabled", false);
        defaultContainersContents = Registry.createRegistry();
        if(cfg.contains("default-containers.containers")) {
            for (String container : cfg.getConfigurationSection("default-containers.containers").getKeys(false)) {
                try {
                    InventoryType containerType = InventoryType.valueOf(container.toUpperCase());
                    ListTag items = new ListTag(CompoundTag.class, new ArrayList<>());
                    defaultContainersContents.add(containerType, items);

                    ConfigurationSection containerSection = cfg.getConfigurationSection("default-containers.containers." + container);
                    for (String slot : containerSection.getKeys(false)) {
                        try {
                            // Reading the item from the config
                            ItemStack itemStack = FileUtils.getItemStack("config.yml", containerSection.getConfigurationSection(slot)).build();
                            itemStack.setAmount(containerSection.getInt(slot + ".amount", 1));

                            // Parsing it into compound tag
                            CompoundTag itemCompound = plugin.getNMSAdapter().getNMSCompound(itemStack);
                            itemCompound.setByte("Slot", Byte.parseByte(slot));

                            items.addTag(itemCompound);
                        } catch (Exception ignored) {}
                    }
                } catch (IllegalArgumentException ex) {
                    SuperiorSkyblockPlugin.log("&cInvalid container type: " + container + ".");
                }
            }
        }
        defaultSignLines = cfg.getStringList("default-signs");
        eventCommands = Registry.createRegistry();
        if(cfg.contains("event-commands")) {
            for (String eventName : cfg.getConfigurationSection("event-commands").getKeys(false)) {
                eventCommands.add(eventName.toLowerCase(), cfg.getStringList("event-commands." + eventName));
            }
        }
        warpsWarmup = cfg.getLong("warps-warmup", 0);
        homeWarmup = cfg.getLong("home-warmup", 0);
        liquidUpdate = cfg.getBoolean("liquid-update", false);
        lightsUpdate = cfg.getBoolean("lights-update", false);
        pvpWorlds = cfg.getStringList("pvp-worlds");
        stopLeaving = cfg.getBoolean("stop-leaving", false);
        valuesMenu = cfg.getBoolean("values-menu", true);
        chunksPerTick = cfg.getInt("chunks-per-tick", 10);
        cropsToGrow = cfg.getStringList("crops-to-grow");
        cropsInterval = cfg.getInt("crops-interval", 5);
        onlyBackButton = cfg.getBoolean("only-back-button", false);
        buildOutsideIsland = cfg.getBoolean("build-outside-island", false);
        defaultLanguage = cfg.getString("default-language", "en-US");
        defaultWorldBorder = cfg.getBoolean("default-world-border", true);
        defaultBlocksStacker = cfg.getBoolean("default-blocks-stacker", true);
        defaultToggledPanel = cfg.getBoolean("default-toggled-panel", false);
        defaultIslandFly = cfg.getBoolean("default-island-fly", false);
        defaultBorderColor = cfg.getString("default-border-color", "BLUE");
        generators = cfg.getBoolean("generators", true);
        obsidianToLava = cfg.getBoolean("obsidian-to-lava", false);
        syncWorth = BlockValuesHandler.SyncWorthStatus.of(cfg.getString("sync-worth", "NONE"));
        negativeWorth = cfg.getBoolean("negative-worth", true);
        negativeLevel = cfg.getBoolean("negative-level", true);
        disabledEvents = cfg.getStringList("disabled-events").stream().map(String::toLowerCase).collect(Collectors.toList());
        schematicNameArgument = cfg.getBoolean("schematic-name-argument", true);
        islandChestTitle = StringUtils.translateColors(cfg.getString("island-chests.chest-title", "&4Island Chest"));
        islandChestsDefaultPage = cfg.getInt("island-chests.default-pages", 0);
        islandChestsDefaultSize = cfg.getInt("island-chests.default-size", 3);
        commandAliases = new HashMap<>();
        if(cfg.isConfigurationSection("command-aliases")){
            for(String label : cfg.getConfigurationSection("command-aliases").getKeys(false)){
                commandAliases.put(label.toLowerCase(), cfg.getStringList("command-aliases." + label));
            }
        }
        valuableBlocks = new KeySet(cfg.getStringList("valuable-blocks"));
        islandPreviewLocations = Registry.createRegistry();
        if(cfg.isConfigurationSection("preview-islands")){
            for(String schematic : cfg.getConfigurationSection("preview-islands").getKeys(false))
                islandPreviewLocations.add(schematic.toLowerCase(), LocationUtils.getLocation(cfg.getString("preview-islands." + schematic)));
        }
        bankLogs = cfg.getBoolean("bank-logs", true);
        bankInterestEnabled = cfg.getBoolean("bank-interest.enabled", true);
        bankInterestInterval = cfg.getInt("bank-interest.interval", 86400);
        bankInterestPercentage = cfg.getInt("bank-interest.percentage", 10);
        bankInterestRecentActive = cfg.getInt("bank-interest.recent-active", 86400);
        tabCompleteHideVanished = cfg.getBoolean("tab-complete-hide-vanished", true);
        dropsUpgradePlayersMultiply = cfg.getBoolean("drops-upgrade-players-multiply", false);
        protectedMessageDelay = cfg.getLong("protected-message-delay", 60L);
        warpCategories = cfg.getBoolean("warp-categories", true);
        physicsListener = cfg.getBoolean("physics-listener", true);
        chargeOnWarp = cfg.getDouble("charge-on-warp", 0D);
        publicWarps = cfg.getBoolean("public-warps");
    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException("Not supported for SettingsHandler");
    }

    public void updateValue(String path, Object value) throws IOException {
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml",
                "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases", "preview-islands");

        cfg.set(path, value);

        cfg.save(file);

        plugin.setSettings(new SettingsHandler(plugin));
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
        if(cfg.contains("spawn-location"))
            cfg.set("spawn.location", cfg.getString("spawn-location"));
        if(cfg.contains("spawn-protection"))
            cfg.set("spawn.protection", cfg.getBoolean("spawn-protection"));
        if(cfg.getBoolean("spawn-pvp", false))
            cfg.set("spawn.settings", Collections.singletonList("PVP"));
        if(cfg.contains("island-world"))
            cfg.set("worlds.normal-world", cfg.getString("island-world"));
        if(cfg.contains("welcome-sign-line"))
            cfg.set("visitors-sign.line", cfg.getString("welcome-sign-line"));
        if(cfg.contains("island-roles.ladder")){
            for(String name : cfg.getConfigurationSection("island-roles.ladder").getKeys(false)){
                if(!cfg.contains("island-roles.ladder." + name + ".id"))
                    cfg.set("island-roles.ladder." + name + ".id", cfg.getInt("island-roles.ladder." + name + ".weight"));
            }
        }
        if(cfg.contains("default-island-size"))
            cfg.set("default-values.island-size", cfg.getInt("default-island-size"));
        if(cfg.contains("default-limits"))
            cfg.set("default-values.block-limits", cfg.getStringList("default-limits"));
        if(cfg.contains("default-entity-limits"))
            cfg.set("default-values.entity-limits", cfg.getStringList("default-entity-limits"));
        if(cfg.contains("default-warps-limit"))
            cfg.set("default-values.warps-limit", cfg.getInt("default-warps-limit"));
        if(cfg.contains("default-team-limit"))
            cfg.set("default-values.team-limit", cfg.getInt("default-team-limit"));
        if(cfg.contains("default-crop-growth"))
            cfg.set("default-values.crop-growth", cfg.getInt("default-crop-growth"));
        if(cfg.contains("default-spawner-rates"))
            cfg.set("default-values.spawner-rates", cfg.getInt("default-spawner-rates"));
        if(cfg.contains("default-mob-drops"))
            cfg.set("default-values.mob-drops", cfg.getInt("default-mob-drops"));
        if(cfg.contains("default-island-height"))
            cfg.set("islands-height", cfg.getInt("default-island-height"));
        if(cfg.contains("starter-chest")){
            cfg.set("default-containers.enabled", cfg.getBoolean("starter-chest.enabled"));
            cfg.set("default-containers.containers.chest", cfg.getConfigurationSection("starter-chest.contents"));
        }
        if(cfg.contains("default-generator"))
            cfg.set("default-values.generator", cfg.getStringList("default-generator"));
        if(cfg.isBoolean("void-teleport")){
            boolean voidTeleport = cfg.getBoolean("void-teleport");
            cfg.set("void-teleport.members", voidTeleport);
            cfg.set("void-teleport.visitors", voidTeleport);
        }
        if(cfg.isBoolean("sync-worth"))
            cfg.set("sync-worth", cfg.getBoolean("sync-worth") ? "BUY" : "NONE");
        if(!cfg.contains("worlds.nether")){
            cfg.set("worlds.nether.enabled", cfg.getBoolean("worlds.nether-world"));
            cfg.set("worlds.nether.unlock", cfg.getBoolean("worlds.nether-unlock"));
        }
        if(!cfg.contains("worlds.end")){
            cfg.set("worlds.end.enabled", cfg.getBoolean("worlds.end-world"));
            cfg.set("worlds.end.unlock", cfg.getBoolean("worlds.end-unlock"));
        }
    }

    private void convertInteractables(SuperiorSkyblockPlugin plugin, YamlConfiguration cfg){
        if(!cfg.contains("interactables"))
            return;

        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if(!file.exists())
            plugin.saveResource("interactables.yml", false);

        CommentedConfiguration commentedConfig = CommentedConfiguration.loadConfiguration(file);

        commentedConfig.set("interactables", cfg.getStringList("interactables"));

        try {
            commentedConfig.save(file);
        }catch (Exception ex){
            ex.printStackTrace();
        }
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
            String key = sections.length == 2 ? sections[0] : sections[0] + sections[1];
            String percentage = sections.length == 2 ? sections[1] : sections[2];
            defaultGenerator[index].put(key, new UpgradeValue<>(Integer.parseInt(percentage), true));
        }
    }

}
