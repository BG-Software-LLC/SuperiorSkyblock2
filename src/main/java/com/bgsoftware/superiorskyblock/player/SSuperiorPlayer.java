package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
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
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.EmptyDataHandler;
import com.bgsoftware.superiorskyblock.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.database.deserializer.PlayersDeserializer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.mission.MissionData;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SSuperiorPlayer implements SuperiorPlayer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<Mission<?>, Integer> completedMissions = new ConcurrentHashMap<>();
    private final DatabaseBridge databaseBridge = plugin.getFactory().createDatabaseBridge(this);
    private final UUID uuid;

    private SuperiorPlayer islandLeader = this;
    private String name, textureValue = "";
    private PlayerRole playerRole;
    private java.util.Locale userLocale;

    private boolean worldBorderEnabled = plugin.getSettings().isDefaultWorldBorder();
    private boolean blocksStackerEnabled = plugin.getSettings().isDefaultStackedBlocks();
    private boolean schematicModeEnabled = false;
    private boolean bypassModeEnabled = false;
    private boolean teamChatEnabled = false;
    private boolean toggledPanel = plugin.getSettings().isDefaultToggledPanel();
    private boolean islandFly = plugin.getSettings().isDefaultIslandFly();
    private boolean adminSpyEnabled = false;

    private SBlockPosition schematicPos1 = null, schematicPos2 = null;
    private int disbands;
    private BorderColor borderColor = BorderColor.safeValue(plugin.getSettings().getDefaultBorderColor(), BorderColor.BLUE);
    private long lastTimeStatus = -1;

    private boolean immuneToPvP = false;
    private boolean immuneToPortals = false;
    private boolean leavingFlag = false;

    private BukkitTask teleportTask = null;

    public SSuperiorPlayer(DatabaseResult resultSet) {
        this.uuid = UUID.fromString(resultSet.getString("uuid"));
        this.name = resultSet.getString("last_used_name");
        this.textureValue = resultSet.getString("last_used_skin");
        this.disbands = resultSet.getInt("disbands");
        this.lastTimeStatus = resultSet.getLong("last_time_updated");

        PlayersDeserializer.deserializeMissions(this, this.completedMissions);

        PlayersDeserializer.deserializePlayerSettings(this, playerSettingsRaw -> {
            DatabaseResult playerSettings = new DatabaseResult(playerSettingsRaw);
            this.toggledPanel = playerSettings.getBoolean("toggled_panel");
            this.islandFly = playerSettings.getBoolean("island_fly");
            this.borderColor = BorderColor.safeValue(playerSettings.getString("border_color"), BorderColor.BLUE);
            this.userLocale = LocaleUtils.getLocale(playerSettings.getString("language"));
            this.worldBorderEnabled = playerSettings.getBoolean("toggled_border");
        });

        databaseBridge.startSavingData();
    }

    public SSuperiorPlayer(UUID player){
        OfflinePlayer offlinePlayer;
        this.uuid = player;
        this.name = (offlinePlayer = Bukkit.getOfflinePlayer(player)) == null || offlinePlayer.getName() == null ? "null" : offlinePlayer.getName();
        this.playerRole = SPlayerRole.guestRole();
        this.disbands = plugin.getSettings().getDisbandCount();
        this.userLocale = LocaleUtils.getDefault();
        databaseBridge.startSavingData();
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
        PlayersDatabaseBridge.saveTextureValue(this);
    }


    @Override
    public void updateLastTimeStatus() {
        lastTimeStatus = System.currentTimeMillis() / 1000;

        SuperiorSkyblockPlugin.debug("Action: Update Last Time, Player: " + getName() + ", Last Time: " + lastTimeStatus);

        PlayersDatabaseBridge.saveLastTimeStatus(this);
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
            PlayersDatabaseBridge.savePlayerName(this);
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
    public boolean isShownAsOnline() {
        Player player = asPlayer();
        return player != null && player.getGameMode() != GameMode.SPECTATOR && !isVanished();
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
                (world == null || !plugin.getSettings().getPvPWorlds().contains(world.getName())))
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
        if (!isOnline())
            return;

        Location islandTeleportLocation = island.getTeleportLocation(plugin.getSettings().getWorlds().getDefaultWorld());
        assert islandTeleportLocation != null;
        Block islandTeleportBlock = islandTeleportLocation.getBlock();

        if (island instanceof SpawnIsland) {
            SuperiorSkyblockPlugin.debug("Action: Teleport Player, Player: " + getName() + ", Location: " + LocationUtils.getLocation(islandTeleportLocation));
            teleport(islandTeleportLocation.add(0, 0.5, 0));
            if (result != null)
                result.accept(true);
            return;
        }

        teleportIfSafe(island, islandTeleportBlock, islandTeleportLocation, 0, 0, (teleportResult, teleportLocation) -> {
            if (teleportResult) {
                if (result != null)
                    result.accept(true);
                return;
            }

            Block islandCenterBlock = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getBlock();
            float rotationYaw = islandTeleportLocation.getYaw();
            float rotationPitch = islandTeleportLocation.getPitch();

            teleportIfSafe(island, islandCenterBlock, null, rotationYaw, rotationPitch, (centerTeleportResult, centerTeleportLocation) -> {
                if (centerTeleportResult) {
                    island.setTeleportLocation(centerTeleportLocation);
                    if (result != null)
                        result.accept(true);
                    return;
                }

                {
                    Block teleportLocationHighestBlock = islandTeleportBlock.getWorld()
                            .getHighestBlockAt(islandTeleportBlock.getLocation()).getRelative(BlockFace.UP);
                    if (LocationUtils.isSafeBlock(teleportLocationHighestBlock)) {
                        adjustAndTeleportPlayerToLocation(island, teleportLocationHighestBlock.getLocation(),
                                rotationYaw, rotationPitch, result);
                        return;
                    }
                }

                {
                    Block centerHighestBlock = islandCenterBlock.getWorld()
                            .getHighestBlockAt(islandCenterBlock.getLocation()).getRelative(BlockFace.UP);
                    if (LocationUtils.isSafeBlock(centerHighestBlock)) {
                        adjustAndTeleportPlayerToLocation(island, centerHighestBlock.getLocation(), rotationYaw,
                                rotationPitch, result);
                        return;
                    }
                }

                /*
                 *   Finding a new block to teleport the player to.
                 */

                List<CompletableFuture<ChunkSnapshot>> chunksToLoad = island.getAllChunksAsync(
                                plugin.getSettings().getWorlds().getDefaultWorld(), true, true, null)
                        .stream().map(future -> future.thenApply(Chunk::getChunkSnapshot)).collect(Collectors.toList());

                Executor.createTask().runAsync(v -> {
                    List<Location> safeLocations = new ArrayList<>();

                    for (CompletableFuture<ChunkSnapshot> chunkToLoad : chunksToLoad) {
                        ChunkSnapshot chunkSnapshot;

                        try {
                            chunkSnapshot = chunkToLoad.get();
                        } catch (Exception ex) {
                            SuperiorSkyblockPlugin.log("&cCouldn't load chunk!");
                            continue;
                        }

                        if (LocationUtils.isChunkEmpty(null, chunkSnapshot))
                            continue;

                        int worldBuildLimit = Bukkit.getWorld(chunkSnapshot.getWorldName()).getMaxHeight();

                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                int y = Math.min(chunkSnapshot.getHighestBlockYAt(x, z), worldBuildLimit);
                                Key blockKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y, z),
                                        belowKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y == 0 ? 0 : y - 1, z);

                                Material blockType, belowType;

                                try {
                                    blockType = Material.valueOf(blockKey.getGlobalKey());
                                    belowType = Material.valueOf(belowKey.getGlobalKey());
                                } catch (IllegalArgumentException ex) {
                                    continue;
                                }

                                if (blockType.isSolid() || belowType.isSolid()) {
                                    safeLocations.add(new Location(Bukkit.getWorld(chunkSnapshot.getWorldName()),
                                            chunkSnapshot.getX() * 16 + x, y, chunkSnapshot.getZ() * 16 + z));
                                }
                            }
                        }
                    }

                    return safeLocations.stream().min(Comparator.comparingDouble(loc ->
                            loc.distanceSquared(islandTeleportLocation))).orElse(null);
                }).runSync(location -> {
                    if (location != null) {
                        adjustAndTeleportPlayerToLocation(island, location, rotationYaw, rotationPitch, result);
                    } else if (result != null) {
                        result.accept(false);
                    }
                });

            });

        });
    }

    private void adjustAndTeleportPlayerToLocation(Island island, Location location, float yaw, float pitch, Consumer<Boolean> result){
        location = location.add(0.5, 0, 0.5);
        location.setYaw(yaw);
        location.setPitch(pitch);

        SuperiorSkyblockPlugin.debug("Action: Teleport Player, Player: " + getName() + ", Location: " + LocationUtils.getLocation(location));

        island.setTeleportLocation(location);
        teleport(location.add(0, 0.5, 0));
        if(result != null)
            result.accept(true);
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
        return islandLeader;
    }

    @Override
    public void setIslandLeader(SuperiorPlayer islandLeader) {
        Preconditions.checkNotNull(islandLeader, "islandLeader parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Change Leader, Player: " + getName() + ", Leader: " + islandLeader.getName());
        this.islandLeader = islandLeader;
        //PlayersDatabaseBridge.saveIslandLeader(this);
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
        Island island = getIsland();
        if(island != null && island.getOwner() != this)
            IslandsDatabaseBridge.saveMemberRole(island, this);
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
        PlayersDatabaseBridge.saveDisbands(this);
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

        PlayersDatabaseBridge.saveUserLocale(this);
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return worldBorderEnabled;
    }

    @Override
    public void toggleWorldBorder() {
        worldBorderEnabled = !worldBorderEnabled;
        SuperiorSkyblockPlugin.debug("Action: Toggle Border, Player: " + getName() + ", Border: " + worldBorderEnabled);
        PlayersDatabaseBridge.saveToggledBorder(this);
    }

    @Override
    public void updateWorldBorder(@Nullable Island island) {
        plugin.getNMSWorld().setWorldBorder(this, island);
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
        PlayersDatabaseBridge.saveToggledPanel(this);
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
        PlayersDatabaseBridge.saveIslandFly(this);
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
        PlayersDatabaseBridge.saveBorderColor(this);
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
        int finishCount = completedMissions.getOrDefault(mission, 0) + 1;
        completedMissions.put(mission, disbands);
        PlayersDatabaseBridge.saveMission(this, mission, finishCount);
    }

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Reset Mission, Player: " + getName() + ", Mission: " + mission.getName());

        int finishCount = completedMissions.getOrDefault(mission, 0) - 1;

        if(finishCount > 0) {
            completedMissions.put(mission, finishCount);
            PlayersDatabaseBridge.saveMission(this, mission, finishCount);
        }
        else {
            completedMissions.remove(mission);
            PlayersDatabaseBridge.removeMission(this, mission);
        }

        mission.clearData(this);
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return completedMissions.getOrDefault(mission, 0) > 0;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Optional<MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);
        return missionDataOptional.isPresent() && getAmountMissionCompleted(mission) <
                missionDataOptional.get().getResetAmount();
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return completedMissions.getOrDefault(mission, 0);
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return Collections.unmodifiableList(new ArrayList<>(completedMissions.keySet()));
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts(){
        return Collections.unmodifiableMap(completedMissions);
    }

    /*
     *   Data Methods
     */

    @Override
    public void setImmunedToPvP(boolean immunedToPvP){
        SuperiorSkyblockPlugin.debug("Action: Set PvP Immune, Player: " + getName() + ", Immune: " + immunedToPvP);
        this.immuneToPvP = immunedToPvP;
    }

    @Override
    public boolean isImmunedToPvP(){
        return immuneToPvP;
    }

    @Override
    public void setLeavingFlag(boolean leavingFlag){
        SuperiorSkyblockPlugin.debug("Action: Set Leaving Flag, Player: " + getName() + ", Flag: " + leavingFlag);
        this.leavingFlag = leavingFlag;
    }

    @Override
    public boolean isLeavingFlag() {
        return leavingFlag;
    }

    @Override
    public void setTeleportTask(BukkitTask teleportTask){
        if(this.teleportTask != null)
            this.teleportTask.cancel();
        this.teleportTask = teleportTask;
    }

    @Override
    public BukkitTask getTeleportTask(){
        return teleportTask;
    }

    @Override
    public void setImmunedToPortals(boolean immuneToTeleport){
        SuperiorSkyblockPlugin.debug("Action: Set Portals Immune, Player: " + getName() + ", Immune: " + immuneToTeleport);
        this.immuneToPortals = immuneToTeleport;
    }

    @Override
    public boolean isImmunedToPortals(){
        return immuneToPortals;
    }

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
        Executor.async(() -> FileUtils.replaceString(new File(BuiltinModules.MISSIONS.getDataFolder(), "_data.yml"),
                otherPlayer.getUniqueId() + "", uuid + ""));

        PlayersDatabaseBridge.updatePlayer(this);
        PlayersDatabaseBridge.deletePlayer(otherPlayer);
    }

    @Override
    @Deprecated
    public PlayerDataHandler getDataHandler() {
        return EmptyDataHandler.getInstance();
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
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

    private void teleportIfSafe(Island island, Block block, Location customLocation, float yaw, float pitch,
                                BiConsumer<Boolean, Location> teleportResult){
        ChunksProvider.loadChunk(ChunkPosition.of(block), chunk -> {
            if(!LocationUtils.isSafeBlock(block)){
                if(teleportResult != null)
                    teleportResult.accept(false, null);
                return;
            }

            Location toTeleport;

            if(customLocation != null){
                toTeleport = customLocation;
            }
            else{
                toTeleport = block.getLocation().add(0.5, 0, 0.5);
                toTeleport.setYaw(yaw);
                toTeleport.setPitch(pitch);
                island.setTeleportLocation(toTeleport);
            }

            SuperiorSkyblockPlugin.debug("Action: Teleport Player, Player: " + getName() + ", Location: " + LocationUtils.getLocation(toTeleport));
            teleport(toTeleport.add(0, 0.5, 0));

            if(teleportResult != null)
                teleportResult.accept(true, toTeleport);
        });
    }

    private static HitActionResult checkPvPAllow(SuperiorPlayer player, boolean target){
        // Checks for online status
        if(!player.isOnline())
            return target ? HitActionResult.TARGET_NOT_ONLINE : HitActionResult.NOT_ONLINE;

        // Checks for pvp warm-up
        if(player.isImmunedToPvP())
            return target ? HitActionResult.TARGET_PVP_WARMUP : HitActionResult.PVP_WARMUP;

        Island standingIsland = plugin.getGrid().getIslandAt(player.getLocation());

        if(standingIsland != null && (plugin.getSettings().getSpawn().isProtected() || !standingIsland.isSpawn())){
            // Checks for pvp status
            if(!standingIsland.hasSettingsEnabled(IslandFlags.PVP))
                return target ? HitActionResult.TARGET_ISLAND_PVP_DISABLE : HitActionResult.ISLAND_PVP_DISABLE;

            // Checks for coop damage
            if(standingIsland.isCoop(player) && !plugin.getSettings().isCoopDamage())
                return target ? HitActionResult.TARGET_COOP_DAMAGE : HitActionResult.COOP_DAMAGE;

            // Checks for visitors damage
            if(standingIsland.isVisitor(player, false) && !plugin.getSettings().isVisitorsDamage())
                return target ? HitActionResult.TARGET_VISITOR_DAMAGE : HitActionResult.VISITOR_DAMAGE;
        }

        return HitActionResult.SUCCESS;
    }

}
