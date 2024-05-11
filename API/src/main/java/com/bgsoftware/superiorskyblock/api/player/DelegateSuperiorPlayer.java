package com.bgsoftware.superiorskyblock.api.player;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DelegateSuperiorPlayer implements SuperiorPlayer {

    protected final SuperiorPlayer handle;

    protected DelegateSuperiorPlayer(SuperiorPlayer handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUniqueId() {
        return this.handle.getUniqueId();
    }

    @Override
    public String getName() {
        return this.handle.getName();
    }

    @Override
    public String getTextureValue() {
        return this.handle.getTextureValue();
    }

    @Override
    public void setTextureValue(String textureValue) {
        this.handle.setTextureValue(textureValue);
    }

    @Override
    public void updateLastTimeStatus() {
        this.handle.updateLastTimeStatus();
    }

    @Override
    public void setLastTimeStatus(long lastTimeStatus) {
        this.handle.setLastTimeStatus(lastTimeStatus);
    }

    @Override
    public long getLastTimeStatus() {
        return this.handle.getLastTimeStatus();
    }

    @Override
    public void updateName() {
        this.handle.updateName();
    }

    @Override
    public void setName(String name) {
        this.handle.setName(name);
    }

    @Nullable
    @Override
    public Player asPlayer() {
        return this.handle.asPlayer();
    }

    @Nullable
    @Override
    public OfflinePlayer asOfflinePlayer() {
        return this.handle.asOfflinePlayer();
    }

    @Override
    public boolean isOnline() {
        return this.handle.isOnline();
    }

    @Override
    public void runIfOnline(Consumer<Player> toRun) {
        this.handle.runIfOnline(toRun);
    }

    @Override
    public boolean hasFlyGamemode() {
        return this.handle.hasFlyGamemode();
    }

    @Nullable
    @Override
    public MenuView<?, ?> getOpenedView() {
        return this.handle.getOpenedView();
    }

    @Override
    public boolean isAFK() {
        return this.handle.isAFK();
    }

    @Override
    public boolean isVanished() {
        return this.handle.isVanished();
    }

    @Override
    public boolean isShownAsOnline() {
        return this.handle.isShownAsOnline();
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        return this.handle.hasPermissionWithoutOP(permission);
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public HitActionResult canHit(SuperiorPlayer otherPlayer) {
        return this.handle.canHit(otherPlayer);
    }

    @Nullable
    @Override
    public World getWorld() {
        return this.handle.getWorld();
    }

    @Nullable
    @Override
    public Location getLocation() {
        return this.handle.getLocation();
    }

    @Override
    public void teleport(Location location) {
        this.handle.teleport(location);
    }

    @Override
    public void teleport(Location location, @Nullable Consumer<Boolean> teleportResult) {
        this.handle.teleport(location, teleportResult);
    }

    @Override
    public void teleport(Island island) {
        this.handle.teleport(island);
    }

    @Override
    public void teleport(Island island, World.Environment environment) {
        this.handle.teleport(island, environment);
    }

    @Override
    public void teleport(Island island, @Nullable Consumer<Boolean> teleportResult) {
        this.handle.teleport(island, teleportResult);
    }

    @Override
    public void teleport(Island island, World.Environment environment, @Nullable Consumer<Boolean> teleportResult) {
        this.handle.teleport(island, environment, teleportResult);
    }

    @Override
    public boolean isInsideIsland() {
        return this.handle.isInsideIsland();
    }

    @Override
    public SuperiorPlayer getIslandLeader() {
        return this.handle.getIslandLeader();
    }

    @Override
    @Deprecated
    public void setIslandLeader(SuperiorPlayer islandLeader) {
        this.handle.setIslandLeader(islandLeader);
    }

    @Nullable
    @Override
    public Island getIsland() {
        return this.handle.getIsland();
    }

    @Override
    public void setIsland(Island island) {
        this.handle.setIsland(island);
    }

    @Override
    public boolean hasIsland() {
        return this.handle.hasIsland();
    }

    @Override
    public void addInvite(Island island) {
        this.handle.addInvite(island);
    }

    @Override
    public void removeInvite(Island island) {
        this.handle.removeInvite(island);
    }

    @Override
    public List<Island> getInvites() {
        return this.handle.getInvites();
    }

    @Override
    public PlayerRole getPlayerRole() {
        return this.handle.getPlayerRole();
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        this.handle.setPlayerRole(playerRole);
    }

    @Override
    public int getDisbands() {
        return this.handle.getDisbands();
    }

    @Override
    public void setDisbands(int disbands) {
        this.handle.setDisbands(disbands);
    }

    @Override
    public boolean hasDisbands() {
        return this.handle.hasDisbands();
    }

    @Override
    public Locale getUserLocale() {
        return this.handle.getUserLocale();
    }

    @Override
    public void setUserLocale(Locale locale) {
        this.handle.setUserLocale(locale);
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return this.handle.hasWorldBorderEnabled();
    }

    @Override
    public void toggleWorldBorder() {
        this.handle.toggleWorldBorder();
    }

    @Override
    public void setWorldBorderEnabled(boolean enabled) {
        this.handle.setWorldBorderEnabled(enabled);
    }

    @Override
    public void updateWorldBorder(@Nullable Island island) {
        this.handle.updateWorldBorder(island);
    }

    @Override
    public boolean hasBlocksStackerEnabled() {
        return this.handle.hasBlocksStackerEnabled();
    }

    @Override
    public void toggleBlocksStacker() {
        this.handle.toggleBlocksStacker();
    }

    @Override
    public void setBlocksStacker(boolean enabled) {
        this.handle.setBlocksStacker(enabled);
    }

    @Override
    public boolean hasSchematicModeEnabled() {
        return this.handle.hasSchematicModeEnabled();
    }

    @Override
    public void toggleSchematicMode() {
        this.handle.toggleSchematicMode();
    }

    @Override
    public void setSchematicMode(boolean enabled) {
        this.handle.setSchematicMode(enabled);
    }

    @Override
    public boolean hasTeamChatEnabled() {
        return this.handle.hasTeamChatEnabled();
    }

    @Override
    public void toggleTeamChat() {
        this.handle.toggleTeamChat();
    }

    @Override
    public void setTeamChat(boolean enabled) {
        this.handle.setTeamChat(enabled);
    }

    @Override
    public boolean hasBypassModeEnabled() {
        return this.handle.hasBypassModeEnabled();
    }

    @Override
    public void toggleBypassMode() {
        this.handle.toggleBypassMode();
    }

    @Override
    public void setBypassMode(boolean enabled) {
        this.handle.setBypassMode(enabled);
    }

    @Override
    public boolean hasToggledPanel() {
        return this.handle.hasToggledPanel();
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        this.handle.setToggledPanel(toggledPanel);
    }

    @Override
    public boolean hasIslandFlyEnabled() {
        return this.handle.hasIslandFlyEnabled();
    }

    @Override
    public void toggleIslandFly() {
        this.handle.toggleIslandFly();
    }

    @Override
    public void setIslandFly(boolean enabled) {
        this.handle.setIslandFly(enabled);
    }

    @Override
    public boolean hasAdminSpyEnabled() {
        return this.handle.hasAdminSpyEnabled();
    }

    @Override
    public void toggleAdminSpy() {
        this.handle.toggleAdminSpy();
    }

    @Override
    public void setAdminSpy(boolean enabled) {
        this.handle.setAdminSpy(enabled);
    }

    @Override
    public BorderColor getBorderColor() {
        return this.handle.getBorderColor();
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {
        this.handle.setBorderColor(borderColor);
    }

    @Override
    public BlockPosition getSchematicPos1() {
        return this.handle.getSchematicPos1();
    }

    @Override
    public void setSchematicPos1(@Nullable Block block) {
        this.handle.setSchematicPos1(block);
    }

    @Override
    public BlockPosition getSchematicPos2() {
        return this.handle.getSchematicPos2();
    }

    @Override
    public void setSchematicPos2(@Nullable Block block) {
        this.handle.setSchematicPos2(block);
    }

    @Override
    @Deprecated
    public boolean isImmunedToPvP() {
        return this.handle.isImmunedToPvP();
    }

    @Override
    @Deprecated
    public void setImmunedToPvP(boolean immunedToPvP) {
        this.handle.setImmunedToPvP(immunedToPvP);
    }

    @Override
    @Deprecated
    public boolean isLeavingFlag() {
        return this.handle.isLeavingFlag();
    }

    @Override
    @Deprecated
    public void setLeavingFlag(boolean leavingFlag) {
        this.handle.setLeavingFlag(leavingFlag);
    }

    @Override
    @Deprecated
    public boolean isImmunedToPortals() {
        return this.handle.isImmunedToPortals();
    }

    @Override
    @Deprecated
    public void setImmunedToPortals(boolean immuneToPortals) {
        this.handle.setImmunedToPortals(immuneToPortals);
    }

    @Nullable
    @Override
    public BukkitTask getTeleportTask() {
        return this.handle.getTeleportTask();
    }

    @Override
    public void setTeleportTask(@Nullable BukkitTask teleportTask) {
        this.handle.setTeleportTask(teleportTask);
    }

    @Override
    @Deprecated
    public PlayerStatus getPlayerStatus() {
        return this.handle.getPlayerStatus();
    }

    @Override
    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.handle.setPlayerStatus(playerStatus);
    }

    @Override
    public void removePlayerStatus(PlayerStatus playerStatus) {
        this.handle.removePlayerStatus(playerStatus);
    }

    @Override
    public boolean hasPlayerStatus(PlayerStatus playerStatus) {
        return this.handle.hasPlayerStatus(playerStatus);
    }

    @Override
    public void merge(SuperiorPlayer otherPlayer) {
        this.handle.merge(otherPlayer);
    }

    @Override
    public void completeMission(Mission<?> mission) {
        this.handle.completeMission(mission);
    }

    @Override
    public void resetMission(Mission<?> mission) {
        this.handle.resetMission(mission);
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return this.handle.hasCompletedMission(mission);
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        return this.handle.canCompleteMissionAgain(mission);
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        return this.handle.getAmountMissionCompleted(mission);
    }

    @Override
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        this.handle.setAmountMissionCompleted(mission, finishCount);
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return this.handle.getCompletedMissions();
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return this.handle.getCompletedMissionsWithAmounts();
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return this.handle.getDatabaseBridge();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.handle.getPersistentDataContainer();
    }

    @Override
    public boolean isPersistentDataContainerEmpty() {
        return this.handle.isPersistentDataContainerEmpty();
    }

    @Override
    public void savePersistentDataContainer() {
        this.handle.savePersistentDataContainer();
    }

}
