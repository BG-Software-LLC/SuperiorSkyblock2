package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public interface Island extends Comparable<Island> {

    SuperiorPlayer getOwner();

    List<UUID> getMembers();

    boolean isSpawn();

    void inviteMember(SuperiorPlayer superiorPlayer);

    void revokeInvite(SuperiorPlayer superiorPlayer);

    boolean isInvited(SuperiorPlayer superiorPlayer);

    void addMember(SuperiorPlayer superiorPlayer, IslandRole islandRole);

    void kickMember(SuperiorPlayer superiorPlayer);

    void banMember(SuperiorPlayer superiorPlayer);

    void unbanMember(SuperiorPlayer superiorPlayer);

    boolean isBanned(SuperiorPlayer superiorPlayer);

    List<UUID> getAllBannedMembers();

    List<UUID> getAllMembers();

    List<UUID> getVisitors();

    List<UUID> allPlayersInside();

    boolean isMember(SuperiorPlayer superiorPlayer);

    Location getCenter();

    Location getTeleportLocation();

    Location getVisitorsLocation();

    void setTeleportLocation(Location teleportLocation);

    void setVisitorsLocation(Location visitorsLocation);

    Location getMinimum();

    Location getMaximum();

    boolean hasPermission(CommandSender sender, IslandPermission islandPermission);

    boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission);

    void setPermission(IslandRole islandRole, IslandPermission islandPermission, boolean value);

    void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value);

    PermissionNode getPermisisonNode(IslandRole islandRole);

    PermissionNode getPermisisonNode(SuperiorPlayer superiorPlayer);

    IslandRole getRequiredRole(IslandPermission islandPermission);

    void disbandIsland();

    List<Chunk> getAllChunks();

    List<Chunk> getAllChunks(boolean onlyProtected);

    @Deprecated
    double getMoneyInBank();

    BigDecimal getMoneyInBankAsBigDecimal();

    @Deprecated
    String getMoneyAsString();

    void depositMoney(double amount);

    void withdrawMoney(double amount);

    void calcIslandWorth(SuperiorPlayer asker);

    void handleBlockPlace(Block block);

    void handleBlockPlace(Block block, int amount);

    void handleBlockPlace(Block block, int amount, boolean save);

    void handleBlockPlace(Key key, int amount);

    void handleBlockPlace(Key key, int amount, boolean save);

    void handleBlockBreak(Block block);

    void handleBlockBreak(Block block, int amount);

    void handleBlockBreak(Block block, int amount, boolean save);

    void handleBlockBreak(Key key, int amount);

    void handleBlockBreak(Key key, int amount, boolean save);

    @Deprecated
    int getHoppersAmount();

    int getBlockCount(Key key);

    @Deprecated
    double getWorth();

    @Deprecated
    double getRawWorth();

    BigDecimal getWorthAsBigDecimal();

    BigDecimal getRawWorthAsBigDecimal();

    @Deprecated
    String getWorthAsString();

    void setBonusWorth(BigDecimal bonusWorth);

    @Deprecated
    int getIslandLevel();

    BigDecimal getIslandLevelAsBigDecimal();

    @Deprecated
    String getLevelAsString();

    boolean isInside(Location location);

    boolean isInsideRange(Location location);

    int getUpgradeLevel(String upgradeName);

    void setUpgradeLevel(String upgradeName, int level);

    void updateBorder();

    int getIslandSize();

    @Deprecated
    int getHoppersLimit();

    int getBlockLimit(Key key);

    int getTeamLimit();

    double getCropGrowthMultiplier();

    double getSpawnerRatesMultiplier();

    double getMobDropsMultiplier();

    void setIslandSize(int islandSize);

    @Deprecated
    void setHoppersLimit(int hoppersLimit);

    void setBlockLimit(Key key, int limit);

    void setTeamLimit(int teamLimit);

    void setCropGrowthMultiplier(double cropGrowth);

    void setSpawnerRatesMultiplier(double spawnerRates);

    void setMobDropsMultiplier(double mobDrops);

    String getDiscord();

    void setDiscord(String discord);

    String getPaypal();

    void setPaypal(String paypal);

    void setBiome(Biome biome);

    void sendMessage(String message, UUID... ignoredMembers);

    Location getWarpLocation(String name);

    boolean isWarpPrivate(String name);

    @Deprecated
    void setWarpLocation(String name, Location location);

    void setWarpLocation(String name, Location location, boolean privateFlag);

    void warpPlayer(SuperiorPlayer superiorPlayer, String warp);

    void deleteWarp(SuperiorPlayer superiorPlayer, Location location);

    void deleteWarp(String name);

    List<String> getAllWarps();

    boolean hasMoreWarpSlots();

    void setWarpsLimit(int warpsLimit);

    int getWarpsLimit();

    boolean transferIsland(SuperiorPlayer superiorPlayer);

    @Deprecated
    void transfer(SuperiorPlayer player);

    boolean isLocked();

    void setLocked(boolean locked);

    String getName();

    void setName(String islandName);

    String getDescription();

    void setDescription(String description);

    Rating getRating(UUID uuid);

    void setRating(UUID uuid, Rating rating);

    double getTotalRating();

    int getRatingAmount();

    Map<UUID, Rating> getRatings();

    void completeMission(Mission mission);

    void resetMission(Mission mission);

    boolean hasCompletedMission(Mission mission);

}