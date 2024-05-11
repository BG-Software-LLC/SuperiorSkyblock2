package com.bgsoftware.superiorskyblock.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.config.section.AFKIntegrationsSection;
import com.bgsoftware.superiorskyblock.config.section.DatabaseSection;
import com.bgsoftware.superiorskyblock.config.section.DefaultContainersSection;
import com.bgsoftware.superiorskyblock.config.section.DefaultValuesSection;
import com.bgsoftware.superiorskyblock.config.section.GlobalSection;
import com.bgsoftware.superiorskyblock.config.section.IslandChestsSection;
import com.bgsoftware.superiorskyblock.config.section.IslandNamesSection;
import com.bgsoftware.superiorskyblock.config.section.IslandRolesSection;
import com.bgsoftware.superiorskyblock.config.section.SpawnSection;
import com.bgsoftware.superiorskyblock.config.section.StackedBlocksSection;
import com.bgsoftware.superiorskyblock.config.section.VisitorsSignSection;
import com.bgsoftware.superiorskyblock.config.section.VoidTeleportSection;
import com.bgsoftware.superiorskyblock.config.section.WorldsSection;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class SettingsManagerImpl extends Manager implements SettingsManager {

    private static final String[] IGNORED_SECTIONS = new String[]{
            "config.yml", "ladder", "commands-cooldown", "containers", "event-commands", "command-aliases",
            "preview-islands", "default-values.block-limits", "default-values.entity-limits",
            "default-values.role-limits", "stacked-blocks.limits", "default-values.generator"
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

    public SettingsManagerImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() throws ManagerLoadException {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        convertData(cfg);
        convertInteractables(plugin, cfg);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), IGNORED_SECTIONS);
        } catch (Exception error) {
            Log.error(error, file, "An unexpected error occurred while loading config file:");
        }

        loadContainerFromConfig(cfg);
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
    public DefaultValues getDefaultValues() {
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
        return this.global.getIslandLevelFormula();
    }

    @Override
    public boolean isRoundedIslandLevels() {
        return this.global.isRoundedIslandLevels();
    }

    @Override
    public String getIslandTopOrder() {
        return this.global.getIslandTopOrder();
    }

    @Override
    public boolean isCoopMembers() {
        return this.global.isCoopMembers();
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
    public VoidTeleport getVoidTeleport() {
        return this.voidTeleport;
    }

    @Override
    public List<String> getInteractables() {
        return this.global.getInteractables();
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
    public String getSpawnersProvider() {
        return this.global.getSpawnersProvider();
    }

    @Override
    public String getStackedBlocksProvider() {
        return this.global.getStackedBlocksProvider();
    }

    @Override
    public boolean isDisbandInventoryClear() {
        return this.global.isDisbandInventoryClear();
    }

    @Override
    public IslandNames getIslandNames() {
        return this.islandNames;
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
    public boolean isClearOnJoin() {
        return this.global.isClearOnJoin();
    }

    @Override
    public boolean isRateOwnIsland() {
        return this.global.isRateOwnIsland();
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
        return this.global.getHomeWarmup();
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
    public List<String> getCropsToGrow() {
        return this.global.getCropsToGrow();
    }

    @Override
    public int getCropsInterval() {
        return this.global.getCropsInterval();
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
    public Map<String, Location> getPreviewIslands() {
        return this.global.getPreviewIslands();
    }

    @Override
    public boolean isTabCompleteHideVanished() {
        return this.global.isTabCompleteHideVanished();
    }

    @Override
    public boolean isDropsUpgradePlayersMultiply() {
        return this.global.isDropsUpgradePlayersMultiply();
    }

    @Override
    public long getProtectedMessageDelay() {
        return this.global.getProtectedMessageDelay();
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
            loadContainerFromConfig(cfg);
        } catch (ManagerLoadException ex) {
            ManagerLoadException.handle(ex);
        }
    }

    private void loadContainerFromConfig(YamlConfiguration cfg) throws ManagerLoadException {
        SettingsContainer container = new SettingsContainer(plugin, cfg);
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
        } catch (Exception error) {
            Log.error(error, file, "An unexpected error occurred while saving new interactables file:");
        }


    }

}
