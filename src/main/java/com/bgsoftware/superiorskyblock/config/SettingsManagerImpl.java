package com.bgsoftware.superiorskyblock.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.player.inventory.ClearAction;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.config.section.AFKIntegrationsSection;
import com.bgsoftware.superiorskyblock.config.section.DatabaseSection;
import com.bgsoftware.superiorskyblock.config.section.DefaultContainersSection;
import com.bgsoftware.superiorskyblock.config.section.DefaultValuesSection;
import com.bgsoftware.superiorskyblock.config.section.GlobalSection;
import com.bgsoftware.superiorskyblock.config.section.IslandChestsSection;
import com.bgsoftware.superiorskyblock.config.section.IslandNamesSection;
import com.bgsoftware.superiorskyblock.config.section.IslandPreviewsSection;
import com.bgsoftware.superiorskyblock.config.section.IslandRolesSection;
import com.bgsoftware.superiorskyblock.config.section.SpawnSection;
import com.bgsoftware.superiorskyblock.config.section.StackedBlocksSection;
import com.bgsoftware.superiorskyblock.config.section.VisitorsSignSection;
import com.bgsoftware.superiorskyblock.config.section.VoidTeleportSection;
import com.bgsoftware.superiorskyblock.config.section.WorldsSection;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.player.inventory.ClearActions;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class SettingsManagerImpl extends Manager implements SettingsManager {

    private static final String[] IGNORED_SECTIONS = new String[]{
            "config.yml", "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases", "worlds.dimensions",
            "island-previews.locations", "default-values.block-limits", "default-values.entity-limits",
            "default-values.role-limits", "stacked-blocks.limits", "default-values.generator", "message-delays", "default-placeholders"
    };

    private final GlobalSection global = new GlobalSection();
    private final DatabaseSection database = new DatabaseSection();
    private final DefaultValuesSection defaultValues = new DefaultValuesSection();
    private final StackedBlocksSection stackedBlocks = new StackedBlocksSection();
    private final IslandRolesSection islandRoles = new IslandRolesSection();
    private final VisitorsSignSection visitorsSign = new VisitorsSignSection();
    private final WorldsSection worlds = new WorldsSection();
    private final SpawnSection spawn = new SpawnSection();
    private final VoidTeleportSection voidTeleport = new VoidTeleportSection();
    private final IslandNamesSection islandNames = new IslandNamesSection();
    private final AFKIntegrationsSection afkIntegrations = new AFKIntegrationsSection();
    private final DefaultContainersSection defaultContainers = new DefaultContainersSection();
    private final IslandChestsSection islandChests = new IslandChestsSection();
    private final IslandPreviewsSection islandPreviews = new IslandPreviewsSection();

    public SettingsManagerImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() throws ManagerLoadException {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(file);

        boolean forceSave = convertData(config);

        if (convertInteractables(plugin, config)) {
            forceSave = true;
        }
        if (convertEntityCategories(plugin, config)) {
            forceSave = true;
        }
        if (convertBankModule(plugin, config)) {
            forceSave = true;
        }
        if (convertGeneratorsModule(plugin, config)) {
            forceSave = true;
        }
        if (convertUpgradesModule(plugin, config)) {
            forceSave = true;
        }

        if (forceSave) {
            try {
                config.save(file);
            } catch (Exception error) {
                Log.errorFromFile(error, file.getName(), "An unexpected error occurred while saving config file:");
            }
        }

        try {
            config.syncWithConfig(file, plugin.getResource("config.yml"), IGNORED_SECTIONS);
        } catch (Exception error) {
            Log.error(error, file, "An unexpected error occurred while loading config file:");
        }

        loadContainerFromConfig(config);

        PluginEventsFactory.callSettingsUpdateEvent();
    }

    @Override
    public long getCalcInterval() {
        return this.global.getCalcInterval();
    }

    @Override
    public Database getDatabase() {
        return this.database;
    }

    @Override
    public String getIslandCommand() {
        return this.global.getIslandCommand();
    }

    @Override
    public int getMaxIslandSize() {
        return this.global.getMaxIslandSize();
    }

    @Override
    public DefaultValuesSection getDefaultValues() {
        return this.defaultValues;
    }

    @Override
    public int getIslandHeight() {
        return this.global.getIslandHeight();
    }

    @Override
    public boolean isWorldBorders() {
        return this.global.isWorldBorders();
    }

    @Override
    public StackedBlocks getStackedBlocks() {
        return this.stackedBlocks;
    }

    @Override
    public String getIslandLevelFormula() {
        return this.global.getBlockLevelFormula();
    }

    @Override
    public boolean isRoundedIslandLevels() {
        return this.global.isRoundedIslandLevels();
    }

    @Override
    public RoundingMode getIslandLevelRoundingMode() {
        return this.global.getIslandLevelRoundingMode();
    }

    @Override
    public boolean isAutoBlocksTracking() {
        return this.global.isAutoBlocksTracking();
    }

    @Override
    public String getIslandTopOrder() {
        return this.global.getIslandTopOrder().getName();
    }

    @Override
    public String getGlobalWarpsOrder() {
        return this.global.getGlobalWarpsOrder().getName();
    }

    @Override
    public boolean isCoopMembers() {
        return this.global.isCoopMembers();
    }

    @Override
    public boolean isEditPlayerPermissions() {
        return this.global.isEditPlayerPermissions();
    }

    @Override
    public IslandRoles getIslandRoles() {
        return this.islandRoles;
    }

    @Override
    public String getSignWarpLine() {
        return this.global.getSignWarpLine();
    }

    @Override
    public List<String> getSignWarp() {
        return this.global.getSignWarp();
    }

    @Override
    public VisitorsSign getVisitorsSign() {
        return this.visitorsSign;
    }

    @Override
    public Worlds getWorlds() {
        return this.worlds;
    }

    @Override
    public Spawn getSpawn() {
        return this.spawn;
    }

    @Override
    public Collection<String> getWorldPermissions() {
        return this.global.getWorldPermissions();
    }

    @Override
    public VoidTeleport getVoidTeleport() {
        return this.voidTeleport;
    }

    @Override
    public List<String> getInteractables() {
        List<String> interactables = new LinkedList<>();
        for (Key key : getInteractablesMap().getInteractables()) {
            interactables.add(key.toString());
        }
        return interactables.isEmpty() ? Collections.emptyList() : interactables;
    }

    @Override
    public Interactables getInteractablesMap() {
        return this.global.getInteractablesMap();
    }

    @Override
    public Collection<Key> getSafeBlocks() {
        return this.global.getSafeBlocks();
    }

    @Override
    public boolean isVisitorsDamage() {
        return this.global.isVisitorsDamage();
    }

    @Override
    public boolean isCoopDamage() {
        return this.global.isCoopDamage();
    }

    @Override
    public int getDisbandCount() {
        return this.global.getDisbandCount();
    }

    @Override
    public boolean isIslandTopIncludeLeader() {
        return this.global.isIslandTopIncludeLeader();
    }

    @Override
    public Map<String, String> getDefaultPlaceholders() {
        return this.global.getDefaultPlaceholders();
    }

    @Override
    public boolean isBanConfirm() {
        return this.global.isBanConfirm();
    }

    @Override
    public boolean isDisbandConfirm() {
        return this.global.isDisbandConfirm();
    }

    @Override
    public boolean isKickConfirm() {
        return this.global.isKickConfirm();
    }

    @Override
    public boolean isLeaveConfirm() {
        return this.global.isLeaveConfirm();
    }

    @Override
    public boolean isTransferConfirm() {
        return this.global.isTransferConfirm();
    }

    @Override
    public boolean isDisbandInventoryClear() {
        List<ClearAction> clearActions = this.global.getClearActionsOnDisband();
        return clearActions.contains(ClearActions.ENDER_CHEST) && clearActions.contains(ClearActions.INVENTORY);
    }

    @Override
    public IslandNames getIslandNames() {
        return this.islandNames;
    }

    @Override
    public boolean isTeleportOnCreate() {
        return this.global.isTeleportOnCreate();
    }

    @Override
    public boolean isTeleportOnJoin() {
        return this.global.isTeleportOnJoin();
    }

    @Override
    public boolean isTeleportOnKick() {
        return this.global.isTeleportOnKick();
    }

    @Override
    public boolean isTeleportOnLeave() {
        return this.global.isTeleportOnLeave();
    }

    @Override
    public boolean isClearOnJoin() {
        List<ClearAction> clearActions = this.global.getClearActionsOnJoin();
        return clearActions.contains(ClearActions.ENDER_CHEST) && clearActions.contains(ClearActions.INVENTORY);
    }

    @Override
    public List<ClearAction> getClearActionsOnDisband() {
        return this.global.getClearActionsOnDisband();
    }

    @Override
    public List<ClearAction> getClearActionsOnJoin() {
        return this.global.getClearActionsOnJoin();
    }

    @Override
    public List<ClearAction> getClearActionsOnKick() {
        return this.global.getClearActionsOnKick();
    }

    @Override
    public List<ClearAction> getClearActionsOnLeave() {
        return this.global.getClearActionsOnLeave();
    }

    @Override
    public boolean isRateOwnIsland() {
        return this.global.isRateOwnIsland();
    }

    @Override
    public boolean isChangeIslandRating() {
        return this.global.isChangeIslandRating();
    }

    @Override
    public List<String> getDefaultSettings() {
        return this.global.getDefaultSettings();
    }

    @Override
    public boolean isDisableRedstoneOffline() {
        return this.global.isDisableRedstoneOffline();
    }

    @Override
    public AFKIntegrations getAFKIntegrations() {
        return this.afkIntegrations;
    }

    @Override
    public Map<String, Pair<Integer, String>> getCommandsCooldown() {
        return this.global.getCommandsCooldown();
    }

    @Override
    public long getUpgradeCooldown() {
        return this.global.getUpgradeCooldown();
    }

    @Override
    public String getNumbersFormat() {
        return this.global.getNumbersFormat();
    }

    @Override
    public String getDateFormat() {
        return this.global.getDateFormat();
    }

    @Override
    public boolean isSkipOneItemMenus() {
        return this.global.isSkipOneItemMenus();
    }

    @Override
    public boolean isTeleportOnPvPEnable() {
        return this.global.isTeleportOnPvPEnable();
    }

    @Override
    public boolean isImmuneToPvPWhenTeleport() {
        return this.global.isImmuneToPvPWhenTeleport();
    }

    @Override
    public List<String> getBlockedVisitorsCommands() {
        return this.global.getBlockedVisitorsCommands();
    }

    @Override
    public DefaultContainersSection getDefaultContainers() {
        return this.defaultContainers;
    }

    @Override
    public List<String> getDefaultSign() {
        return this.global.getDefaultSign();
    }

    @Override
    public Map<String, List<String>> getEventCommands() {
        return this.global.getEventCommands();
    }

    @Override
    public long getWarpsWarmup() {
        return this.global.getWarpsWarmup();
    }

    @Override
    public long getHomeWarmup() {
        return this.global.getHomeWarmup();
    }

    @Override
    public long getVisitWarmup() {
        return this.global.getVisitWarmup();
    }

    @Override
    public boolean isLiquidUpdate() {
        return this.global.isLiquidUpdate();
    }

    @Override
    public boolean isLightsUpdate() {
        return this.global.isLightsUpdate();
    }

    @Override
    public List<String> getPvPWorlds() {
        return this.global.getPvPWorlds();
    }

    @Override
    public boolean isStopLeaving() {
        return this.global.isStopLeaving();
    }

    @Override
    public boolean isValuesMenu() {
        return this.global.isValuesMenu();
    }

    @Override
    @Deprecated
    public List<String> getCropsToGrow() {
        List<String> list = new ArrayList<>();
        for (Key key : BuiltinModules.UPGRADES.getConfiguration().getCropGrowthWhitelistedCrops()) {
            list.add(key.toString());
        }

        return Collections.unmodifiableList(list);
    }

    @Override
    @Deprecated
    public int getCropsInterval() {
        return BuiltinModules.UPGRADES.getConfiguration().getCropGrowthInterval();
    }

    @Override
    public boolean isOnlyBackButton() {
        return this.global.isOnlyBackButton();
    }

    @Override
    public boolean isBuildOutsideIsland() {
        return this.global.isBuildOutsideIsland();
    }

    @Override
    public String getDefaultLanguage() {
        return this.global.getDefaultLanguage();
    }

    @Override
    public boolean isDefaultWorldBorder() {
        return this.global.isDefaultWorldBorder();
    }

    @Override
    public boolean isDefaultStackedBlocks() {
        return this.global.isDefaultStackedBlocks();
    }

    @Override
    public boolean isDefaultToggledPanel() {
        return this.global.isDefaultToggledPanel();
    }

    @Override
    public boolean isDefaultIslandFly() {
        return this.global.isDefaultIslandFly();
    }

    @Override
    public String getDefaultBorderColor() {
        return this.global.getDefaultBorderColor();
    }

    @Override
    public boolean isObsidianToLava() {
        return this.global.isObsidianToLava();
    }

    @Override
    public String getSpawnersProvider() {
        return this.global.getSpawnersProvider();
    }

    @Override
    public String getStackedBlocksProvider() {
        return this.global.getStackedBlocksProvider();
    }

    @Override
    public String getPricesProvider() {
        return this.global.getPricesProvider();
    }

    @Override
    public BlockValuesManager.SyncWorthStatus getSyncWorth() {
        return this.global.getSyncWorth();
    }

    @Override
    public boolean isNegativeWorth() {
        return this.global.isNegativeWorth();
    }

    @Override
    public boolean isNegativeLevel() {
        return this.global.isNegativeLevel();
    }

    @Override
    public List<String> getDisabledEvents() {
        return this.global.getDisabledEvents();
    }

    @Override
    public List<String> getDisabledCommands() {
        return this.global.getDisabledCommands();
    }

    @Override
    public List<String> getDisabledHooks() {
        return this.global.getDisabledHooks();
    }

    @Override
    public boolean isSchematicNameArgument() {
        return this.global.isSchematicNameArgument();
    }

    @Override
    public IslandChests getIslandChests() {
        return this.islandChests;
    }

    @Override
    public Map<String, List<String>> getCommandAliases() {
        return this.global.getCommandAliases();
    }

    @Override
    public Set<Key> getValuableBlocks() {
        return this.global.getValuableBlocks();
    }

    @Override
    @Deprecated
    public Map<String, Location> getPreviewIslands() {
        return this.islandPreviews.getLocations();
    }

    @Override
    public IslandPreviewsSection getIslandPreviews() {
        return this.islandPreviews;
    }

    @Override
    public boolean isTabCompleteHideVanished() {
        return this.global.isTabCompleteHideVanished();
    }

    @Override
    @Deprecated
    public boolean isDropsUpgradePlayersMultiply() {
        return BuiltinModules.UPGRADES.getConfiguration().isMobDropsOnlyPlayerKills();
    }

    @Override
    @Deprecated
    public long getProtectedMessageDelay() {
        return this.global.getMessageDelays().getOrDefault("ISLAND_PROTECTED", 0L);
    }

    @Override
    public Map<String, Long> getMessageDelays() {
        return this.global.getMessageDelays();
    }

    @Override
    public boolean isWarpCategories() {
        return this.global.isWarpCategories();
    }

    @Override
    public boolean isPhysicsListener() {
        return this.global.isPhysicsListener();
    }

    @Override
    public double getChargeOnWarp() {
        return this.global.getChargeOnWarp();
    }

    @Override
    public boolean isPublicWarps() {
        return this.global.isPublicWarps();
    }

    @Override
    public boolean isLockedIslands() {
        return this.global.isLockedIslands();
    }

    @Override
    public long getRecalcTaskTimeout() {
        return this.global.getRecalcTaskTimeout();
    }

    @Override
    public boolean isAutoLanguageDetection() {
        return this.global.isAutoLanguageDetection();
    }

    @Override
    public boolean isAutoUncoopWhenAlone() {
        return this.global.isAutoUncoopWhenAlone();
    }

    @Override
    public TopIslandMembersSorting getTopIslandMembersSorting() {
        return this.global.getTopIslandMembersSorting();
    }

    @Override
    public int getBossbarLimit() {
        return this.global.getBossbarLimit();
    }

    @Override
    public boolean getDeleteUnsafeWarps() {
        return this.global.getDeleteUnsafeWarps();
    }

    @Override
    public List<RespawnAction> getPlayerRespawn() {
        return this.global.getPlayerRespawn();
    }

    @Override
    public BigInteger getBlockCountsSaveThreshold() {
        return this.global.getBlockCountsSaveThreshold();
    }

    @Override
    public boolean getChatSigningSupport() {
        return this.global.getChatSigningSupport();
    }

    @Override
    public int getCommandsPerPage() {
        return this.global.getCommandsPerPage();
    }

    @Override
    public boolean isHelpOnInvalidCommand() {
        return this.global.isHelpOnInvalidCommand();
    }

    @Override
    public boolean isHelpOnNoPermission() {
        return this.global.isHelpOnNoPermission();
    }

    @Override
    public boolean isCacheSchematics() {
        return this.global.isCacheSchematics();
    }

    @Override
    public Map<String, KeySet> getEntityCategories() {
        Map<String, KeySet> categories = new HashMap<>();
        for (EntityCategory entityCategory : getEntityCategoriesMap().getCategories()) {
            categories.put(entityCategory.getName(), entityCategory.getEntities());
        }
        return categories.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(categories);
    }

    @Override
    public EntityCategories getEntityCategoriesMap() {
        return this.global.getEntityCategoriesMap();
    }

    public void updateValue(String path, Object value) throws IOException {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml",
                "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases", "island-previews.locations", "worlds.dimensions");

        cfg.set(path, value);

        cfg.save(file);

        try {
            loadContainerFromConfig(cfg);
        } catch (ManagerLoadException ex) {
            ManagerLoadException.handle(ex);
        }
    }

    private void loadContainerFromConfig(YamlConfiguration config) throws ManagerLoadException {
        SettingsContainer container = new SettingsContainer(plugin, config);
        this.global.setContainer(container);
        this.database.setContainer(container);
        this.defaultValues.setContainer(container);
        this.stackedBlocks.setContainer(container);
        this.islandRoles.setContainer(container);
        this.visitorsSign.setContainer(container);
        this.worlds.setContainer(container);
        this.spawn.setContainer(container);
        this.voidTeleport.setContainer(container);
        this.islandNames.setContainer(container);
        this.afkIntegrations.setContainer(container);
        this.defaultContainers.setContainer(container);
        this.islandChests.setContainer(container);
        this.islandPreviews.setContainer(container);
    }

    private boolean convertData(YamlConfiguration config) {
        // If we don't change the path but only the format of the existing one,
        // we must force a save, because syncWithConfig() won't detect it.
        boolean forceSave = false;

        if (config.get("island-level-formula") != null) {
            config.set("block-level-formula", config.getString("island-level-formula"));
            config.set("island-level-formula", null);
        }
        if (config.get("protected-message-delay") instanceof Number) {
            long delay = config.getLong("protected-message-delay") * 50;
            config.set("message-delays.ISLAND_PROTECTED", delay);
            config.set("message-delays.ISLAND_PROTECTED_OPPED", delay);
            config.set("message-delays.SPAWN_PROTECTED", delay);
            config.set("message-delays.SPAWN_PROTECTED_OPPED", delay);
            config.set("protected-message-delay", null);
        }
        if (config.isConfigurationSection("preview-islands")) {
            config.set("island-previews.locations", config.getConfigurationSection("preview-islands"));
            config.set("preview-islands", null);
        }
        if (config.isBoolean("disband-inventory-clear")) {
            if (config.getBoolean("disband-inventory-clear")) {
                config.set("clear-on-disband", Arrays.asList("ENDER_CHEST", "INVENTORY"));
            } else {
                config.set("clear-on-disband", Collections.emptyList());
            }
            config.set("disband-inventory-clear", null);
        }
        if (config.isBoolean("clear-on-join")) {
            if (config.getBoolean("disband-inventory-clear")) {
                config.set("clear-on-join", Arrays.asList("ENDER_CHEST", "INVENTORY"));
            } else {
                config.set("clear-on-join", Collections.emptyList());
            }
        }
        if (config.isInt("disband-count")) {
            config.set("default-disband-count", config.getInt("disband-count") == 0 ? -1 : config.getInt("disband-count"));
            config.set("disband-count", null);
        }
        if (config.isInt("default-hoppers-limit")) {
            config.set("default-limits", Collections.singletonList("HOPPER:" + config.getInt("default-hoppers-limit")));
            config.set("default-hoppers-limit", null);
        }
        if (config.isConfigurationSection("default-permissions")) {
            config.set("island-roles.guest.name", "Guest");
            config.set("island-roles.guest.permissions", config.getStringList("default-permissions.guest"));
            config.set("island-roles.ladder.member.name", "Member");
            config.set("island-roles.ladder.member.weight", 0);
            config.set("island-roles.ladder.member.permissions", config.getStringList("default-permissions.member"));
            config.set("island-roles.ladder.mod.name", "Moderator");
            config.set("island-roles.ladder.mod.weight", 1);
            config.set("island-roles.ladder.mod.permissions", config.getStringList("default-permissions.mod"));
            config.set("island-roles.ladder.admin.name", "Admin");
            config.set("island-roles.ladder.admin.weight", 2);
            config.set("island-roles.ladder.admin.permissions", config.getStringList("default-permissions.admin"));
            config.set("island-roles.ladder.leader.name", "Leader");
            config.set("island-roles.ladder.leader.weight", 3);
            config.set("island-roles.ladder.leader.permissions", config.getStringList("default-permissions.leader"));
        }
        if (config.isString("spawn-location"))
            config.set("spawn.location", config.getString("spawn-location"));
        if (config.isBoolean("spawn-protection"))
            config.set("spawn.protection", config.getBoolean("spawn-protection"));
        if (config.getBoolean("spawn-pvp", false))
            config.set("spawn.settings", Collections.singletonList("PVP"));
        if (config.isString("island-world"))
            config.set("worlds.normal-world", config.getString("island-world"));
        if (config.isString("welcome-sign-line"))
            config.set("visitors-sign.line", config.getString("welcome-sign-line"));
        if (config.isConfigurationSection("island-roles.ladder")) {
            for (String name : config.getConfigurationSection("island-roles.ladder").getKeys(false)) {
                if (!config.isInt("island-roles.ladder." + name + ".id"))
                    config.set("island-roles.ladder." + name + ".id", config.getInt("island-roles.ladder." + name + ".weight"));
            }
        }
        if (config.isInt("default-island-size"))
            config.set("default-values.island-size", config.getInt("default-island-size"));
        if (config.isList("default-limits"))
            config.set("default-values.block-limits", config.getStringList("default-limits"));
        if (config.isList("default-entity-limits"))
            config.set("default-values.entity-limits", config.getStringList("default-entity-limits"));
        if (config.isInt("default-warps-limit"))
            config.set("default-values.warps-limit", config.getInt("default-warps-limit"));
        if (config.isInt("default-team-limit"))
            config.set("default-values.team-limit", config.getInt("default-team-limit"));
        if (config.isInt("default-crop-growth"))
            config.set("default-values.crop-growth", config.getInt("default-crop-growth"));
        if (config.isInt("default-spawner-rates"))
            config.set("default-values.spawner-rates", config.getInt("default-spawner-rates"));
        if (config.isInt("default-mob-drops"))
            config.set("default-values.mob-drops", config.getInt("default-mob-drops"));
        if (config.isInt("default-island-height"))
            config.set("islands-height", config.getInt("default-island-height"));
        if (config.isConfigurationSection("starter-chest")) {
            config.set("default-containers.enabled", config.getBoolean("starter-chest.enabled"));
            config.set("default-containers.containers.chest", config.getConfigurationSection("starter-chest.contents"));
        }
        if (config.isList("default-generator"))
            config.set("default-values.generator", config.getStringList("default-generator"));
        if (config.isBoolean("void-teleport")) {
            boolean voidTeleport = config.getBoolean("void-teleport");
            config.set("void-teleport.members", voidTeleport);
            config.set("void-teleport.visitors", voidTeleport);
        }
        if (config.isBoolean("sync-worth"))
            config.set("sync-worth", config.getBoolean("sync-worth") ? "BUY" : "NONE");
        if (!config.isConfigurationSection("worlds.nether")) {
            config.set("worlds.nether.enabled", config.getBoolean("worlds.nether-world"));
            config.set("worlds.nether.unlock", config.getBoolean("worlds.nether-unlock"));
        }
        if (!config.isConfigurationSection("worlds.end")) {
            config.set("worlds.end.enabled", config.getBoolean("worlds.end-world"));
            config.set("worlds.end.unlock", config.getBoolean("worlds.end-unlock"));
        }
        if (config.isString("worlds.normal-world")) {
            config.set("worlds.world-name", config.getString("worlds.normal-world"));
            config.set("worlds.normal-world", null);
        }
        if (config.isBoolean("worlds.end.dragon-fight")) {
            config.set("worlds.end.dragon-fight.enabled", config.getBoolean("worlds.end.dragon-fight"));
        }
        if (config.getConfigurationSection("worlds.dimensions") == null) {
            config.set("worlds.dimensions.normal", config.getConfigurationSection("worlds.normal"));
            config.set("worlds.dimensions.normal.environment", "NORMAL");
            config.set("worlds.dimensions.normal.portals.NETHER", "nether");
            config.set("worlds.dimensions.normal.portals.ENDER", "the_end");
            config.set("worlds.normal", null);
            config.set("worlds.dimensions.nether", config.getConfigurationSection("worlds.nether"));
            config.set("worlds.dimensions.nether.environment", "NETHER");
            config.set("worlds.dimensions.nether.portals.NETHER", "normal");
            config.set("worlds.dimensions.nether.portals.ENDER", "the_end");
            config.set("worlds.nether", null);
            config.set("worlds.dimensions.the_end", config.getConfigurationSection("worlds.end"));
            config.set("worlds.dimensions.the_end.environment", "THE_END");
            config.set("worlds.dimensions.the_end.portals.NETHER", "nether");
            config.set("worlds.dimensions.the_end.portals.ENDER", "normal");
            config.set("worlds.end", null);
        }
        if (config.get("default-values.island-effects") == null) {
            config.createSection("default-values.island-effects");
        }
        if (convertListToSection(config, "default-values.block-limits")) {
            forceSave = true;
        }
        if (convertListToSection(config, "default-values.entity-limits")) {
            forceSave = true;
        }
        if (convertListToSection(config, "default-values.island-effects")) {
            forceSave = true;
        }
        if (convertListToSection(config, "default-values.role-limits")) {
            forceSave = true;
        }
        if (convertListToSection(config, "stacked-blocks.limits")) {
            forceSave = true;
        }
        if (convertListToSection(config, "default-placeholders")) {
            forceSave = true;
        }
        if (config.isConfigurationSection("worlds.dimensions")) {
            boolean hasDimensionalGeneratorRates = false;

            for (String dimension : config.getConfigurationSection("worlds.dimensions").getKeys(false)) {
                if (config.contains("default-values.generator." + dimension)) {
                    if (convertListToSection(config, "default-values.generator." + dimension)) {
                        forceSave = true;
                    }
                    hasDimensionalGeneratorRates = true;
                }
            }

            if (!hasDimensionalGeneratorRates) {
                String defaultDimension = config.getString("worlds.default-world");
                config.set("default-values.generator." + defaultDimension, config.get("default-values.generator"));
                if (convertListToSection(config, "default-values.generator." + defaultDimension)) {
                    forceSave = true;
                }
            }
        }

        return forceSave;
    }

    private boolean convertListToSection(YamlConfiguration config, String path) {
        if (!config.isList(path)) {
            return false;
        }

        List<String> list = config.getStringList(path);
        config.createSection(path);

        for (String line : list) {
            String[] sections = line.split(":");

            String key;
            String value;
            if (sections.length == 2) {
                key = sections[0];
                value = sections[1];
            } else if (sections.length == 3) {
                key = sections[0] + ":" + sections[1];
                value = sections[2];
            } else {
                Log.warnFromFile("config.yml", "The value '", line, "' has an incorrect amount of sections, skipping...");
                continue;
            }

            try {
                config.set(path + "." + key, Integer.parseInt(value));
            } catch (NumberFormatException e) {
                config.set(path + "." + key, value);
            }
        }

        return true;
    }

    private boolean convertInteractables(SuperiorSkyblockPlugin plugin, YamlConfiguration mainConfig) {
        if (!mainConfig.isList("interactables")) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if (!file.exists()) {
            plugin.saveResource("interactables.yml", false);
        }

        CommentedConfiguration interactablesConfig = CommentedConfiguration.loadConfiguration(file);

        interactablesConfig.set("interactables", mainConfig.getStringList("interactables"));
        mainConfig.set("interactables", null);

        try {
            interactablesConfig.save(file);
        } catch (Exception error) {
            Log.errorFromFile(error, file.getName(), "An unexpected error occurred while saving file:");
        }

        return true;
    }

    private boolean convertEntityCategories(SuperiorSkyblockPlugin plugin, YamlConfiguration mainConfig) {
        if (!mainConfig.isConfigurationSection("entity-categories")) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), "entity-categories.yml");

        if (!file.exists()) {
            plugin.saveResource("entity-categories.yml", false);
        }

        CommentedConfiguration entitiesConfig = CommentedConfiguration.loadConfiguration(file);

        for (String categoryName : mainConfig.getConfigurationSection("entity-categories").getKeys(false)) {
            List<String> entities = mainConfig.getStringList("entity-categories." + categoryName);

            if (!entities.isEmpty()) {
                categoryName = categoryName.toUpperCase(Locale.ENGLISH);
                entitiesConfig.set(categoryName + ".entities", entities);
                entitiesConfig.set(categoryName + ".actions.SPAWN", categoryName + "_SPAWN");
                entitiesConfig.set(categoryName + ".actions.DAMAGE", categoryName + "_DAMAGE");
                entitiesConfig.set(categoryName + ".actions.SPAWNER_SPAWN", "SPAWNER_" + categoryName + "_SPAWN");
                entitiesConfig.set(categoryName + ".actions.NATURAL_SPAWN", "NATURAL_" + categoryName + "_SPAWN");
            }
        }

        mainConfig.set("entity-categories", null);

        try {
            entitiesConfig.save(file);
        } catch (Exception error) {
            Log.errorFromFile(error, file.getName(), "An unexpected error occurred while saving file:");
        }

        return true;
    }

    private boolean convertBankModule(SuperiorSkyblockPlugin plugin, YamlConfiguration mainConfig) {
        File file = new File(plugin.getDataFolder(), "modules/bank/config.yml");

        if (!file.exists()) {
            plugin.saveResource("modules/bank/config.yml", false);
        }

        CommentedConfiguration bankConfig = CommentedConfiguration.loadConfiguration(file);

        boolean forceSave = false;

        // The value might have been entered as an integer, so we cannot use isDouble().
        if (mainConfig.get("bank-worth-rate") != null) {
            bankConfig.set("bank-worth-rate", mainConfig.getDouble("bank-worth-rate"));
            mainConfig.set("bank-worth-rate", null);
            forceSave = true;
        }

        // The value might have been entered as an integer, so we cannot use isDouble().
        if (mainConfig.get("disband-refund") != null) {
            bankConfig.set("disband-refund", mainConfig.getDouble("disband-refund"));
            mainConfig.set("disband-refund", null);
            forceSave = true;
        }

        if (mainConfig.isBoolean("bank-logs")) {
            bankConfig.set("bank-logs", mainConfig.getBoolean("bank-logs"));
            mainConfig.set("bank-logs", null);
            forceSave = true;
        }

        if (mainConfig.isBoolean("cache-logs")) {
            bankConfig.set("cache-logs", mainConfig.getBoolean("cache-logs"));
            mainConfig.set("cache-logs", null);
            forceSave = true;
        }

        if (mainConfig.isConfigurationSection("bank-interest")) {
            bankConfig.set("bank-interest", mainConfig.getConfigurationSection("bank-interest"));
            mainConfig.set("bank-interest", null);
            forceSave = true;
        }

        if (forceSave) {
            try {
                bankConfig.save(file);
            } catch (Exception error) {
                Log.errorFromFile(error, file.getName(), "An unexpected error occurred while saving file:");
            }
        }

        return forceSave;
    }

    private boolean convertGeneratorsModule(SuperiorSkyblockPlugin plugin, YamlConfiguration mainConfig) {
        File file = new File(plugin.getDataFolder(), "modules/generators/config.yml");

        if (!file.exists()) {
            plugin.saveResource("modules/generators/config.yml", false);
        }

        CommentedConfiguration generatorsConfig = CommentedConfiguration.loadConfiguration(file);

        boolean forceSave = false;

        if (mainConfig.isBoolean("generators")) {
            generatorsConfig.set("enabled", mainConfig.getBoolean("generators"));
            mainConfig.set("generators", null);
            forceSave = true;
        }

        if (forceSave) {
            try {
                generatorsConfig.save(file);
            } catch (Exception error) {
                Log.errorFromFile(error, file.getName(), "An unexpected error occurred while saving file:");
            }
        }

        return forceSave;
    }

    private boolean convertUpgradesModule(SuperiorSkyblockPlugin plugin, YamlConfiguration mainConfig) {
        File file = new File(plugin.getDataFolder(), "modules/upgrades/config.yml");

        if (!file.exists()) {
            plugin.saveResource("modules/upgrades/config.yml", false);
        }

        CommentedConfiguration upgradesConfig = CommentedConfiguration.loadConfiguration(file);

        boolean forceSave = false;

        if (!upgradesConfig.isConfigurationSection("crop-growth")) {
            upgradesConfig.set("crop-growth.enabled", upgradesConfig.getBoolean("crop-growth"));
            forceSave = true;
        }

        if (mainConfig.isList("crops-to-grow")) {
            upgradesConfig.set("crop-growth.whitelisted-crops", mainConfig.getList("crops-to-grow"));
            mainConfig.set("crops-to-grow", null);
            forceSave = true;
        }

        if (mainConfig.isInt("crops-interval")) {
            upgradesConfig.set("crop-growth.interval", mainConfig.getInt("crops-interval"));
            mainConfig.set("crops-interval", null);
            forceSave = true;
        }

        if (!upgradesConfig.isConfigurationSection("mob-drops")) {
            upgradesConfig.set("mob-drops.enabled", upgradesConfig.getBoolean("mob-drops"));
            forceSave = true;
        }

        if (mainConfig.isBoolean("drops-upgrade-players-multiply")) {
            upgradesConfig.set("mob-drops.only-player-kills", mainConfig.getBoolean("drops-upgrade-players-multiply"));
            mainConfig.set("drops-upgrade-players-multiply", null);
            forceSave = true;
        }

        if (forceSave) {
            try {
                upgradesConfig.save(file);
            } catch (Exception error) {
                Log.errorFromFile(error, file.getName(), "An unexpected error occurred while saving file:");
            }
        }

        return forceSave;
    }

}
