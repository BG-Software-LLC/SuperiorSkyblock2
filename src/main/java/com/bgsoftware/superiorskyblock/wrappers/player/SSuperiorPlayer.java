package com.bgsoftware.superiorskyblock.wrappers.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.PlayerDataHandler;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.data.SPlayerDataHandler;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.teleport.TeleportUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SSuperiorPlayer implements SuperiorPlayer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Registry<Mission<?>, Integer> completedMissions = Registry.createRegistry();
    private final PlayerDataHandler playerDataHandler = new SPlayerDataHandler(this);
    private final UUID uuid;

    private UUID islandLeaderFromCache;

    private SuperiorPlayer islandLeader;
    private String name, textureValue = "";
    private PlayerRole playerRole;
    private java.util.Locale userLocale;

    private boolean worldBorderEnabled = plugin.getSettings().defaultWorldBorder;
    private boolean blocksStackerEnabled = plugin.getSettings().defaultBlocksStacker;
    private boolean schematicModeEnabled = false;
    private boolean bypassModeEnabled = false;
    private boolean teamChatEnabled = false;
    private boolean toggledPanel = plugin.getSettings().defaultToggledPanel;
    private boolean islandFly = plugin.getSettings().defaultIslandFly;
    private boolean adminSpyEnabled = false;

    private SBlockPosition schematicPos1 = null, schematicPos2 = null;
    private int disbands;
    private BorderColor borderColor = BorderColor.safeValue(plugin.getSettings().defaultBorderColor, BorderColor.BLUE);
    private long lastTimeStatus = -1;

    public SSuperiorPlayer(ResultSet resultSet) throws SQLException {
        uuid = UUID.fromString(resultSet.getString("player"));
        islandLeaderFromCache = UUID.fromString(resultSet.getString("teamLeader"));
        name = resultSet.getString("name");
        textureValue = resultSet.getString("textureValue");

        try{
            playerRole = SPlayerRole.fromId(Integer.parseInt(resultSet.getString("islandRole")));
        }catch(Exception ex){
            playerRole = SPlayerRole.of(resultSet.getString("islandRole"));
        }

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
        this.uuid = player;
        this.name = (offlinePlayer = Bukkit.getOfflinePlayer(player)) == null || offlinePlayer.getName() == null ? "null" : offlinePlayer.getName();
        this.islandLeader = this;
        this.playerRole = SPlayerRole.guestRole();
        this.disbands = plugin.getSettings().disbandCount;
        this.userLocale = LocaleUtils.getDefault();
    }

    @Override
    public UUID getUniqueId(){
        return uuid;
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
        SuperiorSkyblockPlugin.debug("Action: Set Texture Value, Player: " + getName() + ", Texture: " + textureValue);
        this.textureValue = textureValue;
        playerDataHandler.saveTextureValue();
    }

    @Override
    public void updateName(){
        this.name = asPlayer().getName();
        playerDataHandler.savePlayerName();
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

        SuperiorSkyblockPlugin.debug("Action: Set User Locale, Player: " + getName() + ", Locale: " + userLocale.getLanguage() + "-" + userLocale.getCountry());

        this.userLocale = userLocale;

        playerDataHandler.saveUserLocale();
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
        teleport(location, null);
    }

    @Override
    public void teleport(Location location, Consumer<Boolean> teleportResult) {
        if(isOnline()) {
            TeleportUtils.teleport(asPlayer(), location, teleportResult);
            return;
        }

        if(teleportResult != null)
            teleportResult.accept(false);
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
        Block islandTeleportBlock = islandTeleportLocation.getBlock();
        Block islandCenterBlock = island.getCenter(World.Environment.NORMAL).getBlock();

        if(island instanceof SpawnIsland){
            SuperiorSkyblockPlugin.debug("Action: Teleport Player, Player: " + getName() + ", Location: " + LocationUtils.getLocation(islandTeleportLocation));
            teleport(islandTeleportLocation.add(0, 0.5, 0));
            if(result != null)
                result.accept(true);
            return;
        }

        Location toTeleport = null;

        //We check if the island's teleport location is safe.
        if(LocationUtils.isSafeBlock(islandTeleportBlock)){
            toTeleport = islandTeleportLocation;
        }

        //We check if the island's center location is safe.
        else if(LocationUtils.isSafeBlock(islandCenterBlock)){
            toTeleport = islandCenterBlock.getLocation().add(0.5, 0, 0.5);
            island.setTeleportLocation(toTeleport);
        }

        //We check if the highest block at the island's center location is safe.
        else if(LocationUtils.isSafeBlock((islandCenterBlock = islandCenterBlock.getWorld().getHighestBlockAt(islandCenterBlock.getLocation())))){
            toTeleport = islandCenterBlock.getLocation().add(0.5, 0, 0.5);
            island.setTeleportLocation(toTeleport);
        }

        //Checking if one of the options above is safe.
        if(toTeleport != null){
            SuperiorSkyblockPlugin.debug("Action: Teleport Player, Player: " + getName() + ", Location: " + LocationUtils.getLocation(toTeleport));
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

        Executor.createTask().runAsync(v -> {
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
                            blockType = Material.valueOf(blockKey.getGlobalKey());
                            belowType = Material.valueOf(belowKey.getGlobalKey());
                        }catch(IllegalArgumentException ex){
                            continue;
                        }

                        if(blockType.isSolid() || belowType.isSolid()){
                            return new Location(Bukkit.getWorld(chunkSnapshot.getWorldName()),
                                    chunkSnapshot.getX() * 16 + x, y, chunkSnapshot.getZ() * 16 + z);
                        }
                    }
                }
            }

            return null;
        }).runSync(location -> {
            if(location != null){
                island.setTeleportLocation(location);
                SuperiorSkyblockPlugin.debug("Action: Teleport Player, Player: " + getName() + ", Location: " + LocationUtils.getLocation(location));
                teleport(location.add(0.5, 0.5, 0.5));
                if(result != null)
                    result.accept(true);
            }
            else if(result != null){
                result.accept(false);
            }
        });
    }

    @Override
    public UUID getTeamLeader() {
        return getIslandLeader().getUniqueId();
    }

    @Override
    public SuperiorPlayer getIslandLeader() {
        if(islandLeaderFromCache != null){
            islandLeader = plugin.getPlayers().getSuperiorPlayer(islandLeaderFromCache);
            islandLeaderFromCache = null;
        }

        return islandLeader;
    }

    @Override
    public void setTeamLeader(UUID teamLeader) {
        setIslandLeader(plugin.getPlayers().getSuperiorPlayer(teamLeader));
    }

    @Override
    public void setIslandLeader(SuperiorPlayer superiorPlayer) {
        SuperiorSkyblockPlugin.debug("Action: Change Leader, Player: " + getName() + ", Leader: " + superiorPlayer.getName());
        this.islandLeader = superiorPlayer;
        playerDataHandler.saveIslandLeader();
    }

    @Override
    public Island getIsland(){
        return plugin.getGrid().getIsland(this);
    }

    @Override
    public PlayerRole getPlayerRole() {
        if(playerRole == null)
            setPlayerRole(SPlayerRole.guestRole());

        return playerRole;
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        SuperiorSkyblockPlugin.debug("Action: Change Role, Player: " + getName() + ", Role: " + playerRole);
        this.playerRole = playerRole;
        playerDataHandler.savePlayerRole();
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return worldBorderEnabled;
    }

    @Override
    public void toggleWorldBorder() {
        worldBorderEnabled = !worldBorderEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Border, Player: " + getName() + ", Border: " + worldBorderEnabled);
        playerDataHandler.saveToggledBorder();
    }

    @Override
    public boolean hasBlocksStackerEnabled() {
        return blocksStackerEnabled;
    }

    @Override
    public void toggleBlocksStacker() {
        blocksStackerEnabled = !blocksStackerEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Stacker, Player: " + getName() + ", Stacker: " + blocksStackerEnabled);
    }

    @Override
    public boolean hasSchematicModeEnabled() {
        return schematicModeEnabled;
    }

    @Override
    public void toggleSchematicMode() {
        schematicModeEnabled = !schematicModeEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Schematic, Player: " + getName() + ", Schematic: " + schematicModeEnabled);
    }

    @Override
    public boolean hasTeamChatEnabled() {
        return teamChatEnabled;
    }

    @Override
    public void toggleBypassMode(){
        bypassModeEnabled = !bypassModeEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Bypass, Player: " + getName() + ", Bypass: " + bypassModeEnabled);
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
        SuperiorSkyblockPlugin.debug("Action: Toggle Chat, Player: " + getName() + ", Chat: " + teamChatEnabled);
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
        SuperiorSkyblockPlugin.debug("Action: Set Disbands, Player: " + getName() + ", Amount: " + disbands);
        this.disbands = Math.max(disbands, 0);
        playerDataHandler.saveDisbands();
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        this.toggledPanel = toggledPanel;
        SuperiorSkyblockPlugin.debug("Action: Toggle Panel, Player: " + getName() + ", Panel: " + toggledPanel);
        playerDataHandler.saveToggledPanel();
    }

    @Override
    public boolean hasToggledPanel() {
        return toggledPanel;
    }

    @Override
    public boolean hasIslandFlyEnabled(){
        Player player = asPlayer();

        if(islandFly && player != null && !player.hasPermission("superior.island.fly")) {
            islandFly = false;
            if(player.isFlying()){
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }

        return islandFly;
    }

    @Override
    public void toggleIslandFly(){
        islandFly = !islandFly;
        SuperiorSkyblockPlugin.debug("Action: Toggle Fly, Player: " + getName() + ", Fly: " + islandFly);
        playerDataHandler.saveIslandFly();
    }

    @Override
    public boolean hasAdminSpyEnabled() {
        return adminSpyEnabled;
    }

    @Override
    public void toggleAdminSpy() {
        adminSpyEnabled = !adminSpyEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Spy, Player: " + getName() + ", Spy: " + adminSpyEnabled);
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
        SuperiorSkyblockPlugin.debug("Action: Set Border Color, Player: " + getName() + ", Border Color: " + borderColor);
        this.borderColor = borderColor;
        playerDataHandler.saveBorderColor();
    }

    @Override
    public void updateLastTimeStatus() {
        lastTimeStatus = System.currentTimeMillis() / 1000;

        SuperiorSkyblockPlugin.debug("Action: Update Last Time, Player: " + getName() + ", Last Time: " + lastTimeStatus);

        playerDataHandler.saveLastTimeStatus();
    }

    @Override
    public long getLastTimeStatus() {
        return lastTimeStatus;
    }

    @Override
    public void completeMission(Mission<?> mission) {
        SuperiorSkyblockPlugin.debug("Action: Complete Mission, Player: " + getName() + ", Mission: " + mission.getName());
        completedMissions.add(mission, completedMissions.get(mission, 0) + 1);
        playerDataHandler.saveMissions();
    }

    @Override
    public void resetMission(Mission<?> mission) {
        SuperiorSkyblockPlugin.debug("Action: Reset Mission, Player: " + getName() + ", Mission: " + mission.getName());

        if(completedMissions.get(mission, 0) > 0) {
            completedMissions.add(mission, completedMissions.get(mission) - 1);
        }
        else {
            completedMissions.remove(mission);
        }

        mission.clearData(this);

        playerDataHandler.saveMissions();
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return completedMissions.get(mission, 0) > 0;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        Optional<MissionsHandler.MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);
        return missionDataOptional.isPresent() && getAmountMissionCompleted(mission) < missionDataOptional.get().resetAmount;
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        return completedMissions.get(mission, 0);
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new ArrayList<>(completedMissions.keys());
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts(){
        return Collections.unmodifiableMap(completedMissions.toMap());
    }

    @Override
    public BlockPosition getSchematicPos1() {
        return schematicPos1;
    }

    @Override
    public void setSchematicPos1(Block block) {
        this.schematicPos1 = block == null ? null : SBlockPosition.of(block.getLocation());
        SuperiorSkyblockPlugin.debug("Action: Schematic Position #1, Player: " + getName() + ", Pos: " + schematicPos1);
    }

    @Override
    public SBlockPosition getSchematicPos2() {
        return schematicPos2;
    }

    @Override
    public void setSchematicPos2(Block block) {
        this.schematicPos2 = block == null ? null : SBlockPosition.of(block.getLocation());
        SuperiorSkyblockPlugin.debug("Action: Schematic Position #2, Player: " + getName() + ", Pos: " + schematicPos2);
    }

    @Override
    public Player asPlayer(){
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public OfflinePlayer asOfflinePlayer(){
        return Bukkit.getOfflinePlayer(uuid);
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
        return player != null && plugin.getProviders().hasPermission(player, permission);
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission){
        Island island = getIsland();
        return island != null && island.hasPermission(this, permission);
    }

    @Override
    public boolean hasFlyGamemode() {
        Player player = asPlayer();
        return player != null && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
    }

    @Override
    public void merge(SuperiorPlayer other){
        this.name = other.getName();
        this.playerRole = other.getPlayerRole();
        this.userLocale = other.getUserLocale();
        this.worldBorderEnabled |= other.hasWorldBorderEnabled();
        this.blocksStackerEnabled |= other.hasBlocksStackerEnabled();
        this.schematicModeEnabled |= other.hasSchematicModeEnabled();
        this.bypassModeEnabled |= other.hasBypassModeEnabled();
        this.teamChatEnabled |= other.hasTeamChatEnabled();
        this.toggledPanel |= other.hasToggledPanel();
        this.islandFly |= other.hasToggledPanel();
        this.adminSpyEnabled |= other.hasAdminSpyEnabled();
        this.disbands = other.getDisbands();
        this.borderColor = other.getBorderColor();
        this.lastTimeStatus = other.getLastTimeStatus();
        ((SPlayerDataHandler) playerDataHandler).executeUpdateStatement(true);
        ((SPlayerDataHandler) other.getDataHandler()).executeDeleteStatement(true);
    }

    @Override
    public PlayerDataHandler getDataHandler() {
        return playerDataHandler;
    }

    @Override
    public String toString() {
        return "SSuperiorPlayer{" +
                "uuid=[" + uuid + "]," +
                "name=[" + name + "]" +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperiorPlayer && uuid.equals(((SuperiorPlayer) obj).getUniqueId());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

}
