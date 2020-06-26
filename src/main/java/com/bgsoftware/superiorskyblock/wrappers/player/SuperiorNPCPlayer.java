package com.bgsoftware.superiorskyblock.wrappers.player;

import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public final class SuperiorNPCPlayer implements SuperiorPlayer {

    private final Entity npc;

    public SuperiorNPCPlayer(Entity npc){
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

    }

    @Override
    public void updateName() {

    }

    @Override
    public Locale getUserLocale() {
        return com.bgsoftware.superiorskyblock.Locale.getDefaultLocale();
    }

    @Override
    public void setUserLocale(Locale locale) {

    }

    @Override
    public World getWorld() {
        return getLocation().getWorld();
    }

    @Override
    public Location getLocation() {
        return npc.getLocation();
    }

    @Override
    public void teleport(Location location) {
        npc.teleport(location);
    }

    @Override
    public void teleport(Island island) {
        teleport(island, null);
    }

    @Override
    public void teleport(Island island, Consumer<Boolean> consumerResult) {
        boolean result = npc.teleport(island.getCenter(World.Environment.NORMAL));
        if(consumerResult != null)
            consumerResult.accept(result);
    }

    @Override
    public UUID getTeamLeader() {
        return npc.getUniqueId();
    }

    @Override
    public SuperiorPlayer getIslandLeader() {
        return this;
    }

    @Override
    public void setTeamLeader(UUID teamLeader) {

    }

    @Override
    public void setIslandLeader(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public Island getIsland() {
        return null;
    }

    @Override
    public PlayerRole getPlayerRole() {
        return SPlayerRole.guestRole();
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {

    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return false;
    }

    @Override
    public void toggleWorldBorder() {

    }

    @Override
    public boolean hasBlocksStackerEnabled() {
        return false;
    }

    @Override
    public void toggleBlocksStacker() {

    }

    @Override
    public boolean hasSchematicModeEnabled() {
        return false;
    }

    @Override
    public void toggleSchematicMode() {

    }

    @Override
    public boolean hasTeamChatEnabled() {
        return false;
    }

    @Override
    public void toggleBypassMode() {

    }

    @Override
    public boolean hasBypassModeEnabled() {
        return false;
    }

    @Override
    public void toggleTeamChat() {

    }

    @Override
    public BlockPosition getSchematicPos1() {
        return null;
    }

    @Override
    public void setSchematicPos1(Block block) {

    }

    @Override
    public BlockPosition getSchematicPos2() {
        return null;
    }

    @Override
    public void setSchematicPos2(Block block) {

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
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        return false;
    }

    @Override
    public boolean hasPermission(IslandPermission permission) {
        return false;
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission) {
        return false;
    }

    @Override
    public int getDisbands() {
        return 0;
    }

    @Override
    public boolean hasDisbands() {
        return false;
    }

    @Override
    public void setDisbands(int disbands) {

    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {

    }

    @Override
    public boolean hasToggledPanel() {
        return false;
    }

    @Override
    public boolean hasIslandFlyEnabled() {
        return false;
    }

    @Override
    public void toggleIslandFly() {

    }

    @Override
    public boolean hasAdminSpyEnabled() {
        return false;
    }

    @Override
    public void toggleAdminSpy() {

    }

    @Override
    public boolean isInsideIsland() {
        return false;
    }

    @Override
    public BorderColor getBorderColor() {
        return BorderColor.BLUE;
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {

    }

    @Override
    public void updateLastTimeStatus() {

    }

    @Override
    public long getLastTimeStatus() {
        return System.currentTimeMillis() / 1000;
    }

    @Override
    public void completeMission(Mission<?> mission) {

    }

    @Override
    public void resetMission(Mission<?> mission) {

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
    public List<Mission<?>> getCompletedMissions() {
        return new ArrayList<>();
    }
}
