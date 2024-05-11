package com.bgsoftware.superiorskyblock.api.wrappers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.IDatabaseBridgeHolder;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
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

public interface SuperiorPlayer extends IMissionsHolder, IPersistentDataHolder, IDatabaseBridgeHolder {

    /*
     *   General Methods
     */

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
     *
     * @param textureValue The skin texture.
     */
    void setTextureValue(String textureValue);

    /**
     * Update the last time player joined or left the server.
     */
    void updateLastTimeStatus();

    /**
     * Set the last time player joined or left the server.
     *
     * @param lastTimeStatus The time to set.
     */
    void setLastTimeStatus(long lastTimeStatus);

    /**
     * Get the last time player joined or left the server.
     */
    long getLastTimeStatus();

    /**
     * Update the cached name with the current player's name.
     * When the player is offline, nothing will happen.
     */
    void updateName();

    /**
     * Set the cached name with the given name.
     * When the player will join the server, it will be synced again with his correct name.
     */
    void setName(String name);

    /**
     * Get the player object.
     */
    @Nullable
    Player asPlayer();

    /**
     * Get the offline-player object.
     * This can return null if the player is invalid, for example - npcs.
     */
    @Nullable
    OfflinePlayer asOfflinePlayer();

    /**
     * Check whether or not the player is online.
     */
    boolean isOnline();

    /**
     * Execute code only if the player is online.
     */
    void runIfOnline(Consumer<Player> toRun);

    /**
     * Check whether or not this player is in a gamemode with fly mode enabled.
     * When the player is offline, false will be returned.
     */
    boolean hasFlyGamemode();

    /**
     * Get the opened view for the player.
     * When the player is offline or no view is opened, null will be returned.
     */
    @Nullable
    MenuView<?, ?> getOpenedView();

    /**
     * Check whether or not the player is AFK.
     * When the player is offline, false will be returned.
     */
    boolean isAFK();

    /**
     * Check whether or not the player is vanished.
     * When the player is offline, false will be returned.
     */
    boolean isVanished();

    /**
     * Checks whether the player is shown as online.
     *
     * @return false if vanished, in spectator mode or offline.
     */
    boolean isShownAsOnline();

    /**
     * Check whether or not the player has a permission.
     * When the player is offline, false will be returned.
     */
    boolean hasPermission(String permission);

    /**
     * Check whether or not the player has a permission without having op.
     * When the player is offline, false will be returned.
     */
    boolean hasPermissionWithoutOP(String permission);

    /**
     * Check whether or not the player has a permission on his island.
     * When the player doesn't have an island, false will be returned.
     */
    boolean hasPermission(IslandPrivilege permission);

    /**
     * Check whether or not this player can hit another player.
     * <p>
     * Players cannot hit each other if one of the followings is true:
     * 1) They are inside an island that has pvp disabled.
     * 2) One of them has pvp warm-up.
     * 3) They are both in the same island, and they hit each other outside of a pvp world.
     * 4) One of the players isn't online (duh?)
     * 5) The target player is inside an island as a visitor and can't take damage.
     * 6) The target player is inside an island as a coop and can't take damage.
     *
     * @param otherPlayer The other player to check.
     */
    HitActionResult canHit(SuperiorPlayer otherPlayer);

    /*
     *   Location Methods
     */

    /**
     * Get the world that the player is inside.
     * When the player is offline, null will be returned.
     */
    @Nullable
    World getWorld();

    /**
     * Get the location of the player.
     * When the player is offline, null will be returned.
     */
    @Nullable
    Location getLocation();

    /**
     * Teleport the player to a location.
     *
     * @param location The location to teleport the player to.
     */
    void teleport(Location location);

    /**
     * Teleport the player to a location.
     *
     * @param location       The location to teleport the player to.
     * @param teleportResult The result of the teleportation process.
     */
    void teleport(Location location, @Nullable Consumer<Boolean> teleportResult);

    /**
     * Teleport the player to an island.
     *
     * @param island The island to teleport the player to.
     */
    void teleport(Island island);

    /**
     * Teleport the player to an island.
     *
     * @param island      The island to teleport the player to.
     * @param environment The environment to teleport the player to.
     */
    void teleport(Island island, World.Environment environment);

    /**
     * Teleport the player to an island.
     *
     * @param island         The island to teleport the player to.
     * @param teleportResult Consumer that will be ran when task is finished.
     */
    void teleport(Island island, @Nullable Consumer<Boolean> teleportResult);

    /**
     * Teleport the player to an island.
     *
     * @param island         The island to teleport the player to.
     * @param environment    The environment to teleport the player to.
     * @param teleportResult Consumer that will be ran when task is finished.
     */
    void teleport(Island island, World.Environment environment, @Nullable Consumer<Boolean> teleportResult);

    /**
     * Check whether or not the player is inside their island.
     * When the player is offline or he doesn't have an island, false will be returned.
     */
    boolean isInsideIsland();

    /*
     *   Island Methods
     */

