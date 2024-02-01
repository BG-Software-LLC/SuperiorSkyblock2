package com.bgsoftware.superiorskyblock.player;

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
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.bridge.EmptyDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.persistence.EmptyPersistentDataContainer;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SuperiorNPCPlayer implements SuperiorPlayer {

    private final Entity npc;

    public SuperiorNPCPlayer(Entity npc) {
        this.npc = npc;
    }

    @Override
    public UUID getUniqueId() {
        return npc.getUniqueId();
    }

    @Override
    public String getName() {
        return "NPC-Citizens";
    }

    @Override
    public String getTextureValue() {
        return "";
    }

    @Override
    public void setTextureValue(String textureValue) {
        // Do nothing.
    }

    @Override
    public void updateLastTimeStatus() {
        // Do nothing.
    }

    @Override
    public void setLastTimeStatus(long lastTimeStatus) {
        // Do nothing.
    }

    @Override
    public long getLastTimeStatus() {
        return System.currentTimeMillis() / 1000;
    }

    @Override
    public void updateName() {
        // Do nothing.
    }

    @Override
    public void setName(String name) {
        // Do nothing.
    }

    @Override
    public Player asPlayer() {
        return null;
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void runIfOnline(Consumer<Player> toRun) {
        // Do nothing.
    }

    @Override
    public boolean hasFlyGamemode() {
        return false;
    }

    @Nullable
    @Override
    public MenuView<?, ?> getOpenedView() {
        return null;
    }

    @Override
    public boolean isAFK() {
        return false;
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public boolean isShownAsOnline() {
        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        return false;
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission) {
        return false;
    }

    @Override
    public HitActionResult canHit(SuperiorPlayer other) {
        return HitActionResult.NOT_ONLINE;
    }

    @Override
    public World getWorld() {
        return npc.getLocation().getWorld();
    }

    @Override
    public Location getLocation() {
        return npc.getLocation();
    }

    @Override
    public void teleport(Location location) {
        teleport(location, null);
    }

    @Override
    public void teleport(Location location, @Nullable Consumer<Boolean> teleportResult) {
        if (teleportResult != null)
            teleportResult.accept(false);
    }

    @Override
    public void teleport(Island island) {
        this.teleport(island, (Consumer<Boolean>) null);
    }

    @Override
    public void teleport(Island island, World.Environment environment) {
        this.teleport(island, environment, null);
    }

    @Override
    public void teleport(Island island, @Nullable Consumer<Boolean> teleportResult) {
        if (teleportResult != null)
            teleportResult.accept(false);
    }

    @Override
    public void teleport(Island island, World.Environment environment, @Nullable Consumer<Boolean> teleportResult) {
        if (teleportResult != null)
            teleportResult.accept(false);
    }

    @Override
    public boolean isInsideIsland() {
        return false;
    }

    @Override
    public SuperiorPlayer getIslandLeader() {
        return this;
    }

    @Override
    public void setIslandLeader(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public Island getIsland() {
        return null;
    }

    @Override
    public void setIsland(Island island) {
        // Do nothing.
    }

    @Override
    public boolean hasIsland() {
        return false;
    }

    @Override
    public void addInvite(Island island) {
        // Do nothing.
    }

    @Override
    public void removeInvite(Island island) {
        // Do nothing.
    }

    @Override
    public List<Island> getInvites() {
        return Collections.emptyList();
    }

    @Override
    public PlayerRole getPlayerRole() {
        return SPlayerRole.guestRole();
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        // Do nothing.
    }

    @Override
    public int getDisbands() {
        return 0;
    }

    @Override
    public void setDisbands(int disbands) {
        // Do nothing.
    }

    @Override
    public boolean hasDisbands() {
        return false;
    }

    @Override
    public Locale getUserLocale() {
        return PlayerLocales.getDefaultLocale();
    }

    @Override
    public void setUserLocale(Locale locale) {
        // Do nothing.
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return false;
    }

    @Override
    public void toggleWorldBorder() {
        // Do nothing.
    }

    @Override
    public void setWorldBorderEnabled(boolean enabled) {
        // Do nothing.
    }

    @Override
    public void updateWorldBorder(@Nullable Island island) {
        // Do nothing.
    }

    @Override
    public boolean hasBlocksStackerEnabled() {
        return false;
    }

    @Override
    public void toggleBlocksStacker() {
        // Do nothing.
    }

    @Override
    public void setBlocksStacker(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean hasSchematicModeEnabled() {
        return false;
    }

    @Override
    public void toggleSchematicMode() {
        // Do nothing.
    }

    @Override
    public void setSchematicMode(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean hasTeamChatEnabled() {
        return false;
    }

    @Override
    public void toggleTeamChat() {
        // Do nothing.
    }

    @Override
    public void setTeamChat(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean hasBypassModeEnabled() {
        return false;
    }

    @Override
    public void toggleBypassMode() {
        // Do nothing.
    }

    @Override
    public void setBypassMode(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean hasToggledPanel() {
        return false;
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        // Do nothing.
    }

    @Override
    public boolean hasIslandFlyEnabled() {
        return false;
    }

    @Override
    public void toggleIslandFly() {
        // Do nothing.
    }

    @Override
    public void setIslandFly(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean hasAdminSpyEnabled() {
        return false;
    }

    @Override
    public void toggleAdminSpy() {
        // Do nothing.
    }

    @Override
    public void setAdminSpy(boolean enabled) {
        // Do nothing.
    }

    @Override
    public BorderColor getBorderColor() {
        return BorderColor.BLUE;
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {
        // Do nothing.
    }

    @Override
    public BlockPosition getSchematicPos1() {
        return null;
    }

    @Override
    public void setSchematicPos1(Block block) {
        // Do nothing.
    }

    @Override
    public BlockPosition getSchematicPos2() {
        return null;
    }

    @Override
    public void setSchematicPos2(Block block) {
        // Do nothing.
    }

    @Nullable
    @Override
    public BukkitTask getTeleportTask() {
        return null;
    }

    @Override
    public void setTeleportTask(@Nullable BukkitTask teleportTask) {
        // Do nothing.
    }

    @Override
    public boolean isImmunedToPvP() {
        return false;
    }

    @Override
    public void setImmunedToPvP(boolean immunedToPvP) {
        // Do nothing.
    }

    @Override
    public boolean isLeavingFlag() {
        return false;
    }

    @Override
    public void setLeavingFlag(boolean leavingFlag) {
        // Do nothing.
    }

    @Override
    public boolean isImmunedToPortals() {
        return false;
    }

    @Override
    public void setImmunedToPortals(boolean immuneToPortals) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public PlayerStatus getPlayerStatus() {
        return PlayerStatus.NONE;
    }

    @Override
    public void setPlayerStatus(PlayerStatus playerStatus) {
        // Do nothing.
    }

    @Override
    public void removePlayerStatus(PlayerStatus playerStatus) {
        // Do nothing.
    }

    @Override
    public boolean hasPlayerStatus(PlayerStatus playerStatus) {
        return false;
    }

    @Override
    public void merge(SuperiorPlayer other) {
        // Do nothing.
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return EmptyDatabaseBridge.getInstance();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return EmptyPersistentDataContainer.getInstance();
    }

    @Override
    public boolean isPersistentDataContainerEmpty() {
        return true;
    }

    @Override
    public void savePersistentDataContainer() {
        // Do nothing.
    }

    @Override
    public void completeMission(Mission<?> mission) {
        // Do nothing.
    }

    @Override
    public void resetMission(Mission<?> mission) {
        // Do nothing.
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return false;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        return false;
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        return 0;
    }

    @Override
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        // Do nothing.
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return Collections.emptyList();
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return Collections.emptyMap();
    }
}
