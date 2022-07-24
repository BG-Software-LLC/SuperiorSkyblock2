package com.bgsoftware.superiorskyblock.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.config.section.AFKIntegrationsSection;
import com.bgsoftware.superiorskyblock.config.section.DatabaseSection;
import com.bgsoftware.superiorskyblock.config.section.DefaultContainersSection;
import com.bgsoftware.superiorskyblock.config.section.DefaultValuesSection;
import com.bgsoftware.superiorskyblock.config.section.IslandChestsSection;
import com.bgsoftware.superiorskyblock.config.section.IslandNamesSection;
import com.bgsoftware.superiorskyblock.config.section.IslandRolesSection;
import com.bgsoftware.superiorskyblock.config.section.SpawnSection;
import com.bgsoftware.superiorskyblock.config.section.StackedBlocksSection;
import com.bgsoftware.superiorskyblock.config.section.VisitorsSignSection;
import com.bgsoftware.superiorskyblock.config.section.VoidTeleportSection;
import com.bgsoftware.superiorskyblock.config.section.WorldsSection;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class SettingsManagerImpl extends Manager implements SettingsManager {

    private final SettingsContainer container;
    private final Database database;
    private final DefaultValues defaultValues;
    private final StackedBlocks stackedBlocks;
    private final IslandRoles islandRoles;
    private final VisitorsSign visitorsSign;
    private final Worlds worlds;
    private final Spawn spawn;
    private final VoidTeleport voidTeleport;
    private final IslandNames islandNames;
    private final AFKIntegrations afkIntegrations;
    private final DefaultContainersSection defaultContainers;
    private final IslandChests islandChests;

    public SettingsManagerImpl(SuperiorSkyblockPlugin plugin) throws ManagerLoadException {
        super(plugin);

        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        convertData(cfg);
        convertInteractables(plugin, cfg);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml",
                    "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases", "preview-islands");
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }

        this.container = new SettingsContainer(plugin, cfg);
        this.database = new DatabaseSection(this.container);
        this.defaultValues = new DefaultValuesSection(this.container);
        this.stackedBlocks = new StackedBlocksSection(this.container);
        this.islandRoles = new IslandRolesSection(this.container);
        this.visitorsSign = new VisitorsSignSection(this.container);
        this.worlds = new WorldsSection(this.container);
        this.spawn = new SpawnSection(this.container);
        this.voidTeleport = new VoidTeleportSection(this.container);
        this.islandNames = new IslandNamesSection(this.container);
        this.afkIntegrations = new AFKIntegrationsSection(this.container);
        this.defaultContainers = new DefaultContainersSection(this.container);
        this.islandChests = new IslandChestsSection(this.container);
    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException("Not supported for SettingsHandler");
    }

    @Override
    public long getCalcInterval() {
        return this.container.calcInterval;
    }

    @Override
    public Database getDatabase() {
        return this.database;
    }

    @Override
    public String getIslandCommand() {
        return this.container.islandCommand;
    }

    @Override
    public int getMaxIslandSize() {
        return this.container.maxIslandSize;
    }

    @Override
    public DefaultValues getDefaultValues() {
        return this.defaultValues;
    }

    @Override
    public int getIslandHeight() {
        return this.container.islandsHeight;
    }

    @Override
    public boolean isWorldBorders() {
        return this.container.worldBordersEnabled;
    }

    @Override
    public StackedBlocks getStackedBlocks() {
        return this.stackedBlocks;
    }

    @Override
    public String getIslandLevelFormula() {
        return this.container.islandLevelFormula;
    }

    @Override
    public boolean isRoundedIslandLevels() {
        return this.container.roundedIslandLevel;
    }

    @Override
    public String getIslandTopOrder() {
        return this.container.islandTopOrder;
    }

    @Override
    public boolean isCoopMembers() {
        return this.container.coopMembers;
    }

    @Override
    public IslandRoles getIslandRoles() {
        return this.islandRoles;
    }

    @Override
    public String getSignWarpLine() {
        return this.container.signWarpLine;
    }

    @Override
    public List<String> getSignWarp() {
        return this.container.signWarp;
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
    public VoidTeleport getVoidTeleport() {
        return this.voidTeleport;
    }

    @Override
    public List<String> getInteractables() {
        return this.container.interactables;
    }

    @Override
    public Collection<Key> getSafeBlocks() {
        return this.container.safeBlocks;
    }

    @Override
    public boolean isVisitorsDamage() {
        return this.container.visitorsDamage;
    }

    @Override
    public boolean isCoopDamage() {
        return this.container.coopDamage;
    }

    @Override
    public int getDisbandCount() {
        return this.container.disbandCount;
    }

    @Override
    public boolean isIslandTopIncludeLeader() {
        return this.container.islandTopIncludeLeader;
    }

    @Override
    public Map<String, String> getDefaultPlaceholders() {
        return this.container.defaultPlaceholders;
    }

    @Override
    public boolean isBanConfirm() {
        return this.container.banConfirm;
    }

    @Override
    public boolean isDisbandConfirm() {
        return this.container.disbandConfirm;
    }

    @Override
    public boolean isKickConfirm() {
        return this.container.kickConfirm;
    }

    @Override
    public boolean isLeaveConfirm() {
        return this.container.leaveConfirm;
    }

    @Override
    public String getSpawnersProvider() {
        return this.container.spawnersProvider;
    }

    @Override
    public String getStackedBlocksProvider() {
        return this.container.stackedBlocksProvider;
    }

    @Override
    public boolean isDisbandInventoryClear() {
        return this.container.disbandInventoryClear;
    }

    @Override
    public IslandNames getIslandNames() {
        return this.islandNames;
    }

    @Override
    public boolean isTeleportOnJoin() {
        return this.container.teleportOnJoin;
    }

    @Override
    public boolean isTeleportOnKick() {
        return this.container.teleportOnKick;
    }

    @Override
    public boolean isClearOnJoin() {
        return this.container.clearOnJoin;
    }

    @Override
    public boolean isRateOwnIsland() {
        return this.container.rateOwnIsland;
    }

    @Override
    public List<String> getDefaultSettings() {
        return this.container.defaultSettings;
    }

    @Override
    public boolean isDisableRedstoneOffline() {
        return this.container.disableRedstoneOffline;
    }

    @Override
    public AFKIntegrations getAFKIntegrations() {
        return this.afkIntegrations;
    }

    @Override
    public Map<String, Pair<Integer, String>> getCommandsCooldown() {
        return this.container.commandsCooldown;
    }

    @Override
    public long getUpgradeCooldown() {
        return this.container.upgradeCooldown;
    }

    @Override
    public String getNumbersFormat() {
        return this.container.numberFormat;
    }

    @Override
    public String getDateFormat() {
        return this.container.dateFormat;
    }

    @Override
    public boolean isSkipOneItemMenus() {
        return this.container.skipOneItemMenus;
    }

    @Override
    public boolean isTeleportOnPvPEnable() {
        return this.container.teleportOnPVPEnable;
    }

    @Override
    public boolean isImmuneToPvPWhenTeleport() {
        return this.container.immuneToPVPWhenTeleport;
    }

    @Override
    public List<String> getBlockedVisitorsCommands() {
        return this.container.blockedVisitorsCommands;
    }

    @Override
    public DefaultContainersSection getDefaultContainers() {
        return this.defaultContainers;
    }

    @Override
    public List<String> getDefaultSign() {
        return this.container.defaultSignLines;
    }

    @Override
    public Map<String, List<String>> getEventCommands() {
        return this.container.eventCommands;
    }

    @Override
    public long getWarpsWarmup() {
        return this.container.warpsWarmup;
    }

    @Override
    public long getHomeWarmup() {
        return this.container.homeWarmup;
    }

    @Override
    public boolean isLiquidUpdate() {
        return this.container.liquidUpdate;
    }

    @Override
    public boolean isLightsUpdate() {
        return this.container.lightsUpdate;
    }

    @Override
    public List<String> getPvPWorlds() {
        return this.container.pvpWorlds;
    }

    @Override
    public boolean isStopLeaving() {
        return this.container.stopLeaving;
    }

    @Override
    public boolean isValuesMenu() {
        return this.container.valuesMenu;
    }

    @Override
    public List<String> getCropsToGrow() {
        return this.container.cropsToGrow;
    }

    @Override
    public int getCropsInterval() {
        return this.container.cropsInterval;
    }

    @Override
    public boolean isOnlyBackButton() {
        return this.container.onlyBackButton;
    }

    @Override
    public boolean isBuildOutsideIsland() {
        return this.container.buildOutsideIsland;
    }

    @Override
    public String getDefaultLanguage() {
        return this.container.defaultLanguage;
    }

    @Override
    public boolean isDefaultWorldBorder() {
        return this.container.defaultWorldBorder;
    }

    @Override
    public boolean isDefaultStackedBlocks() {
        return this.container.defaultBlocksStacker;
    }

    @Override
    public boolean isDefaultToggledPanel() {
        return this.container.defaultToggledPanel;
    }

    @Override
    public boolean isDefaultIslandFly() {
        return this.container.defaultIslandFly;
    }

    @Override
    public String getDefaultBorderColor() {
        return this.container.defaultBorderColor;
    }

    @Override
    public boolean isObsidianToLava() {
        return this.container.obsidianToLava;
    }

    @Override
    public BlockValuesManager.SyncWorthStatus getSyncWorth() {
        return this.container.syncWorth;
    }

    @Override
    public boolean isNegativeWorth() {
        return this.container.negativeWorth;
    }

    @Override
    public boolean isNegativeLevel() {
        return this.container.negativeLevel;
    }

    @Override
    public List<String> getDisabledEvents() {
        return this.container.disabledEvents;
    }

    @Override
    public List<String> getDisabledCommands() {
        return this.container.disabledCommands;
    }

    @Override
    public List<String> getDisabledHooks() {
        return this.container.disabledHooks;
    }

    @Override
    public boolean isSchematicNameArgument() {
        return this.container.schematicNameArgument;
    }

    @Override
    public IslandChests getIslandChests() {
        return this.islandChests;
    }

    @Override
    public Map<String, List<String>> getCommandAliases() {
        return this.container.commandAliases;
    }

    @Override
    public Set<Key> getValuableBlocks() {
        return this.container.valuableBlocks;
    }

    @Override
    public Map<String, Location> getPreviewIslands() {
        return this.container.islandPreviewLocations;
    }

    @Override
    public boolean isTabCompleteHideVanished() {
        return this.container.tabCompleteHideVanished;
    }

    @Override
    public boolean isDropsUpgradePlayersMultiply() {
        return this.container.dropsUpgradePlayersMultiply;
    }

    @Override
    public long getProtectedMessageDelay() {
        return this.container.protectedMessageDelay;
    }

    @Override
    public boolean isWarpCategories() {
        return this.container.warpCategories;
    }

    @Override
    public boolean isPhysicsListener() {
        return this.container.physicsListener;
    }

    @Override
    public double getChargeOnWarp() {
        return this.container.chargeOnWarp;
    }

    @Override
    public boolean isPublicWarps() {
        return this.container.publicWarps;
    }

    @Override
    public long getRecalcTaskTimeout() {
        return this.container.recalcTaskTimeout;
    }

    @Override
    public boolean isAutoLanguageDetection() {
        return this.container.autoLanguageDetection;
    }

    @Override
    public boolean isAutoUncoopWhenAlone() {
        return this.container.autoUncoopWhenAlone;
    }

    @Override
    public TopIslandMembersSorting getTopIslandMembersSorting() {
        return this.container.islandTopMembersSorting;
    }

    @Override
    public int getBossbarLimit() {
        return this.container.bossBarLimit;
    }

    @Override
    public boolean getDeleteUnsafeWarps() {
        return this.container.deleteUnsafeWarps;
    }

    public void updateValue(String path, Object value) throws IOException {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml",
                "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases", "preview-islands");

        cfg.set(path, value);

        cfg.save(file);

        try {
            plugin.setSettings(new SettingsManagerImpl(plugin));
        } catch (ManagerLoadException ex) {
            ManagerLoadException.handle(ex);
        }
    }

    private void convertData(YamlConfiguration cfg) {
        if (cfg.contains("default-hoppers-limit")) {
            cfg.set("default-limits", Collections.singletonList("HOPPER:" + cfg.getInt("default-hoppers-limit")));
            cfg.set("default-hoppers-limit", null);
        }
        if (cfg.contains("default-permissions")) {
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
        if (cfg.contains("spawn-location"))
            cfg.set("spawn.location", cfg.getString("spawn-location"));
        if (cfg.contains("spawn-protection"))
            cfg.set("spawn.protection", cfg.getBoolean("spawn-protection"));
        if (cfg.getBoolean("spawn-pvp", false))
            cfg.set("spawn.settings", Collections.singletonList("PVP"));
        if (cfg.contains("island-world"))
            cfg.set("worlds.normal-world", cfg.getString("island-world"));
        if (cfg.contains("welcome-sign-line"))
            cfg.set("visitors-sign.line", cfg.getString("welcome-sign-line"));
        if (cfg.contains("island-roles.ladder")) {
            for (String name : cfg.getConfigurationSection("island-roles.ladder").getKeys(false)) {
                if (!cfg.contains("island-roles.ladder." + name + ".id"))
                    cfg.set("island-roles.ladder." + name + ".id", cfg.getInt("island-roles.ladder." + name + ".weight"));
            }
        }
        if (cfg.contains("default-island-size"))
            cfg.set("default-values.island-size", cfg.getInt("default-island-size"));
        if (cfg.contains("default-limits"))
            cfg.set("default-values.block-limits", cfg.getStringList("default-limits"));
        if (cfg.contains("default-entity-limits"))
            cfg.set("default-values.entity-limits", cfg.getStringList("default-entity-limits"));
        if (cfg.contains("default-warps-limit"))
            cfg.set("default-values.warps-limit", cfg.getInt("default-warps-limit"));
        if (cfg.contains("default-team-limit"))
            cfg.set("default-values.team-limit", cfg.getInt("default-team-limit"));
        if (cfg.contains("default-crop-growth"))
            cfg.set("default-values.crop-growth", cfg.getInt("default-crop-growth"));
        if (cfg.contains("default-spawner-rates"))
            cfg.set("default-values.spawner-rates", cfg.getInt("default-spawner-rates"));
        if (cfg.contains("default-mob-drops"))
            cfg.set("default-values.mob-drops", cfg.getInt("default-mob-drops"));
        if (cfg.contains("default-island-height"))
            cfg.set("islands-height", cfg.getInt("default-island-height"));
        if (cfg.contains("starter-chest")) {
            cfg.set("default-containers.enabled", cfg.getBoolean("starter-chest.enabled"));
            cfg.set("default-containers.containers.chest", cfg.getConfigurationSection("starter-chest.contents"));
        }
        if (cfg.contains("default-generator"))
            cfg.set("default-values.generator", cfg.getStringList("default-generator"));
        if (cfg.isBoolean("void-teleport")) {
            boolean voidTeleport = cfg.getBoolean("void-teleport");
            cfg.set("void-teleport.members", voidTeleport);
            cfg.set("void-teleport.visitors", voidTeleport);
        }
        if (cfg.isBoolean("sync-worth"))
            cfg.set("sync-worth", cfg.getBoolean("sync-worth") ? "BUY" : "NONE");
        if (!cfg.contains("worlds.nether")) {
            cfg.set("worlds.nether.enabled", cfg.getBoolean("worlds.nether-world"));
            cfg.set("worlds.nether.unlock", cfg.getBoolean("worlds.nether-unlock"));
        }
        if (!cfg.contains("worlds.end")) {
            cfg.set("worlds.end.enabled", cfg.getBoolean("worlds.end-world"));
            cfg.set("worlds.end.unlock", cfg.getBoolean("worlds.end-unlock"));
        }
        if (cfg.contains("worlds.normal-world")) {
            cfg.set("worlds.world-name", cfg.getString("worlds.normal-world"));
            cfg.set("worlds.normal-world", null);
        }
        if (cfg.isBoolean("worlds.end.dragon-fight")) {
            cfg.set("worlds.end.dragon-fight.enabled", cfg.getBoolean("worlds.end.dragon-fight"));
        }
    }

    private void convertInteractables(SuperiorSkyblockPlugin plugin, YamlConfiguration cfg) {
        if (!cfg.contains("interactables"))
            return;

        File file = new File(plugin.getDataFolder(), "interactables.yml");

        if (!file.exists())
            plugin.saveResource("interactables.yml", false);

        CommentedConfiguration commentedConfig = CommentedConfiguration.loadConfiguration(file);

        commentedConfig.set("interactables", cfg.getStringList("interactables"));

        try {
            commentedConfig.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }


    }

}