    /**
     * Get the island owner of the player's island.
     */
    SuperiorPlayer getIslandLeader();

    /**
     * Set the island owner of the player's island.
     * !Can cause issues if not used properly!
     *
     * @param islandLeader The island owner's player.
     * @deprecated see {@link #setIsland(Island)}
     */
    @Deprecated
    void setIslandLeader(SuperiorPlayer islandLeader);

    /**
     * Get the island of the player.
     */
    @Nullable
    Island getIsland();

    /**
     * Set the island of the player.
     * !Can cause issues if not used properly!
     *
     * @param island The island to set the player to.
     * @throws IllegalArgumentException if island doesn't contain player as a member.
     */
    void setIsland(Island island);

    /**
     * Check if this player is a member of an island.
     */
    boolean hasIsland();

    /**
     * Add an invitation to an island for the player.
     * Do not call use this method directly unless you know what you're doing.
     * Instead, use {@link Island#inviteMember(SuperiorPlayer)}
     *
     * @param island The island that invited the player.
     */
    void addInvite(Island island);

    /**
     * Remove an invitation from an island for the player.
     * Do not call use this method directly unless you know what you're doing.
     * Instead, use {@link Island#revokeInvite(SuperiorPlayer)} (SuperiorPlayer)}
     *
     * @param island The island to remove the invitation from.
     */
    void removeInvite(Island island);

    /**
     * Get all pending invites of the player.
     *
     * @return Pending invites, in the same order they were sent.
     */
    List<Island> getInvites();

    /**
     * Get the role of the player.
     */
    PlayerRole getPlayerRole();

    /**
     * Set the role of the player.
     *
     * @param playerRole The role to give the player.
     */
    void setPlayerRole(PlayerRole playerRole);

    /**
     * Get the amount of left disbands.
     */
    int getDisbands();

    /**
     * Check whether or not the player has a permission.
     */
    void setDisbands(int disbands);

    /**
     * Check whether or not the player has more disbands.
     */
    boolean hasDisbands();

    /*
     *   Preferences Methods
     */

    /**
     * Get the locale of the player.
     */
    Locale getUserLocale();

    /**
     * Set the locale of the player.
     *
     * @param locale The locale to set.
     */
    void setUserLocale(Locale locale);

    /**
     * Check whether the world border is enabled for the player.
     */
    boolean hasWorldBorderEnabled();

    /**
     * Toggle the world border for the player.
     */
    void toggleWorldBorder();

    /**
     * Set whether the world border is enabled for the player.
     *
     * @param enabled true to enable borders.
     */
    void setWorldBorderEnabled(boolean enabled);

    /**
     * Update world border for this player.
     *
     * @param island The island the player should see the border of.
     */
    void updateWorldBorder(@Nullable Island island);

    /**
     * Check whether the blocks-stacker mode is enabled for the player.
     */
    boolean hasBlocksStackerEnabled();

    /**
     * Toggle the blocks-stacker for the player.
     */
    void toggleBlocksStacker();

    /**
     * Set whether the blocks-stacker mode is enabled for the player.
     *
     * @param enabled true to enable blocks-stacker mode.
     */
    void setBlocksStacker(boolean enabled);

    /**
     * Check whether the schematic mode is enabled for the player.
     */
    boolean hasSchematicModeEnabled();

    /**
     * Toggle the schematic mode for the player.
     */
    void toggleSchematicMode();

    /**
     * Set whether the schematic mode is enabled for the player.
     *
     * @param enabled true to enable schematic mode.
     */
    void setSchematicMode(boolean enabled);

    /**
     * Check whether the team chat is enabled for the player.
     */
    boolean hasTeamChatEnabled();

    /**
     * Toggle the team chat for the player.
     */
    void toggleTeamChat();

    /**
     * Set whether the schematic mode is enabled for the player.
     *
     * @param enabled true to enable schematic mode.
     */
    void setTeamChat(boolean enabled);

    /**
     * Check whether the bypass mode is enabled for the player.
     */
    boolean hasBypassModeEnabled();

    /**
     * Toggle the bypass mode for the player.
     */
    void toggleBypassMode();

    /**
     * Set whether the bypass mode is enabled for the player.
     *
     * @param enabled true to enable bypass mode.
     */
    void setBypassMode(boolean enabled);

    /**
     * Check whether or not the player has their panel toggled.
     */
    boolean hasToggledPanel();

    /**
     * Set whether or not the player has their panel toggled.
     */
    void setToggledPanel(boolean toggledPanel);

    /**
     * Set whether the player has flying enabled.
     */
    boolean hasIslandFlyEnabled();

    /**
     * Toggle flying mode.
     */
    void toggleIslandFly();

    /**
     * Set whether the player has flying enabled.
     *
     * @param enabled true to enable flying.
     */
    void setIslandFly(boolean enabled);

    /**
     * Check whether the player has admin spy mode enabled.
     */
    boolean hasAdminSpyEnabled();

    /**
     * Toggle admin spy mode.
     */
    void toggleAdminSpy();

