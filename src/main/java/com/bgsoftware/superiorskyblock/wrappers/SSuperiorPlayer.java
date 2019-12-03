package com.bgsoftware.superiorskyblock.wrappers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import com.bgsoftware.superiorskyblock.database.CachedResultSet;
import com.bgsoftware.superiorskyblock.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.database.Query;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SSuperiorPlayer extends DatabaseObject implements SuperiorPlayer {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Set<String> completedMissions = new HashSet<>();
    private final UUID player;

    private UUID islandLeaderFromCache;

    private SuperiorPlayer islandLeader;
    private String name, textureValue = "";
    private PlayerRole playerRole;

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
    }

    public SSuperiorPlayer(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();

        player = UUID.fromString(((StringTag) compoundValues.get("player")).getValue());
        islandLeader = SSuperiorPlayer.of(UUID.fromString(((StringTag) compoundValues.get("teamLeader")).getValue()));
        name = ((StringTag) compoundValues.get("name")).getValue();
        playerRole = SPlayerRole.of(((StringTag) compoundValues.get("islandRole")).getValue());
        textureValue = ((StringTag) compoundValues.get("textureValue")).getValue();
        disbands = compoundValues.containsKey("disbands") ? (int) compoundValues.get("disbands").getValue() : plugin.getSettings().disbandCount;

        if(islandLeader.getIsland() == null)
            islandLeader = this;
    }

    public SSuperiorPlayer(UUID player){
        OfflinePlayer offlinePlayer;
        this.player = player;
        this.name = (offlinePlayer = Bukkit.getOfflinePlayer(player)) == null || offlinePlayer.getName() == null ? "null" : offlinePlayer.getName();
        this.islandLeader = this;
        this.playerRole = SPlayerRole.guestRole();
        this.disbands = SuperiorSkyblockPlugin.getPlugin().getSettings().disbandCount;
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
    public World getWorld(){
        return getLocation().getWorld();
    }

    @Override
    public Location getLocation(){
        return asPlayer().getLocation();
    }

    @Override
    public void teleport(Location location) {
        asPlayer().teleport(location);
    }

    @Override
    public void teleport(Island island) {
        teleport(island, null);
    }

    @Override
    public void teleport(Island island, Consumer<Boolean> result) {
        Location islandTeleportLocation = island.getTeleportLocation(World.Environment.NORMAL);
        Location islandCenterLocation = island.getCenter(World.Environment.NORMAL);

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

        List<ChunkSnapshot> chunkSnapshots = island.getAllChunks(true).stream()
                .map(Chunk::getChunkSnapshot).collect(Collectors.toList());

        Executor.async(() -> {
            for(ChunkSnapshot chunkSnapshot : chunkSnapshots) {
                if (LocationUtils.isChunkEmpty(chunkSnapshot))
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
    @Deprecated
    public IslandRole getIslandRole() {
        return IslandRole.valueOf(getPlayerRole().toString().toUpperCase());
    }

    @Override
    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    @Override
    @Deprecated
    public void setIslandRole(IslandRole islandRole) {
        setPlayerRole(SPlayerRole.of(islandRole.name()));
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
        completedMissions.add(mission.getName());

        Query.PLAYER_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public void resetMission(Mission mission) {
        completedMissions.remove(mission.getName());

        Query.PLAYER_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(player.toString())
                .execute(true);
    }

    @Override
    public boolean hasCompletedMission(Mission mission) {
        return completedMissions.contains(mission.getName());
    }

    @Override
    public List<Mission> getCompletedMissions() {
        return completedMissions.stream().map(plugin.getMissions()::getMission).collect(Collectors.toList());
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
        return asPlayer().getEffectivePermissions().stream().anyMatch(permissionAttachmentInfo -> permissionAttachmentInfo.getPermission().equalsIgnoreCase(permission));
    }

    @Override
    public boolean hasPermission(IslandPermission permission){
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
        if(sender == null)
            throw new NullPointerException("CommandSender cannot be null.");

        return of((Player) sender);
    }

    public static SuperiorPlayer of(Player player){
        if(player == null)
            throw new NullPointerException("Player cannot be null.");

        return of(player.getUniqueId());
    }

    public static SuperiorPlayer of(UUID uuid){
        if(uuid == null)
            throw new NullPointerException("UUID cannot be null.");
        if(plugin.getPlayers() == null)
            throw new NullPointerException("PlayersHandle is not ready yet.");

        return plugin.getPlayers().getSuperiorPlayer(uuid);
    }

    public static SuperiorPlayer of(String name){
        return name == null ? null : plugin.getPlayers().getSuperiorPlayer(name);
    }

}
