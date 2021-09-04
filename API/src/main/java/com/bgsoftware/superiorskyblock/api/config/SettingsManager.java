package com.bgsoftware.superiorskyblock.api.config;

import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SettingsManager {

    long getCalcInterval();

    Database getDatabase();

    String getIslandCommand();

    int getMaxIslandSize();

    DefaultValues getDefaultValues();

    int getIslandHeight();

    boolean isWorldBorders();

    StackedBlocks getStackedBlocks();

    String getIslandLevelFormula();

    boolean isRoundedIslandLevels();

    String getIslandTopOrder();

    IslandRoles getIslandRoles();

    String getSignWarpLine();

    List<String> getSignWarp();

    VisitorsSign getVisitorsSign();

    Worlds getWorlds();

    Spawn getSpawn();

    VoidTeleport getVoidTeleport();

    List<String> getInteractables();

    boolean isVisitorsDamage();

    boolean isCoopDamage();

    int getDisbandCount();

    boolean isIslandTopIncludeLeader();

    Map<String, String> getDefaultPlaceholders();

    boolean isBanConfirm();

    boolean isDisbandConfirm();

    boolean isKickConfirm();

    boolean isLeaveConfirm();

    String getSpawnersProvider();

    String getStackedBlocksProvider();

    boolean isDisbandInventoryClear();

    IslandNames getIslandNames();

    boolean isTeleportOnJoin();

    boolean isTeleportOnKick();

    boolean isClearOnJoin();

    boolean isRateOwnIsland();

    List<String> getDefaultSettings();

    boolean isDisableRedstoneOffline();

    AFKIntegrations getAFKIntegrations();

    Map<String, Pair<Integer, String>> getCommandsCooldown();

    long getUpgradeCooldown();

    String getNumbersFormat();

    String getDateFormat();

    boolean isSkipOneItemMenus();

    boolean isTeleportOnPvPEnable();

    boolean isImmuneToPvPWhenTeleport();

    List<String> getBlockedVisitorsCommands();

    DefaultContainers getDefaultContainers();

    List<String> getDefaultSign();

    Map<String, List<String>> getEventCommands();

    long getWarpsWarmup();

    long getHomeWarmup();

    boolean isLiquidUpdate();

    boolean isLightsUpdate();

    List<String> getPvPWorlds();

    boolean isStopLeaving();

    boolean isValuesMenu();

    List<String> getCropsToGrow();

    int getCropsInterval();

    boolean isOnlyBackButton();

    boolean isBuildOutsideIsland();

    String getDefaultLanguage();

    boolean isDefaultWorldBorder();

    boolean isDefaultStackedBlocks();

    boolean isDefaultToggledPanel();

    boolean isDefaultIslandFly();

    String getDefaultBorderColor();

    boolean isObsidianToLava();

    BlockValuesManager.SyncWorthStatus getSyncWorth();

    boolean isNegativeWorth();

    boolean isNegativeLevel();

    List<String> getDisabledEvents();

    boolean isSchematicNameArgument();

    IslandChests getIslandChests();

    Map<String, List<String>> getCommandAliases();

    Set<Key> getValuableBlocks();

    Map<String, Location> getPreviewIslands();

    boolean isTabCompleteHideVanished();

    boolean isDropsUpgradePlayersMultiply();

    long getProtectedMessageDelay();

    boolean isWarpCategories();

    boolean isPhysicsListener();

    double getChargeOnWarp();

    boolean isPublicWarps();

    long getRecalcTaskTimeout();

    interface Database {

        String getType();

        String getAddress();

        int getPort();

        String getDBName();

        String getUsername();

        String getPassword();

        String getPrefix();

        boolean hasSSL();

        boolean hasPublicKeyRetrieval();

    }

    interface DefaultValues {

        int getIslandSize();

        Map<Key, Integer> getBlockLimits();

        Map<Key, Integer> getEntityLimits();

        int getWarpsLimit();

        int getTeamLimit();

        int getCoopLimit();

        double getCropGrowth();

        double getSpawnerRates();

        double getMobDrops();

        BigDecimal getBankLimit();

        Map<Key, Integer>[] getGenerators();

        Map<Integer, Integer> getRoleLimits();

    }

    interface StackedBlocks {

        boolean isEnabled();

        String getCustomName();

        List<String> getDisabledWorlds();

        Set<Key> getWhitelisted();

        Map<Key, Integer> getLimits();

        boolean isAutoCollect();

        DepositMenu getDepositMenu();

        interface DepositMenu {

            boolean isEnabled();

            String getTitle();

        }

    }

    interface IslandRoles {

        ConfigurationSection getSection();

    }

    interface VisitorsSign {

        String getLine();

        String getActive();

        String getInactive();

    }

    interface Worlds {

        World.Environment getDefaultWorld();

        String getWorldName();

        Normal getNormal();

        Nether getNether();

        End getEnd();

        String getDifficulty();

        interface Normal {

            boolean isEnabled();

            boolean isUnlocked();

            boolean isSchematicOffset();

        }

        interface Nether {

            boolean isEnabled();

            boolean isUnlocked();

            String getName();

            boolean isSchematicOffset();

        }

        interface End {

            boolean isEnabled();

            boolean isUnlocked();

            String getName();

            boolean isSchematicOffset();

            boolean isDragonFight();

        }

    }

    interface Spawn {

        String getLocation();

        boolean isProtected();

        List<String> getSettings();

        List<String> getPermissions();

        boolean isWorldBorder();

        int getSize();

        boolean isPlayersDamage();

    }

    interface VoidTeleport {

        boolean isMembers();

        boolean isVisitors();

    }

    interface IslandNames {

        boolean isRequiredForCreation();

        int getMaxLength();

        int getMinLength();

        List<String> getFilteredNames();

        boolean isColorSupport();

        boolean isIslandTop();

        boolean isPreventPlayerNames();

    }

    interface AFKIntegrations {

        boolean isDisableRedstone();

        boolean isDisableSpawning();

    }

    interface DefaultContainers {

        boolean isEnabled();

    }

    interface IslandChests {

        String getChestTitle();

        int getDefaultPages();

        int getDefaultSize();

    }



}
