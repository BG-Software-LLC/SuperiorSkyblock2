package com.bgsoftware.superiorskyblock.api.wrappers;

import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface SuperiorPlayer {

    /**
     * Get the UUID of the player.
     */
    UUID getUniqueId();

    /**
     * Get the last known name of the player.
     */
    String getName();

    /**
     * Get the last known skin-texture value of the player.
     */
    String getTextureValue();

    /**
     * Set the skin-texture value for the player.
     * @param textureValue The skin texture.
     */
    void setTextureValue(String textureValue);

    /**
     * Update the cached name with the current player's name.
     */
    void updateName();

    /**
     * Get the world that the player is inside.
     */
    World getWorld();

    /**
     * Get the location of the player.
     */
    Location getLocation();

    /**
     * Get the island owner of the player's island.
     */
    UUID getTeamLeader();

    /**
     * Set the island owner of the player's island.
     * !Can cause issues if not used properly!
     * @param teamLeader The island owner's uuid.
     */
    void setTeamLeader(UUID teamLeader);

    /**
     * Get the island of the player.
     */
    Island getIsland();

    /**
     * Get the role of the player.
     *
     * @deprecated See getPlayerRole()
     */
    @Deprecated
    IslandRole getIslandRole();

    /**
     * Get the role of the player.
     */
    PlayerRole getPlayerRole();

    /**
     * Set the role of the player.
     * @param islandRole The role to give the player.
     *
     * @deprecated see setPlayerRole(PlayerRole)
     */
    @Deprecated
    void setIslandRole(IslandRole islandRole);

    /**
     * Set the role of the player.
     * @param playerRole The role to give the player.
     */
    void setPlayerRole(PlayerRole playerRole);

    /**
     * Check whether or not the world border is enabled for the player.
     */
    boolean hasWorldBorderEnabled();

    /**
     * Toggle the world border for the player.
     */
    void toggleWorldBorder();

    /**
     * Check whether or not the blocks stacker mode is enabled for the player.
     */
    boolean hasBlocksStackerEnabled();

    /**
     * Toggle the blocks stacker for the player.
     */
    void toggleBlocksStacker();

    /**
     * Check whether or not the schematic mode is enabled for the player.
     */
    boolean hasSchematicModeEnabled();

    /**
     * Toggle the schematic mode for the player.
     */
    void toggleSchematicMode();

    /**
     * Check whether or not the team chat is enabled for the player.
     */
    boolean hasTeamChatEnabled();

    /**
     * Toggle the bypass mode for the player.
     */
    void toggleBypassMode();

    /**
     * Check whether or not the bypass mode is enabled for the player.
     */
    boolean hasBypassModeEnabled();

    /**
     * Toggle the team chat for the player.
     */
    void toggleTeamChat();

    /**
     * Get the first schematic position of the player. May be null.
     */
    BlockPosition getSchematicPos1();

    /**
     * Set the first schematic position of the player.
     * @param block The block to change the position to.
     */
    void setSchematicPos1(Block block);

    /**
     * Get the second schematic position of the player. May be null.
     */
    BlockPosition getSchematicPos2();

    /**
     * Set the second schematic position of the player.
     * @param block The block to change the position to.
     */
    void setSchematicPos2(Block block);

    /**
     * Get the player object.
     */
    Player asPlayer();

    /**
     * Get the offline-player object.
     */
    OfflinePlayer asOfflinePlayer();

    /**
     * Check whether or not the player is online.
     */
    boolean isOnline();

    /**
     * Check whether or not the player has a permission.
     */
    boolean hasPermission(String permission);

    /**
     * Check whether or not the player has a permission on his island.
     */
    boolean hasPermission(IslandPermission permission);

    /**
     * Get the amount of left disbands.
     */
    int getDisbands();

    /**
     * Check whether or not the player has more disbands.
     */
    boolean hasDisbands();

    /**
     * Check whether or not the player has a permission.
     */
    void setDisbands(int disbands);

    /**
     * Set whether or not the player has their panel toggled.
     */
    void setToggledPanel(boolean toggledPanel);

    /**
     * Check whether or not the player has their panel toggled.
     */
    boolean hasToggledPanel();

    /**
     * Set whether or not the player has flying enabled.
     */
    boolean hasIslandFlyEnabled();

    /**
     * Toggle flying mode.
     */
    void toggleIslandFly();

    /**
     * Check whether or not the player has admin spy mode enabled.
     */
    boolean hasAdminSpyEnabled();

    /**
     * Toggle admin spy mode.
     */
    void toggleAdminSpy();

    /**
     * Check whether or not the player is inside their island.
     */
    boolean isInsideIsland();

    /**
     * Get the border color of the player.
     */
    BorderColor getBorderColor();

    /**
     * Set the border color for the player.
     * @param borderColor The color to set.
     */
    void setBorderColor(BorderColor borderColor);

    /**
     * Update the last time player joined or left the server.
     */
    void updateLastTimeStatus();

    /**
     * Get the last time player joined or left the server.
     */
    long getLastTimeStatus();

    /**
     * Complete a mission.
     * @param mission The mission to complete.
     */
    void completeMission(Mission mission);

    /**
     * Reset a mission.
     * @param mission The mission to reset.
     */
    void resetMission(Mission mission);

    /**
     * Check whether the island has completed the mission before.
     * @param mission The mission to check.
     */
    boolean hasCompletedMission(Mission mission);

    /**
     * Get the list of the completed missions of the player.
     */
    List<Mission> getCompletedMissions();

}
