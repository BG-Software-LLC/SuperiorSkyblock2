package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import org.bukkit.Location;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobalSection extends SettingsContainerHolder {

    public long getCalcInterval() {
        return getContainer().calcInterval;
    }

    public String getIslandCommand() {
        return getContainer().islandCommand;
    }

    public int getMaxIslandSize() {
        return getContainer().maxIslandSize;
    }

    public int getIslandHeight() {
        return getContainer().islandsHeight;
    }

    public boolean isWorldBorders() {
        return getContainer().worldBordersEnabled;
    }

    public String getIslandLevelFormula() {
        return getContainer().islandLevelFormula;
    }

    public boolean isRoundedIslandLevels() {
        return getContainer().roundedIslandLevel;
    }

    public String getIslandTopOrder() {
        return getContainer().islandTopOrder;
    }

    public boolean isCoopMembers() {
        return getContainer().coopMembers;
    }

    public String getSignWarpLine() {
        return getContainer().signWarpLine;
    }

    public List<String> getSignWarp() {
        return getContainer().signWarp;
    }

    public List<String> getInteractables() {
        return getContainer().interactables;
    }

    public Collection<Key> getSafeBlocks() {
        return getContainer().safeBlocks;
    }

    public boolean isVisitorsDamage() {
        return getContainer().visitorsDamage;
    }

    public boolean isCoopDamage() {
        return getContainer().coopDamage;
    }

    public int getDisbandCount() {
        return getContainer().disbandCount;
    }

    public boolean isIslandTopIncludeLeader() {
        return getContainer().islandTopIncludeLeader;
    }

    public Map<String, String> getDefaultPlaceholders() {
        return getContainer().defaultPlaceholders;
    }

    public boolean isBanConfirm() {
        return getContainer().banConfirm;
    }

    public boolean isDisbandConfirm() {
        return getContainer().disbandConfirm;
    }

    public boolean isKickConfirm() {
        return getContainer().kickConfirm;
    }

    public boolean isLeaveConfirm() {
        return getContainer().leaveConfirm;
    }

    public String getSpawnersProvider() {
        return getContainer().spawnersProvider;
    }

    public String getStackedBlocksProvider() {
        return getContainer().stackedBlocksProvider;
    }

    public boolean isDisbandInventoryClear() {
        return getContainer().disbandInventoryClear;
    }

    public boolean isTeleportOnJoin() {
        return getContainer().teleportOnJoin;
    }

    public boolean isTeleportOnKick() {
        return getContainer().teleportOnKick;
    }

    public boolean isClearOnJoin() {
        return getContainer().clearOnJoin;
    }

    public boolean isRateOwnIsland() {
        return getContainer().rateOwnIsland;
    }

    public List<String> getDefaultSettings() {
        return getContainer().defaultSettings;
    }

    public boolean isDisableRedstoneOffline() {
        return getContainer().disableRedstoneOffline;
    }

    public Map<String, Pair<Integer, String>> getCommandsCooldown() {
        return getContainer().commandsCooldown;
    }

    public long getUpgradeCooldown() {
        return getContainer().upgradeCooldown;
    }

    public String getNumbersFormat() {
        return getContainer().numberFormat;
    }

    public String getDateFormat() {
        return getContainer().dateFormat;
    }

    public boolean isSkipOneItemMenus() {
        return getContainer().skipOneItemMenus;
    }

    public boolean isTeleportOnPvPEnable() {
        return getContainer().teleportOnPVPEnable;
    }

    public boolean isImmuneToPvPWhenTeleport() {
        return getContainer().immuneToPVPWhenTeleport;
    }

    public List<String> getBlockedVisitorsCommands() {
        return getContainer().blockedVisitorsCommands;
    }

    public List<String> getDefaultSign() {
        return getContainer().defaultSignLines;
    }

    public Map<String, List<String>> getEventCommands() {
        return getContainer().eventCommands;
    }

    public long getWarpsWarmup() {
        return getContainer().warpsWarmup;
    }

    public long getHomeWarmup() {
        return getContainer().homeWarmup;
    }

    public long getVisitWarmup() {
        return getContainer().visitWarmup;
    }

    public boolean isLiquidUpdate() {
        return getContainer().liquidUpdate;
    }

    public boolean isLightsUpdate() {
        return getContainer().lightsUpdate;
    }

    public List<String> getPvPWorlds() {
        return getContainer().pvpWorlds;
    }

    public boolean isStopLeaving() {
        return getContainer().stopLeaving;
    }

    public boolean isValuesMenu() {
        return getContainer().valuesMenu;
    }

    public List<String> getCropsToGrow() {
        return getContainer().cropsToGrow;
    }

    public int getCropsInterval() {
        return getContainer().cropsInterval;
    }

    public boolean isOnlyBackButton() {
        return getContainer().onlyBackButton;
    }

    public boolean isBuildOutsideIsland() {
        return getContainer().buildOutsideIsland;
    }

    public String getDefaultLanguage() {
        return getContainer().defaultLanguage;
    }

    public boolean isDefaultWorldBorder() {
        return getContainer().defaultWorldBorder;
    }

    public boolean isDefaultStackedBlocks() {
        return getContainer().defaultBlocksStacker;
    }

    public boolean isDefaultToggledPanel() {
        return getContainer().defaultToggledPanel;
    }

    public boolean isDefaultIslandFly() {
        return getContainer().defaultIslandFly;
    }

    public String getDefaultBorderColor() {
        return getContainer().defaultBorderColor;
    }

    public boolean isObsidianToLava() {
        return getContainer().obsidianToLava;
    }

    public BlockValuesManager.SyncWorthStatus getSyncWorth() {
        return getContainer().syncWorth;
    }

    public boolean isNegativeWorth() {
        return getContainer().negativeWorth;
    }

    public boolean isNegativeLevel() {
        return getContainer().negativeLevel;
    }

    public List<String> getDisabledEvents() {
        return getContainer().disabledEvents;
    }

    public List<String> getDisabledCommands() {
        return getContainer().disabledCommands;
    }

    public List<String> getDisabledHooks() {
        return getContainer().disabledHooks;
    }

    public boolean isSchematicNameArgument() {
        return getContainer().schematicNameArgument;
    }

    public Map<String, List<String>> getCommandAliases() {
        return getContainer().commandAliases;
    }

    public Set<Key> getValuableBlocks() {
        return getContainer().valuableBlocks;
    }

    public Map<String, Location> getPreviewIslands() {
        return getContainer().islandPreviewLocations;
    }

    public boolean isTabCompleteHideVanished() {
        return getContainer().tabCompleteHideVanished;
    }

    public boolean isDropsUpgradePlayersMultiply() {
        return getContainer().dropsUpgradePlayersMultiply;
    }

    public long getProtectedMessageDelay() {
        return getContainer().protectedMessageDelay;
    }

    public boolean isWarpCategories() {
        return getContainer().warpCategories;
    }

    public boolean isPhysicsListener() {
        return getContainer().physicsListener;
    }

    public double getChargeOnWarp() {
        return getContainer().chargeOnWarp;
    }

    public boolean isPublicWarps() {
        return getContainer().publicWarps;
    }

    public long getRecalcTaskTimeout() {
        return getContainer().recalcTaskTimeout;
    }

    public boolean isAutoLanguageDetection() {
        return getContainer().autoLanguageDetection;
    }

    public boolean isAutoUncoopWhenAlone() {
        return getContainer().autoUncoopWhenAlone;
    }

    public TopIslandMembersSorting getTopIslandMembersSorting() {
        return getContainer().islandTopMembersSorting;
    }

    public int getBossbarLimit() {
        return getContainer().bossBarLimit;
    }

    public boolean getDeleteUnsafeWarps() {
        return getContainer().deleteUnsafeWarps;
    }

    public List<RespawnAction> getPlayerRespawn() {
        return getContainer().playerRespawnActions;
    }

    public BigInteger getBlockCountsSaveThreshold() {
        return getContainer().blockCountsSaveThreshold;
    }

}
