package com.bgsoftware.superiorskyblock.api.config;

import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SettingsManager {

    /**
     * The amount of time between auto-calculations that the plugin runs.
     * If set to 0, it means the task is disabled.
     * Config path: calc-interval
     */
    long getCalcInterval();

    /**
     * All settings related to the database of the plugin.
     * Config path: database
     */
    Database getDatabase();

    /**
     * The main command of the plugin.
     * Aliases can be added by adding "," after the command name, and split them using ",".
     * Config path: island-command
     */
    String getIslandCommand();

    /**
     * The maximum island size.
     * Config path: max-island-size
     */
    int getMaxIslandSize();

    /**
     * All the default values for new islands that are created.
     * Config path: default-values
     */
    DefaultValues getDefaultValues();

    /**
     * The default height islands will generate.
     * Config path: islands-height
     */
    int getIslandHeight();

    /**
     * Whether world borders are enabled for islands or not.
     * Config path: world-borders
     */
    boolean isWorldBorders();

    /**
     * All settings related to the stacked-blocks system of the plugin.
     * Config path: stacked-blocks
     */
    StackedBlocks getStackedBlocks();

    /**
     * The island worth to island level conversion formula.
     * The formula contains a placeholder: `{}`, which is replaced with the island worth.
     * Config path: island-level-formula
     */
    String getIslandLevelFormula();

    /**
     * Whether island levels should be rounded or not.
     * Config path: rounded-island-level
     */
    boolean isRoundedIslandLevels();

    /**
     * The default island-top sorting type.
     * Config path: island-top-order
     */
    String getIslandTopOrder();

    /**
     * Whether coop members are enabled.
     * Config path: coop-members
     */
    boolean isCoopMembers();

    /**
     * All settings related to the island-roles.
     * Config path: island-roles
     */
    IslandRoles getIslandRoles();

    /**
     * The line that determines if a sign is created as an island warp.
     * Config path: sign-warp-line
     */
    String getSignWarpLine();

    /**
     * The lines to be set for warp signs.
     * Config path: sign-warp
     */
    List<String> getSignWarp();

    /**
     * All settings related to the visitors-sign.
     * Config path: visitors-sign
     */
    VisitorsSign getVisitorsSign();

    /**
     * All settings related to the worlds of the plugin..
     * Config path: worlds
     */
    Worlds getWorlds();

    /**
     * All settings related to the spawn island.
     * Config path: spawn
     */
    Spawn getSpawn();

    /**
     * All settings related to the void teleportation.
     * Config path: void-teleport
     */
    VoidTeleport getVoidTeleport();

    /**
     * Get all the interactable blocks.
     */
    List<String> getInteractables();

    /**
     * Get all the safe blocks.
     */
    Collection<Key> getSafeBlocks();

    /**
     * Whether visitors should take damage on islands or not.
     * Config path: visitors-damage
     */
    boolean isVisitorsDamage();

    /**
     * Whether coop players should take damage on islands or not.
     * Config-path: coop-damage
     */
    boolean isCoopDamage();

    /**
     * The default amount of disbands players receive when they first join the server.
     * If 0, then the disbands limit is disabled.
     * Config-path: disband-count
     */
    int getDisbandCount();

    /**
     * Whether the members list shown in island top should include the leader or not.
     * Config-path: island-top-include-leader
     */
    boolean isIslandTopIncludeLeader();

    /**
     * Default placeholders to be returned when no island exists.
     * Config-path: default-placeholders
     */
    Map<String, String> getDefaultPlaceholders();

    /**
     * Whether confirmation menu should be opened before banning a player from an island or not.
     * Config-path: ban-confirm
     */
    boolean isBanConfirm();

    /**
     * Whether confirmation menu should be opened before disbanding an island or not.
     * Config-path: disband-confirm
     */
    boolean isDisbandConfirm();

    /**
     * Whether confirmation menu should be opened before kicking an island member from an island or not.
     * Config-path: kick-confirm
     */
    boolean isKickConfirm();

    /**
     * Whether confirmation menu should be opened before leaving an island or not.
     * Config-path: leave-confirm
     */
    boolean isLeaveConfirm();

    /**
     * The spawners-provider to use.
     * If set to AUTO, the plugin will automatically detect an available spawners provider and use it.
     * Config-path: spawners-provider
     */
    String getSpawnersProvider();

    /**
     * The stacked-blocks provider to use.
     * If set to AUTO, the plugin will automatically detect an available stacked-blocks provider and use it.
     * Config-path: stacked-blocks-provider
     */
    String getStackedBlocksProvider();

    /**
     * Whether inventory of island members should be cleared when their island is disbanded or not.
     * Config-path: disband-inventory-clear
     */
    boolean isDisbandInventoryClear();

    /**
     * All settings related to island-names.
     * Config path: island-names
     */
    IslandNames getIslandNames();

    /**
     * Whether to teleport players to their island when they join it or not.
     * Config-path: teleport-on-join
     */
    boolean isTeleportOnJoin();

    /**
     * Whether to teleport players to the spawn when they are kicked from their island or not.
     * Config-path: teleport-on-kick
     */
    boolean isTeleportOnKick();

    /**
     * Whether to clear players' inventories when they join a new island or not.
     * Config-path: clear-on-join
     */
    boolean isClearOnJoin();

    /**
     * Whether players can rate their own island or not.
     * Config-path: rate-own-island
     */
    boolean isRateOwnIsland();

    /**
     * All the default island-flags that will be enabled for new islands.
     * Config-path: default-settings
     */
    List<String> getDefaultSettings();

    /**
     * Whether redstone should be disabled on islands when all of the members of the island are offline or not.
     * Config-path: disable-redstone-offline
     */
    boolean isDisableRedstoneOffline();

    /**
     * All settings related to afk-integrations.
     * Config path: afk-integrations
     */
    AFKIntegrations getAFKIntegrations();

    /**
     * Cooldowns of commands for players.
     * Represented by a map with keys as the command labels, and values as pairs
     * containing the cooldown and a bypass permission.
     * Config-path: commands-cooldown
     */
    Map<String, Pair<Integer, String>> getCommandsCooldown();

    /**
     * Cooldown between upgrades.
     * If -1, then there is no cooldown.
     * Config-path: upgrade-cooldown
     */
    long getUpgradeCooldown();

    /**
     * The numbers-formatting of the plugin.
     * Config-path: number-format
     */
    String getNumbersFormat();

    /**
     * The date-formatting of the plugin.
     * Config-path: date-format
     */
    String getDateFormat();

    /**
     * Whether menus with only one item inside them should be skipped or not.
     * Config-path: skip-one-item-menus
     */
    boolean isSkipOneItemMenus();

    /**
     * Whether visitors on islands should get teleported to spawn when pvp is enabled on the island they were on or not.
     * Config-path: teleport-on-pvp-enable
     */
    boolean isTeleportOnPvPEnable();

    /**
     * Whether visitors should be immuned to PvP for a few seconds when they visit an island that has pvp enabled or not.
     * Config-path: immune-to-pvp-when-teleport
     */
    boolean isImmuneToPvPWhenTeleport();

    /**
     * List of blocked commands that visitors cannot run on islands.
     * Config-path: blocked-visitors-commands
     */
    List<String> getBlockedVisitorsCommands();

    /**
     * All settings related to default-containers in schematics.
     * Currently, getting contents of containers is not available using the API.
     * Config path: island-names
     */
    DefaultContainers getDefaultContainers();

    /**
     * Lines that should be set for signs of schematics.
     * If empty, no signs will be changed.
     * Config-path: default-signs
     */
    List<String> getDefaultSign();

    /**
     * List of commands to be executed for events.
     * Represented by a map with keys as event names and values as a list of commands.
     * Config-path: event-commands
     */
    Map<String, List<String>> getEventCommands();

    /**
     * Delay before teleporting to an island warp, in milliseconds.
     * If 0, no delay will be.
     * Config-path: warps-warmup
     */
    long getWarpsWarmup();

    /**
     * Delay before teleporting to island home, in milliseconds.
     * If 0, no delay will be.
     * Config-path: home-warmup
     */
    long getHomeWarmup();

    /**
     * Delay before teleporting to another island, in milliseconds.
     * If 0, no delay will be.
     * Config-path: visit-warmup
     */
    long getVisitWarmup();

    /**
     * Whether liquids should receive a physics update when placed in schematics or not.
     * Config-path: liquid-update
     */
    boolean isLiquidUpdate();

    /**
     * Whether lights should be set when placing schematics or not.
     * Config-path: lights-update
     */
    boolean isLightsUpdate();

    /**
     * List of worlds that pvp is allowed between island-members.
     * Config-path: pvp-worlds
     */
    List<String> getPvPWorlds();

    /**
     * Whether the plugin should force players to stay in islands in the islands worlds or not.
     * Config-path: stop-leaving
     */
    boolean isStopLeaving();

    /**
     * Whether players can open the values-menu by right-clicking on islands in the islands top menu or not.
     * Config-path: values-menu
     */
    boolean isValuesMenu();

    /**
     * List of crops that can get affected by the crops-growth multiplier.
     * Config-path: crops-to-grow
     */
    List<String> getCropsToGrow();

    /**
     * Time between each iteration of the crops task.
     * Config-path: crops-interval
     */
    int getCropsInterval();

    /**
     * Whether players can only go back to the previous menu by clicking the back-button or not.
     * Config-path: only-back-button
     */
    boolean isOnlyBackButton();

    /**
     * Whether players can build outside their islands or not.
     * When enabled, island-sizes to be {@link #getMaxIslandSize()} * 1.5,
     * and islands will be connected to each other.
     * Config-path: build-outside-island
     */
    boolean isBuildOutsideIsland();

    /**
     * The default language to be set for new players.
     * Config-path: default-language
     */
    String getDefaultLanguage();

    /**
     * Whether new players should have world-borders enabled by default or not.
     * Config-path: default-world-border
     */
    boolean isDefaultWorldBorder();

    /**
     * Whether new players should be able to stack blocks by default or not.
     * Config-path: default-blocks-stacker
     */
    boolean isDefaultStackedBlocks();

    /**
     * Whether new players should have /is open their island panel by default or not.
     * Config-path: default-toggled-panel
     */
    boolean isDefaultToggledPanel();

    /**
     * Whether new players should have island fly enabled by default or not.
     * Config-path: default-island-fly
     */
    boolean isDefaultIslandFly();

    /**
     * The default border-color for new players.
     * Config-path: default-border-color
     */
    String getDefaultBorderColor();

    /**
     * Whether obsidian should turn into a lava-bucket when clicking on it with an empty bucket in hand or not.
     * Config-path: obsidian-to-lava
     */
    boolean isObsidianToLava();

    /**
     * The sync-worth status of the plugin.
     * Config-path: sync-worth
     */
    BlockValuesManager.SyncWorthStatus getSyncWorth();

    /**
     * Whether island-worth can be negative or not.
     * Config-path: negative-worth
     */
    boolean isNegativeWorth();

    /**
     * Whether island-level can be negative or not.
     * Config-path: negative-level
     */
    boolean isNegativeLevel();

    /**
     * List of plugin-events that should not be fired.
     * Config-path: disabled-events
     */
    List<String> getDisabledEvents();

    /**
     * List of commands that should be disabled within the plugin.
     * Config-path: disabled-commands
     */
    List<String> getDisabledCommands();

    /**
     * List of plugins that their hooks should not be enabled.
     * Config-path: disabled-hooks
     */
    List<String> getDisabledHooks();

    /**
     * Whether the schematic-name argument should be when executing /is create or not.
     * Config-path: schematic-name-argument
     */
    boolean isSchematicNameArgument();

    /**
     * All settings related to island-chests.
     * Config path: island-chests
     */
    IslandChests getIslandChests();

    /**
     * Custom aliases for commands of the plugin.
     * Represented by a map with keys as commands, and values as aliases.
     * Config-path: command-aliases
     */
    Map<String, List<String>> getCommandAliases();

    /**
     * List of valuable-blocks.
     * Config-path: valuable-blocks
     */
    Set<Key> getValuableBlocks();

    /**
     * List of preview-island locations.
     * Represented by a map with keys as schematic names, and values as locations for the preview islands.
     * Config-path: preview-islands
     */
    Map<String, Location> getPreviewIslands();

    /**
     * Whether vanished players should be hidden from command tab completes or not.
     * Config-path: tab-complete-hide-vanished
     */
    boolean isTabCompleteHideVanished();

    /**
     * Whether drops multiplier should only affect entities that are killed by players or not.
     * Config-path: drops-upgrade-players-multiply
     */
    boolean isDropsUpgradePlayersMultiply();

    /**
     * The delay between protect messages, in ticks.
     * Config-path: protected-message-delay
     */
    long getProtectedMessageDelay();

    /**
     * Whether the warp categories system is enabled or not.
     * Config-path: warp-categories
     */
    boolean isWarpCategories();

    /**
     * Whether the plugin should listen for the physics event or not.
     * Config-path: physics-listener
     */
    boolean isPhysicsListener();

    /**
     * Amount of money to be charged from players when they use an island warp.
     * If set to 0, no money will be charged.
     * Config-path: charge-on-warp
     */
    double getChargeOnWarp();

    /**
     * Whether island warps should be public by default or not.
     * Config-path: public-warps
     */
    boolean isPublicWarps();

    /**
     * Cooldown between recalculations of an island, in seconds.
     * If set to 0, no cooldown is set.
     * Config-path: recalc-task-timeout
     */
    long getRecalcTaskTimeout();

    /**
     * Whether to detect the player's language automatically when he first joins the server.
     * Config-path: auto-language-detection
     */
    boolean isAutoLanguageDetection();

    /**
     * Automatically uncoop players when there are no island members left online that can remove uncoop players.
     * Config-path: auto-uncoop-when-alone
     */
    boolean isAutoUncoopWhenAlone();

    /**
     * Get the way to sort members in the top islands menu.
     * Config-path: island-top-members-sorting
     */
    TopIslandMembersSorting getTopIslandMembersSorting();

    /**
     * Limit of the amount of bossbar tasks each player can have at the same time.
     * Config-path: bossbar-limit
     */
    int getBossbarLimit();

    /**
     * Whether to delete unsafe warps when players try to teleport to them automatically.
     * Config-path: delete-unsafe-warps
     */
    boolean getDeleteUnsafeWarps();

    /**
     * Get the list of actions to perform when a player respawns.
     * Config-path: player-respawn
     */
    List<RespawnAction> getPlayerRespawn();

    /**
     * Get the threshold between saves for block counts.
     * Config-path: block-counts-save-threshold
     */
    BigInteger getBlockCountsSaveThreshold();

    interface Database {

        /**
         * Get the database-type to use (SQLite or MySQL).
         * Config-path: database.type
         */
        String getType();

        /**
         * Whether the datastore folder should be back-up on startup.
         * Config-path: database.backup
         */
        boolean isBackup();

        /**
         * The address used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.address
         */
        String getAddress();

        /**
         * The port used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.port
         */
        int getPort();

        /**
         * Get the name of the database.
         * Used for MySQL only.
         * Config-path: database.db-name
         */
        String getDBName();

        /**
         * The username used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.user-name
         */
        String getUsername();

        /**
         * The password used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.password
         */
        String getPassword();

        /**
         * The prefix used for tables in the database.
         * Used for MySQL only.
         * Config-path: database.prefix
         */
        String getPrefix();

        /**
         * Whether the database uses SSL or not.
         * Used for MySQL only.
         * Config-path: database.useSSL
         */
        boolean hasSSL();

        /**
         * Whether public-key-retrieval is allowed in the database or not.
         * Used for MySQL only.
         * Config-path: database.allowPublicKeyRetrieval
         */
        boolean hasPublicKeyRetrieval();

        /**
         * The wait-timeout of the database, in milliseconds.
         * Used for MySQL only.
         * Config-path: database.waitTimeout
         */
        long getWaitTimeout();

        /**
         * The max-lifetime of the database, in milliseconds.
         * Used for MySQL only.
         * Config-path: database.maxLifetime
         */
        long getMaxLifetime();

    }

    interface DefaultValues {

        /**
         * The default island size for new islands.
         * Config-path: default-values.island-size
         */
        int getIslandSize();

        /**
         * The default block limits for new islands.
         * Represented by a map with keys as the block types, and values as the limits.
         * Config-path: default-values.block-limits
         */
        Map<Key, Integer> getBlockLimits();

        /**
         * The default entity limits for new islands.
         * Represented by a map with keys as the entity types, and values as the limits.
         * Config-path: default-values.entity-limits
         */
        Map<Key, Integer> getEntityLimits();

        /**
         * The default warps limit for new islands.
         * Config-path: default-values.warps-limit
         */
        int getWarpsLimit();

        /**
         * The default team limit for new islands.
         * Config-path: default-values.team-limit
         */
        int getTeamLimit();

        /**
         * The default coops limit for new islands.
         * Config-path: default-values.coop-limit
         */
        int getCoopLimit();

        /**
         * The default crop-growth multiplier for new islands.
         * Config-path: default-values.crop-growth
         */
        double getCropGrowth();

        /**
         * The default spawner-rates multiplier for new islands.
         * Config-path: default-values.spawner-rates
         */
        double getSpawnerRates();

        /**
         * The default mob-drops multiplier for new islands.
         * Config-path: default-values.mob-drops
         */
        double getMobDrops();

        /**
         * The default bank-limit for new islands.
         * Config-path: default-values.bank-limit
         */
        BigDecimal getBankLimit();

        /**
         * The default generator-rates for new islands.
         * Represented by an array of maps with keys as the blocks, and values as the rates.
         * The maps are sorted by the {@link World.Environment} they belong to.
         * Config-path: default-values.generator
         */
        Map<Key, Integer>[] getGenerators();

        /**
         * The default role-limits for new islands.
         * Represented by a map with keys as the role ids, and values as the limit.
         * Config-path: default-values.role-limits
         */
        Map<Integer, Integer> getRoleLimits();

    }

    interface StackedBlocks {

        /**
         * Whether stacked blocks are enabled on the server or not.
         * Config-path: stacked-blocks.enabled
         */
        boolean isEnabled();

        /**
         * The custom hologram names for stacked blocks.
         * Config-path: stacked-blocks.custom-name
         */
        String getCustomName();

        /**
         * List of worlds that blocks cannot be stacked in.
         * Config-path: stacked-blocks.disabled-worlds
         */
        List<String> getDisabledWorlds();

        /**
         * List of whitelisted block types that can be stacked together.
         * Config-path: stacked-blocks.whitelisted
         */
        Set<Key> getWhitelisted();

        /**
         * Limits for stacked-blocks
         * Represented by a map with keys as block types, and values as limits.
         * Config-path: stacked-blocks.limits
         */
        Map<Key, Integer> getLimits();

        /**
         * Whether stacked blocks should be auto-collected to players' inventories or not.
         * Config-path: stacked-blocks.auto-collect
         */
        boolean isAutoCollect();

        /**
         * All the settings related to the deposit-menu of stacked blocks.
         * Config path: default-values.deposit-menu
         */
        DepositMenu getDepositMenu();


        interface DepositMenu {

            /**
             * Whether the deposit-menu is enabled or not.
             * Config path: default-values.deposit-menu.enabled
             */
            boolean isEnabled();

            /**
             * The title of the deposit menu.
             * Config path: default-values.deposit-menu.title
             */
            String getTitle();

        }

    }

    interface IslandRoles {

        /**
         * The configuration section of the island-roles.
         * Config path: island-roles
         */
        ConfigurationSection getSection();

    }

    interface VisitorsSign {

        /**
         * Whether a visitors sign is required for others to visit islands.
         * Config-path: visitors-sign.required-for-visit
         */
        boolean isRequiredForVisit();

        /**
         * The line that determines if the sign is used as a visitors home location.
         * Config-path: visitors-sign.line
         */
        String getLine();

        /**
         * The line that is displayed when the visitors sign is active.
         * Config-path: visitors-sign.active
         */
        String getActive();

        /**
         * The line that is displayed when the visitors sign is inactive.
         * Config-path: visitors-sign.inactive
         */
        String getInactive();

    }

    interface Worlds {

        /**
         * The default world environment.
         * Config-path: worlds.default-world
         */
        World.Environment getDefaultWorld();

        /**
         * The name of the overworld world.
         * Config-path: worlds.world-name
         */
        String getWorldName();

        /**
         * The name of the default world.
         */
        String getDefaultWorldName();

        /**
         * All settings related to the overworld world.
         * Config-path: worlds.normal
         */
        Normal getNormal();

        /**
         * All settings related to the nether world.
         * Config-path: worlds.nether
         */
        Nether getNether();

        /**
         * All settings related to the end world.
         * Config-path: worlds.end
         */
        End getEnd();

        /**
         * The difficulty of the islands worlds.
         * Config-path: worlds.difficulty
         */
        String getDifficulty();

        interface Normal {

            /**
             * Whether the overworld world is enabled or not.
             * Config-path: worlds.normal.enabled
             */
            boolean isEnabled();

            /**
             * Whether the overworld world is unlocked by default or not.
             * Config-path: worlds.normal.unlock
             */
            boolean isUnlocked();

            /**
             * Whether the schematic for the overworld world should be offset or not.
             * Config-path: worlds.normal.schematic-offset
             */
            boolean isSchematicOffset();

            /**
             * Get the default biome for the world.
             */
            String getBiome();

        }

        interface Nether {

            /**
             * Whether the nether world is enabled or not.
             * Config-path: worlds.nether.enabled
             */
            boolean isEnabled();

            /**
             * Whether the nether world is unlocked by default or not.
             * Config-path: worlds.nether.unlock
             */
            boolean isUnlocked();

            /**
             * Custom name for the nether world.
             * Config-path: worlds.nether.name
             */
            String getName();

            /**
             * Whether the schematic for the nether world should be offset or not.
             * Config-path: worlds.nether.schematic-offset
             */
            boolean isSchematicOffset();

            /**
             * Get the default biome for the world.
             */
            String getBiome();

        }

        interface End {

            /**
             * Whether the end world is enabled or not.
             * Config-path: worlds.end.enabled
             */
            boolean isEnabled();

            /**
             * Whether the end world is unlocked by default or not.
             * Config-path: worlds.end.unlock
             */
            boolean isUnlocked();

            /**
             * Custom name for the end world.
             * Config-path: worlds.end.name
             */
            String getName();

            /**
             * Whether the schematic for the end world should be offset or not.
             * Config-path: worlds.end.schematic-offset
             */
            boolean isSchematicOffset();

            /**
             * Get the default biome for the world.
             */
            String getBiome();

            /**
             * Whether ender-dragon fights should be enabled for islands or not.
             * Config-path: worlds.end.dragon-fight.enabled
             */
            boolean isDragonFight();

            /**
             * Get the offset of the portal from the center of the island.
             * Config-path: worlds.end.dragon-fight.portal-offset
             */
            BlockOffset getPortalOffset();

        }

    }

    interface Spawn {

        /**
         * The location of the spawn island.
         * Config-path: spawn.location
         */
        String getLocation();

        /**
         * Whether the spawn island has a protection or not.
         * Config-path: spawn.protection
         */
        boolean isProtected();

        /**
         * List of island-flags that will be enabled for the spawn island.
         * Config-path: spawn.settings
         */
        List<String> getSettings();

        /**
         * List of permissions that will be given to players for the spawn island.
         * Config-path: spawn.permissions
         */
        List<String> getPermissions();

        /**
         * Whether the spawn island has a world border or not.
         * Config-path: spawn.world-border
         */
        boolean isWorldBorder();

        /**
         * The size of the spawn island.
         * Config-path: spawn.size
         */
        int getSize();

        /**
         * Whether players should take damage in the spawn island or not.
         * Config-path: spawn.players-damage
         */
        boolean isPlayersDamage();

    }

    interface VoidTeleport {

        /**
         * Whether island members should be teleported when they fall into void on their island or not.
         * Config-path: void-teleport.members
         */
        boolean isMembers();

        /**
         * Whether visitors should be teleported when they fall into void on other islands or not.
         * Config-path: void-teleport.visitors
         */
        boolean isVisitors();

    }

    interface IslandNames {

        /**
         * Whether an island name is required for creating a new island or not.
         * Config-path: island-names.required-for-creation
         */
        boolean isRequiredForCreation();

        /**
         * The maximum length for island-names.
         * Config-path: island-names.max-length
         */
        int getMaxLength();

        /**
         * The minimum length for island-names.
         * Config-path: island-names.min-length
         */
        int getMinLength();

        /**
         * List of names that cannot be used.
         * Config-path: island-names.filtered-names
         */
        List<String> getFilteredNames();

        /**
         * Whether island-names should support colors or not.
         * Config-path: island-names.color-support
         */
        boolean isColorSupport();

        /**
         * Whether island names should be displayed in the island-top menu or not.
         * Config-path: island-names.island-top
         */
        boolean isIslandTop();

        /**
         * Whether the plugin should prevent from choosing player names as island names or not.
         * Config-path: island-names.prevent-player-names
         */
        boolean isPreventPlayerNames();

    }

    interface AFKIntegrations {

        /**
         * Whether redstone should be disabled when all island members are afk or not.
         * Config-path: afk-integrations.disable-redstone
         */
        boolean isDisableRedstone();

        /**
         * Whether mob spawning should be disabled when all island members are afk or not.
         * Config-path: afk-integrations.disable-spawning
         */
        boolean isDisableSpawning();

    }

    interface DefaultContainers {

        /**
         * Whether the default-containers system is enabled or not.
         * Config-path: default-containers.enabled
         */
        boolean isEnabled();

    }

    interface IslandChests {

        /**
         * The title to be shown for island chests.
         * Config-path: island-chests.chest-title
         */
        String getChestTitle();

        /**
         * The default pages new islands will have.
         * Config-path: island-chests.default-pages
         */
        int getDefaultPages();

        /**
         * The default size for chests.
         * Config-path: island-chests.default-size
         */
        int getDefaultSize();

    }


}
