package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.PlayerDataHandler;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
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
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer_Old;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
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
    private final SPlayerDataHandler playerDataHandler = new SPlayerDataHandler(this);
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

    /*
     *   General Methods
     */

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
    public void setTextureValue(@Nonnull String textureValue) {
        Preconditions.checkNotNull(textureValue, "textureValue parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Set Texture Value, Player: " + getName() + ", Texture: " + textureValue);
        this.textureValue = textureValue;
        playerDataHandler.saveTextureValue();
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
    public void updateName(){
        Player player = asPlayer();
        if(player != null) {
            this.name = player.getName();
            playerDataHandler.savePlayerName();
        }
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
    public void runIfOnline(Consumer<Player> toRun) {
        Player player = asPlayer();
        if(player != null)
            toRun.accept(player);
    }

    @Override
    public boolean hasFlyGamemode() {
        Player player = asPlayer();
        return player != null && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
    }

    @Override
    public boolean isAFK() {
        Player player = asPlayer();
        return player != null && plugin.getProviders().isAFK(player);
    }

    @Override
    public boolean isVanished() {
        Player player = asPlayer();
        return player != null && plugin.getProviders().isVanished(player);
    }

    @Override
    public boolean hasPermission(String permission){
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");
        Player player = asPlayer();
        return permission.isEmpty() || (player != null && player.hasPermission(permission));
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");
        Player player = asPlayer();
        return player != null && plugin.getProviders().hasPermission(player, permission);
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission){
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");
        Island island = getIsland();
        return island != null && island.hasPermission(this, permission);
    }

    @Override
    public HitActionResult canHit(SuperiorPlayer otherPlayer) {
        Preconditions.checkNotNull(otherPlayer, "otherPlayer parameter cannot be null.");

        // Players can hit themselves
        if(equals(otherPlayer))
            return HitActionResult.SUCCESS;

        World world = getWorld();

        // Checks for island teammates pvp
        if(getIslandLeader().equals(otherPlayer.getIslandLeader()) &&
                (world == null || !plugin.getSettings().pvpWorlds.contains(world.getName())))
            return HitActionResult.ISLAND_TEAM_PVP;

        // Checks if this player can bypass all pvp restrictions
        {
            HitActionResult selfResult = checkPvPAllow(this, false);
            if (selfResult != HitActionResult.SUCCESS)
                return selfResult;
        }

        // Checks if target player can bypass all pvp restrictions
        {
            HitActionResult targetResult = checkPvPAllow(otherPlayer, true);
            if (targetResult != HitActionResult.SUCCESS)
                return targetResult;
        }

        return HitActionResult.SUCCESS;
    }

    /*
     *   Location Methods
     */

    @Override
    public World getWorld(){
        Location location = getLocation();
        return location == null ? null : location.getWorld();
    }

    @Override
    public Location getLocation(){
        Player player = asPlayer();
        return player == null ? null : player.getLocation();
    }

    @Override
    public void teleport(Location location) {
        teleport(location, null);
    }

    @Override
    public void teleport(Location location, @Nullable Consumer<Boolean> teleportResult) {
        if(isOnline()) {
            TeleportUtils.teleport(asPlayer(), location, teleportResult);
            return;
        }

        if(teleportResult != null)
            teleportResult.accept(false);
    }

    @Override
    public void teleport(Island island) {
        teleport(island, null);
    }

    @Override
    public void teleport(Island island, Consumer<Boolean> result) {
        if(!isOnline())
            return;

        Location islandTeleportLocation = island.getTeleportLocation(World.Environment.NORMAL);
        assert islandTeleportLocation != null;
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

        List<CompletableFuture<ChunkSnapshot>> chunksToLoad = island.getAllChunksAsync(World.Environment.NORMAL, true, true, null)
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

                int worldBuildLimit = Bukkit.getWorld(chunkSnapshot.getWorldName()).getMaxHeight();

                for(int x = 0; x < 16; x++){
                    for(int z = 0; z < 16; z++){
                        int y = Math.min(chunkSnapshot.getHighestBlockYAt(x, z), worldBuildLimit);
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
    public boolean isInsideIsland() {
        Player player = asPlayer();
        Island island = getIsland();
        return player != null && island != null && island.equals(plugin.getGrid().getIslandAt(player.getLocation()));
    }

    /*
     *   Island Methods
     */

    @Override
    public SuperiorPlayer getIslandLeader() {
        if(islandLeaderFromCache != null){
            islandLeader = plugin.getPlayers().getSuperiorPlayer(islandLeaderFromCache);
            if(islandLeader == null)
                islandLeader = this;
            islandLeaderFromCache = null;
        }

        return islandLeader;
    }

    @Override
    public void setIslandLeader(SuperiorPlayer islandLeader) {
        Preconditions.checkNotNull(islandLeader, "islandLeader parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Change Leader, Player: " + getName() + ", Leader: " + islandLeader.getName());
        this.islandLeader = islandLeader;
        playerDataHandler.saveIslandLeader();
    }

    @Override
    public Island getIsland(){
        return plugin.getGrid().getIsland(this);
    }

    @Override
    public boolean hasIsland() {
        return getIsland() != null;
    }

    @Override
    public PlayerRole getPlayerRole() {
        if(playerRole == null)
            setPlayerRole(SPlayerRole.guestRole());

        return playerRole;
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Change Role, Player: " + getName() + ", Role: " + playerRole);
        this.playerRole = playerRole;
        playerDataHandler.savePlayerRole();
    }

    @Override
    public int getDisbands() {
        return disbands;
    }

    @Override
    public boolean hasDisbands() {
        return disbands > 0;
    }

    @Override
    public void setDisbands(int disbands) {
        SuperiorSkyblockPlugin.debug("Action: Set Disbands, Player: " + getName() + ", Amount: " + disbands);
        this.disbands = Math.max(disbands, 0);
        playerDataHandler.saveDisbands();
    }

    /*
     *   Preferences Methods
     */

    @Override
    public java.util.Locale getUserLocale() {
        if(userLocale == null)
            userLocale = LocaleUtils.getDefault();
        return userLocale;
    }

    @Override
    public void setUserLocale(java.util.Locale userLocale) {
        Preconditions.checkNotNull(userLocale, "userLocale parameter cannot be null.");
        Preconditions.checkArgument(Locale.isValidLocale(userLocale), "Locale " + userLocale + " is not a valid locale.");

        SuperiorSkyblockPlugin.debug("Action: Set User Locale, Player: " + getName() + ", Locale: " + userLocale.getLanguage() + "-" + userLocale.getCountry());

        this.userLocale = userLocale;

        playerDataHandler.saveUserLocale();
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
    public void toggleTeamChat() {
        teamChatEnabled = !teamChatEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Chat, Player: " + getName() + ", Chat: " + teamChatEnabled);
    }

    @Override
    public boolean hasBypassModeEnabled() {
        Player player = asPlayer();

        if(bypassModeEnabled && player != null && !player.hasPermission("superior.admin.bypass"))
            bypassModeEnabled = false;

        return bypassModeEnabled;
    }

    @Override
    public void toggleBypassMode(){
        bypassModeEnabled = !bypassModeEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Bypass, Player: " + getName() + ", Bypass: " + bypassModeEnabled);
    }

    @Override
    public boolean hasToggledPanel() {
        return toggledPanel;
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        this.toggledPanel = toggledPanel;
        SuperiorSkyblockPlugin.debug("Action: Toggle Panel, Player: " + getName() + ", Panel: " + toggledPanel);
        playerDataHandler.saveToggledPanel();
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
    public BorderColor getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {
        Preconditions.checkNotNull(borderColor, "borderColor parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Set Border Color, Player: " + getName() + ", Border Color: " + borderColor);
        this.borderColor = borderColor;
        playerDataHandler.saveBorderColor();
    }

    /*
     *   Schematics Methods
     */

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

    /*
     *   Missions Methods
     */

    @Override
    public void completeMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Complete Mission, Player: " + getName() + ", Mission: " + mission.getName());
        completedMissions.add(mission, completedMissions.get(mission, 0) + 1);
        playerDataHandler.saveMissions();
    }

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
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
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return completedMissions.get(mission, 0) > 0;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Optional<MissionsHandler.MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);
        return missionDataOptional.isPresent() && getAmountMissionCompleted(mission) < missionDataOptional.get().resetAmount;
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
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

    /*
     *   Data Methods
     */

    @Override
    public void merge(SuperiorPlayer otherPlayer){
        Preconditions.checkNotNull(otherPlayer, "otherPlayer parameter cannot be null.");

        this.name = otherPlayer.getName();
        this.playerRole = otherPlayer.getPlayerRole();
        this.userLocale = otherPlayer.getUserLocale();
        this.worldBorderEnabled |= otherPlayer.hasWorldBorderEnabled();
        this.blocksStackerEnabled |= otherPlayer.hasBlocksStackerEnabled();
        this.schematicModeEnabled |= otherPlayer.hasSchematicModeEnabled();
        this.bypassModeEnabled |= otherPlayer.hasBypassModeEnabled();
        this.teamChatEnabled |= otherPlayer.hasTeamChatEnabled();
        this.toggledPanel |= otherPlayer.hasToggledPanel();
        this.islandFly |= otherPlayer.hasToggledPanel();
        this.adminSpyEnabled |= otherPlayer.hasAdminSpyEnabled();
        this.disbands = otherPlayer.getDisbands();
        this.borderColor = otherPlayer.getBorderColor();
        this.lastTimeStatus = otherPlayer.getLastTimeStatus();

        // We want to convert the data of the missions data file
        Executor.async(() -> FileUtils.replaceString(new File(plugin.getDataFolder(), "missions/_data.yml"),
                otherPlayer.getUniqueId() + "", uuid + ""));

        playerDataHandler.executeUpdateStatement(true);
        ((SPlayerDataHandler) otherPlayer.getDataHandler()).executeDeleteStatement(true);
    }

    @Override
    public PlayerDataHandler getDataHandler() {
        return playerDataHandler;
    }

    /*
     *   Other Methods
     */

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

    private static HitActionResult checkPvPAllow(SuperiorPlayer player, boolean target){
        // Checks for online status
        if(!player.isOnline())
            return target ? HitActionResult.TARGET_NOT_ONLINE : HitActionResult.NOT_ONLINE;

        // Checks for pvp warm-up
        if(((SPlayerDataHandler) player.getDataHandler()).isImmunedToPvP())
            return target ? HitActionResult.TARGET_PVP_WARMUP : HitActionResult.PVP_WARMUP;

        Island standingIsland = plugin.getGrid().getIslandAt(player.getLocation());

        if(standingIsland != null && (plugin.getSettings().spawnProtection || !standingIsland.isSpawn())){
            // Checks for pvp status
            if(!standingIsland.hasSettingsEnabled(IslandFlags.PVP))
                return target ? HitActionResult.TARGET_ISLAND_PVP_DISABLE : HitActionResult.ISLAND_PVP_DISABLE;

            // Checks for coop damage
            if(standingIsland.isCoop(player) && !plugin.getSettings().coopDamage)
                return target ? HitActionResult.TARGET_COOP_DAMAGE : HitActionResult.COOP_DAMAGE;

            // Checks for visitors damage
            if(standingIsland.isVisitor(player, false) && !plugin.getSettings().visitorsDamage)
                return target ? HitActionResult.TARGET_VISITOR_DAMAGE : HitActionResult.VISITOR_DAMAGE;
        }

        return HitActionResult.SUCCESS;
    }

}
