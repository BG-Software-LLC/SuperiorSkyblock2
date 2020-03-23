package com.bgsoftware.superiorskyblock.wrappers;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.utils.database.CachedResultSet;
import com.bgsoftware.superiorskyblock.utils.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SSuperiorPlayer extends DatabaseObject implements SuperiorPlayer {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Registry<Mission, Integer> completedMissions = Registry.createRegistry();
    private final UUID player;

    private UUID islandLeaderFromCache;

    private SuperiorPlayer islandLeader;
    private String name, textureValue = "";
    private PlayerRole playerRole;
    private java.util.Locale userLocale;

    private boolean worldBorderEnabled = true;
    private boolean blocksStackerEnabled = true;
    private boolean schematicModeEnabled = false;
    private boolean bypassModeEnabled = false;
    private boolean teamChatEnabled = false;
    private SBlockPosition schematicPos1 = null, schematicPos2 = null;
    private int disbands;
    private boolean toggledPanel = false;
    private boolean islandFly = false;
    private boolean adminSpyEnabled = false;
    private BorderColor borderColor = BorderColor.BLUE;
    private long lastTimeStatus = -1;

    private boolean immuneToPvP = false;

    private BukkitTask teleportTask = null;

    public SSuperiorPlayer(CachedResultSet resultSet){
        player = UUID.fromString(resultSet.getString("player"));
        islandLeaderFromCache = UUID.fromString(resultSet.getString("teamLeader"));
        name = resultSet.getString("name");
        textureValue = resultSet.getString("textureValue");
        playerRole = SPlayerRole.of(resultSet.getString("islandRole"));
        disbands = resultSet.getInt("disbands");
        toggledPanel = resultSet.getBoolean("toggledPanel");
        islandFly = resultSet.getBoolean("islandFly");
        borderColor = BorderColor.valueOf(resultSet.getString("borderColor"));
        lastTimeStatus = resultSet.getLong("lastTimeStatus");
        IslandDeserializer.deserializeMissions(resultSet.getString("missions"), completedMissions);
        userLocale = LocaleUtils.getLocale(resultSet.getString("language"));
        worldBorderEnabled = resultSet.getBoolean("toggledBorder");
    }

    public SSuperiorPlayer(UUID player){
        OfflinePlayer offlinePlayer;
        this.player = player;
        this.name = (offlinePlayer = Bukkit.getOfflinePlayer(player)) == null || offlinePlayer.getName() == null ? "null" : offlinePlayer.getName();
        this.islandLeader = this;
        this.playerRole = SPlayerRole.guestRole();
        this.disbands = SuperiorSkyblockPlugin.getPlugin().getSettings().disbandCount;
        this.userLocale = LocaleUtils.getDefault();
    }

    @Override
    public UUID getUniqueId(){
        return player;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTextureValue() {
        return textureValue;
    }

    @Override
    public void setTextureValue(String textureValue) {
        this.textureValue = textureValue;
        Query.PLAYER_SET_TEXTURE.getStatementHolder()
                .setString(textureValue)
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public void updateName(){
        this.name = asPlayer().getName();
        Query.PLAYER_SET_NAME.getStatementHolder()
                .setString(name)
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public java.util.Locale getUserLocale() {
        if(userLocale == null)
            userLocale = LocaleUtils.getDefault();
        return userLocale;
    }

    @Override
    public void setUserLocale(java.util.Locale userLocale) {
        Preconditions.checkArgument(Locale.isValidLocale(userLocale), "Locale " + userLocale + " is not a valid locale.");

        this.userLocale = userLocale;

        Query.PLAYER_SET_LANGUAGE.getStatementHolder()
                .setString(userLocale.getLanguage() + "-" + userLocale.getCountry())
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public World getWorld(){
        return getLocation().getWorld();
    }

    @Override
    public Location getLocation(){
        return asPlayer().getLocation();
    }

    @Override
    public void teleport(Location location) {
        if(isOnline())
            asPlayer().teleport(location);
    }

    @Override
    public void teleport(Island island) {
        if(isOnline())
            teleport(island, null);
    }

    @Override
    public void teleport(Island island, Consumer<Boolean> result) {
        if(!isOnline())
            return;

        Location islandTeleportLocation = island.getTeleportLocation(World.Environment.NORMAL);
        Location islandCenterLocation = island.getCenter(World.Environment.NORMAL);

        if(island instanceof SpawnIsland){
            teleport(islandTeleportLocation.add(0, 0.5, 0));
            if(result != null)
                result.accept(true);
            return;
        }

        Location toTeleport = null;

        //We check if the island's teleport location is safe.
        if(LocationUtils.isSafeBlock(islandTeleportLocation.getBlock())){
            toTeleport = islandTeleportLocation;
        }

        //We check if the island's center location is safe.
        else if(LocationUtils.isSafeBlock(islandCenterLocation.getBlock())){
            island.setTeleportLocation(islandCenterLocation);
            toTeleport = islandCenterLocation;
        }

        //We check if the highest block at the island's center location is safe.
        else if(LocationUtils.isSafeBlock(islandCenterLocation.getWorld().getHighestBlockAt(islandCenterLocation))){
            island.setTeleportLocation(islandCenterLocation);
            toTeleport = islandCenterLocation;
        }

        //Checking if one of the options above is safe.
        if(toTeleport != null){
            teleport(toTeleport.add(0, 0.5, 0));
            if(result != null)
                result.accept(true);
            return;
        }

        /*
         *   Finding a new block to teleport the player to.
         */

        List<CompletableFuture<ChunkSnapshot>> chunksToLoad = island.getAllChunksAsync(World.Environment.NORMAL, true, true, (Consumer<Chunk>) null)
                .stream().map(future -> future.thenApply(Chunk::getChunkSnapshot)).collect(Collectors.toList());

        Executor.async(() -> {
            for(CompletableFuture<ChunkSnapshot> chunkToLoad : chunksToLoad){
                ChunkSnapshot chunkSnapshot;

                try {
                    chunkSnapshot = chunkToLoad.get();
                }catch(Exception ex){
                    SuperiorSkyblockPlugin.log("&cCouldn't load chunk!");
                    continue;
                }

                if (LocationUtils.isChunkEmpty(null, chunkSnapshot))
                    continue;

                for(int x = 0; x < 16; x++){
                    for(int z = 0; z < 16; z++){
                        int y = chunkSnapshot.getHighestBlockYAt(x, z);
                        Key blockKey = plugin.getNMSAdapter().getBlockKey(chunkSnapshot, x, y, z),
                                belowKey = plugin.getNMSAdapter().getBlockKey(chunkSnapshot, x, y == 0 ? 0 : y - 1, z);

                        Material blockType, belowType;

                        try {
                            blockType = Material.valueOf(blockKey.toString().split(":")[0]);
                            belowType = Material.valueOf(belowKey.toString().split(":")[0]);
                        }catch(IllegalArgumentException ex){
                            continue;
                        }

                        if(blockType.isSolid() || belowType.isSolid()){
                            Location islandLocation = new Location(Bukkit.getWorld(chunkSnapshot.getWorldName()),
                                    chunkSnapshot.getX() * 16 + x, y, chunkSnapshot.getZ() * 16 + z);

                            Executor.sync(() -> {
                                island.setTeleportLocation(islandLocation);
                                teleport(islandLocation.add(0.5, 0.5, 0.5));
                                if(result != null)
                                    result.accept(true);
                            });

                            return;
                        }
                    }
                }
            }

            if(result != null)
                Executor.sync(() -> result.accept(false));
        });
    }

    @Override
    public UUID getTeamLeader() {
        return getIslandLeader().getUniqueId();
    }

    @Override
    public SuperiorPlayer getIslandLeader() {
        if(islandLeaderFromCache != null){
            islandLeader = SSuperiorPlayer.of(islandLeaderFromCache);
            islandLeaderFromCache = null;
        }

        return islandLeader;
    }

    @Override
    public void setTeamLeader(UUID teamLeader) {
        setIslandLeader(SSuperiorPlayer.of(teamLeader));
    }

    @Override
    public void setIslandLeader(SuperiorPlayer superiorPlayer) {
        this.islandLeader = superiorPlayer;
        Query.PLAYER_SET_LEADER.getStatementHolder()
                .setString(islandLeader.getUniqueId().toString())
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public Island getIsland(){
        return plugin.getGrid().getIsland(this);
    }

    @Override
    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        this.playerRole = playerRole;
        Query.PLAYER_SET_ROLE.getStatementHolder()
                .setString(playerRole.toString())
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return worldBorderEnabled;
    }

    @Override
    public void toggleWorldBorder() {
        worldBorderEnabled = !worldBorderEnabled;
        Query.PLAYER_SET_TOGGLED_BORDER.getStatementHolder()
                .setBoolean(worldBorderEnabled)
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public boolean hasBlocksStackerEnabled() {
        return blocksStackerEnabled;
    }

    @Override
    public void toggleBlocksStacker() {
        blocksStackerEnabled = !blocksStackerEnabled;
    }

    @Override
    public boolean hasSchematicModeEnabled() {
        return schematicModeEnabled;
    }

    @Override
    public void toggleSchematicMode() {
        schematicModeEnabled = !schematicModeEnabled;
    }

    @Override
    public boolean hasTeamChatEnabled() {
        return teamChatEnabled;
    }

    @Override
    public void toggleBypassMode(){
        bypassModeEnabled = !bypassModeEnabled;
    }

    @Override
    public boolean hasBypassModeEnabled() {
        if(bypassModeEnabled && isOnline() && !asPlayer().hasPermission("superior.admin.bypass"))
            bypassModeEnabled = false;

        return bypassModeEnabled;
    }

    @Override
    public void toggleTeamChat() {
        teamChatEnabled = !teamChatEnabled;
    }

    @Override
    public boolean hasDisbands() {
        return disbands > 0;
    }

    @Override
    public int getDisbands() {
        return disbands;
    }

    @Override
    public void setDisbands(int disbands) {
        this.disbands = Math.max(disbands, 0);
        Query.PLAYER_SET_DISBANDS.getStatementHolder()
                .setInt(disbands)
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        this.toggledPanel = toggledPanel;
        Query.PLAYER_SET_TOGGLED_PANEL.getStatementHolder()
                .setBoolean(toggledPanel)
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public boolean hasToggledPanel() {
        return toggledPanel;
    }

    @Override
    public boolean hasIslandFlyEnabled(){
        if(islandFly && isOnline() && !asPlayer().hasPermission("superior.island.fly"))
            toggleIslandFly();

        return islandFly;
    }

    @Override
    public void toggleIslandFly(){
        islandFly = !islandFly;
        Query.PLAYER_SET_ISLAND_FLY.getStatementHolder()
                .setBoolean(islandFly)
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public boolean hasAdminSpyEnabled() {
        return adminSpyEnabled;
    }

    @Override
    public void toggleAdminSpy() {
        adminSpyEnabled = !adminSpyEnabled;
    }

    @Override
    public boolean isInsideIsland() {
        return isOnline() && plugin.getGrid().getIslandAt(getLocation()).equals(getIsland());
    }

    @Override
    public BorderColor getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {
        this.borderColor = borderColor;

        Query.PLAYER_SET_BORDER.getStatementHolder()
                .setString(borderColor.name())
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public void updateLastTimeStatus() {
        lastTimeStatus = System.currentTimeMillis() / 1000;

        Query.PLAYER_SET_LAST_STATUS.getStatementHolder()
                .setString(lastTimeStatus + "")
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public long getLastTimeStatus() {
        return lastTimeStatus;
    }

    @Override
    public void completeMission(Mission mission) {
        completedMissions.add(mission, completedMissions.get(mission, 0) + 1);

        Query.PLAYER_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public void resetMission(Mission mission) {
        if(completedMissions.get(mission, 0) > 0) {
            completedMissions.add(mission, completedMissions.get(mission) - 1);
        }
        else {
            completedMissions.remove(mission);
        }

        mission.clearData(this);

        Query.PLAYER_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public boolean hasCompletedMission(Mission mission) {
        return completedMissions.containsKey(mission);
    }

    @Override
    public boolean canCompleteMissionAgain(Mission mission) {
        MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission);
        return getAmountMissionCompleted(mission) < missionData.resetAmount;
    }

    @Override
    public int getAmountMissionCompleted(Mission mission) {
        return completedMissions.get(mission, 0);
    }

    @Override
    public List<Mission> getCompletedMissions() {
        return new ArrayList<>(completedMissions.keys());
    }

    @Override
    public BlockPosition getSchematicPos1() {
        return schematicPos1;
    }

    @Override
    public void setSchematicPos1(Block block) {
        this.schematicPos1 = block == null ? null : SBlockPosition.of(block.getLocation());
    }

    @Override
    public SBlockPosition getSchematicPos2() {
        return schematicPos2;
    }

    @Override
    public void setSchematicPos2(Block block) {
        this.schematicPos2 = block == null ? null : SBlockPosition.of(block.getLocation());
    }

    @Override
    public Player asPlayer(){
        return Bukkit.getPlayer(player);
    }

    @Override
    public OfflinePlayer asOfflinePlayer(){
        return Bukkit.getOfflinePlayer(player);
    }

    @Override
    public boolean isOnline(){
        OfflinePlayer offlinePlayer = asOfflinePlayer();
        return offlinePlayer != null && offlinePlayer.isOnline();
    }

    @Override
    public boolean hasPermission(String permission){
        return permission.isEmpty() || asPlayer().hasPermission(permission);
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        Player player = asPlayer();
        return player != null && player.getEffectivePermissions().stream().anyMatch(permissionAttachmentInfo -> permissionAttachmentInfo.getPermission().equalsIgnoreCase(permission));
    }

    @Override
    @Deprecated
    public boolean hasPermission(IslandPermission permission) {
        return hasPermission(IslandPrivilege.getByName(permission.name()));
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission){
        Island island = getIsland();
        return island != null && island.hasPermission(this, permission);
    }

    public boolean isImmunedToPvP(){
        return immuneToPvP;
    }

    public void setImmunedToPvP(boolean immunedToPvP){
        this.immuneToPvP = immunedToPvP;
    }

    public void setTeleportTask(BukkitTask teleportTask){
        this.teleportTask = teleportTask;
    }

    public BukkitTask getTeleportTask(){
        return teleportTask;
    }

    @Override
    public void executeUpdateStatement(boolean async) {
        Query.PLAYER_UPDATE.getStatementHolder()
                .setString(islandLeader.getUniqueId().toString())
                .setString(name)
                .setString(playerRole.toString())
                .setString(textureValue)
                .setInt(disbands)
                .setBoolean(toggledPanel)
                .setBoolean(islandFly)
                .setString(borderColor.name())
                .setString(lastTimeStatus + "")
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(userLocale.getLanguage() + "-" + userLocale.getCountry())
                .setBoolean(worldBorderEnabled)
                .setString(player.toString())
                .execute(async);
    }

    @Override
    public void executeInsertStatement(boolean async) {
        Query.PLAYER_INSERT.getStatementHolder()
                .setString(player.toString())
                .setString(islandLeader.getUniqueId().toString())
                .setString(name)
                .setString(playerRole.toString())
                .setString(textureValue)
                .setInt(plugin.getSettings().disbandCount)
                .setBoolean(toggledPanel)
                .setBoolean(islandFly)
                .setString(borderColor.name())
                .setString(lastTimeStatus + "")
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(userLocale.getLanguage() + "-" + userLocale.getCountry())
                .setBoolean(worldBorderEnabled)
                .execute(async);
    }

    @Override
    public void executeDeleteStatement(boolean async) {
        throw new UnsupportedOperationException("You cannot use delete statement on superior players.");
    }

    @Override
    public String toString() {
        return "SSuperiorPlayer{" +
                "uuid=[" + player + "]," +
                "name=[" + name + "]" +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperiorPlayer && player.equals(((SSuperiorPlayer) obj).player);
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    public static SuperiorPlayer of(CommandSender sender){
        Preconditions.checkArgument(sender != null, "CommandSender cannot be null.");
        return of((Player) sender);
    }

    public static SuperiorPlayer of(Player player){
        Preconditions.checkArgument(player != null, "Player cannot be null.");
        Preconditions.checkArgument(!player.hasMetadata("NPC"), "Cannot get SuperiorPlayer from an NPC.");
        return of(player.getUniqueId());
    }

    public static SuperiorPlayer of(UUID uuid){
        Preconditions.checkArgument(uuid != null, "UUID cannot be null.");
        Preconditions.checkArgument(plugin.getPlayers() != null, "PlayersHandler is not ready yet.");
        return plugin.getPlayers().getSuperiorPlayer(uuid);
    }

    public static SuperiorPlayer of(String name){
        return name == null ? null : plugin.getPlayers().getSuperiorPlayer(name);
    }

}
