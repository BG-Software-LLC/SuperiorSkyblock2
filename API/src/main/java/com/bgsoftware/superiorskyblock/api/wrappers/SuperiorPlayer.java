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

import java.util.UUID;

public interface SuperiorPlayer {

    UUID getUniqueId();

    String getName();

    String getTextureValue();

    void setTextureValue(String textureValue);

    void updateName();

    World getWorld();

    Location getLocation();

    UUID getTeamLeader();

    void setTeamLeader(UUID teamLeader);

    Island getIsland();

    @Deprecated
    IslandRole getIslandRole();

    PlayerRole getPlayerRole();

    @Deprecated
    void setIslandRole(IslandRole islandRole);

    void setPlayerRole(PlayerRole playerRole);

    boolean hasWorldBorderEnabled();

    void toggleWorldBorder();

    boolean hasBlocksStackerEnabled();

    void toggleBlocksStacker();

    boolean hasSchematicModeEnabled();

    void toggleSchematicMode();

    boolean hasTeamChatEnabled();

    void toggleBypassMode();

    boolean hasBypassModeEnabled();

    void toggleTeamChat();

    BlockPosition getSchematicPos1();

    void setSchematicPos1(Block block);

    BlockPosition getSchematicPos2();

    void setSchematicPos2(Block block);

    Player asPlayer();

    OfflinePlayer asOfflinePlayer();

    boolean isOnline();

    boolean hasPermission(String permission);

    boolean hasPermission(IslandPermission permission);

    int getDisbands();

    boolean hasDisbands();

    void setDisbands(int disbands);

    void setToggledPanel(boolean toggledPanel);

    boolean hasToggledPanel();

    boolean hasIslandFlyEnabled();

    void toggleIslandFly();

    boolean hasAdminSpyEnabled();

    void toggleAdminSpy();

    boolean isInsideIsland();

    BorderColor getBorderColor();

    void setBorderColor(BorderColor borderColor);

    void updateLastTimeStatus();

    long getLastTimeStatus();

    void completeMission(Mission mission);

    void resetMission(Mission mission);

    boolean hasCompletedMission(Mission mission);

}