    /**
     * Set whether the player has admin spy mode enabled.
     *
     * @param enabled true to enable admin spy mode.
     */
    void setAdminSpy(boolean enabled);

    /**
     * Get the border color of the player.
     */
    BorderColor getBorderColor();

    /**
     * Set the border color for the player.
     *
     * @param borderColor The color to set.
     */
    void setBorderColor(BorderColor borderColor);

    /*
     *   Schematics Methods
     */

    /**
     * Get the first schematic position of the player. May be null.
     */
    BlockPosition getSchematicPos1();

    /**
     * Set the first schematic position of the player.
     *
     * @param block The block to change the position to.
     */
    void setSchematicPos1(@Nullable Block block);

    /**
     * Get the second schematic position of the player. May be null.
     */
    BlockPosition getSchematicPos2();

    /**
     * Set the second schematic position of the player.
     *
     * @param block The block to change the position to.
     */
    void setSchematicPos2(@Nullable Block block);

    /*
     *   Data Methods
     */

    /**
     * Whether the player is immuned to PvP or not.
     */
    @Deprecated
    boolean isImmunedToPvP();

    /**
     * Set immunity to PvP for this player.
     *
     * @param immunedToPvP Whether or not the player should be immuned to PvP.
     */
    @Deprecated
    void setImmunedToPvP(boolean immunedToPvP);

    /**
     * Whether the player has just left an island's area or not.
     */
    @Deprecated
    boolean isLeavingFlag();

    /**
     * Set whether or not the player has just left an island's area.
     * If set to true, the player will not be able to escape islands.
     *
     * @param leavingFlag Whether or not the island has left an island's area.
     */
    @Deprecated
    void setLeavingFlag(boolean leavingFlag);

    /**
     * Whether the player is immuned to portals or not.
     */
    @Deprecated
    boolean isImmunedToPortals();

    /**
     * Set whether or not the player is immuned to portals.
     * If set to true, players will not be able to get teleported through portals.
     *
     * @param immuneToPortals Whether the player should be immuned or not.
     */
    @Deprecated
    void setImmunedToPortals(boolean immuneToPortals);

    /**
     * Get the current active teleport task of the player.
     */
    @Nullable
    BukkitTask getTeleportTask();

    /**
     * Set a teleportation task for the player.
     * This is used for warmpups, etc.
     *
     * @param teleportTask The teleport task to set.
     */
    void setTeleportTask(@Nullable BukkitTask teleportTask);

    /**
     * Get the status of the player.
     *
     * @deprecated See {@link #hasPlayerStatus(PlayerStatus)}
     */
    @Deprecated
    PlayerStatus getPlayerStatus();

    /**
     * Set the status of the player.
     *
     * @param playerStatus The new status of the player.
     */
    void setPlayerStatus(PlayerStatus playerStatus);

    /**
     * Remove a status of the player.
     *
     * @param playerStatus The status to remove.
     */
    void removePlayerStatus(PlayerStatus playerStatus);

    /**
     * Check if player is in status.
     *
     * @param playerStatus The status to check.
     */
    boolean hasPlayerStatus(PlayerStatus playerStatus);

    /**
     * Merge another player into this object.
     */
    void merge(SuperiorPlayer otherPlayer);

    /**
     * Create a new builder for a {@link SuperiorPlayer} object.
     */
    static Builder newBuilder() {
        return SuperiorSkyblockAPI.getFactory().createPlayerBuilder();
    }

    /**
     * The {@link Builder} interface is used to create {@link SuperiorPlayer} objects with predefined values.
     * All of its methods are setters for all the values possible to create a player with.
     * Use {@link Builder#build()} to create the new {@link SuperiorPlayer} object. You must set
     * {@link Builder#setUniqueId(UUID)} before creating a new {@link SuperiorPlayer}
     */
    interface Builder {

        Builder setUniqueId(UUID uuid);

        UUID getUniqueId();

        Builder setName(String name);

        String getName();

        Builder setPlayerRole(PlayerRole playerRole);

        PlayerRole getPlayerRole();

        Builder setDisbands(int disbands);

        int getDisbands();

        Builder setLocale(Locale locale);

        Locale getLocale();

        Builder setTextureValue(String textureValue);

        String getTextureValue();

        Builder setLastTimeUpdated(long lastTimeUpdated);

        long getLastTimeUpdated();

        Builder setToggledPanel(boolean toggledPanel);

        boolean hasToggledPanel();

        Builder setIslandFly(boolean islandFly);

        boolean hasIslandFly();

        Builder setBorderColor(BorderColor borderColor);

        BorderColor getBorderColor();

        Builder setWorldBorderEnabled(boolean worldBorderEnabled);

        boolean hasWorldBorderEnabled();

        Builder setCompletedMission(Mission<?> mission, int finishCount);

        Map<Mission<?>, Integer> getCompletedMissions();

        Builder setPersistentData(byte[] persistentData);

        byte[] getPersistentData();

        SuperiorPlayer build();

    }

}
