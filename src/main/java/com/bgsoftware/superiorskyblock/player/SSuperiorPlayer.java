package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.mission.MissionData;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SSuperiorPlayer implements SuperiorPlayer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final DatabaseBridge databaseBridge = plugin.getFactory().createDatabaseBridge(this);
    private final PlayerTeleportAlgorithm playerTeleportAlgorithm = plugin.getFactory().createPlayerTeleportAlgorithm(this);
    @Nullable
    private PersistentDataContainer persistentDataContainer; // Lazy loading

    private final Map<Mission<?>, Integer> completedMissions = new ConcurrentHashMap<>();
    private final UUID uuid;

    private Island playerIsland = null;
    private String name;
    private String textureValue = "";
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

    private SBlockPosition schematicPos1 = null;
    private SBlockPosition schematicPos2 = null;
    private int disbands;
    private BorderColor borderColor = BorderColor.safeValue(plugin.getSettings().getDefaultBorderColor(), BorderColor.BLUE);
    private long lastTimeStatus = -1;

    private boolean immuneToPvP = false;
    private boolean immuneToPortals = false;
    private boolean leavingFlag = false;

    private BukkitTask teleportTask = null;

    public SSuperiorPlayer(UUID player) {
        this(player, Bukkit.getOfflinePlayer(player), SPlayerRole.guestRole(), plugin.getSettings().getDisbandCount(),
                PlayerLocales.getDefaultLocale());
    }

    public SSuperiorPlayer(UUID uuid, @Nullable OfflinePlayer offlinePlayer, PlayerRole playerRole, int disbands,
                           Locale userLocale) {
        this(uuid, offlinePlayer == null ? null : offlinePlayer.getName(), playerRole, disbands, userLocale);
    }

    public SSuperiorPlayer(UUID uuid, @Nullable String name, PlayerRole playerRole, int disbands, Locale userLocale) {
        this.uuid = uuid;
        this.name = name == null ? "null" : name;
        this.playerRole = playerRole;
        this.disbands = disbands;
        this.userLocale = userLocale;
        databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
    }

    public static Optional<SuperiorPlayer> fromDatabase(DatabaseCache<CachedPlayerInfo> cache,
                                                        DatabaseResult resultSet) {
        Optional<UUID> uuid = resultSet.getUUID("uuid");
        if (!uuid.isPresent()) {
            SuperiorSkyblockPlugin.log("&cCannot load player with null uuid, skipping...");
            return Optional.empty();
        }

        SSuperiorPlayer superiorPlayer = new SSuperiorPlayer(
                uuid.get(),
                resultSet.getString("last_used_name").orElse(null),
                SPlayerRole.defaultRole(),
                resultSet.getInt("disbands").orElse(0),
                PlayerLocales.getDefaultLocale()
        );

        try {
            superiorPlayer.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);

            superiorPlayer.textureValue = resultSet.getString("last_used_skin").orElse("");
            superiorPlayer.lastTimeStatus = resultSet.getLong("last_time_updated")
                    .orElse(System.currentTimeMillis() / 1000);

            CachedPlayerInfo cachedPlayerInfo = cache.getCachedInfo(uuid.get());

            if (cachedPlayerInfo != null)
                superiorPlayer.loadFromCachedInfo(cachedPlayerInfo);
        } finally {
            superiorPlayer.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }

        return Optional.of(superiorPlayer);
    }

    /*
     *   General Methods
     */

    private static HitActionResult checkPvPAllow(SuperiorPlayer player, boolean target) {
        // Checks for online status
        if (!player.isOnline())
            return target ? HitActionResult.TARGET_NOT_ONLINE : HitActionResult.NOT_ONLINE;

        // Checks for pvp warm-up
        if (player.isImmunedToPvP())
            return target ? HitActionResult.TARGET_PVP_WARMUP : HitActionResult.PVP_WARMUP;

        Island standingIsland = plugin.getGrid().getIslandAt(player.getLocation());

        if (standingIsland != null && (plugin.getSettings().getSpawn().isProtected() || !standingIsland.isSpawn())) {
            // Checks for pvp status
            if (!standingIsland.hasSettingsEnabled(IslandFlags.PVP))
                return target ? HitActionResult.TARGET_ISLAND_PVP_DISABLE : HitActionResult.ISLAND_PVP_DISABLE;

            // Checks for coop damage
            if (standingIsland.isCoop(player) && !plugin.getSettings().isCoopDamage())
                return target ? HitActionResult.TARGET_COOP_DAMAGE : HitActionResult.COOP_DAMAGE;

            // Checks for visitors damage
            if (standingIsland.isVisitor(player, false) && !plugin.getSettings().isVisitorsDamage())
                return target ? HitActionResult.TARGET_VISITOR_DAMAGE : HitActionResult.VISITOR_DAMAGE;
        }

        return HitActionResult.SUCCESS;
    }

    @Override
    public UUID getUniqueId() {
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
        PluginDebugger.debug("Action: Set Texture Value, Player: " + getName() + ", Texture: " + textureValue);
        this.textureValue = textureValue;
        PlayersDatabaseBridge.saveTextureValue(this);
    }

    @Override
    public void updateLastTimeStatus() {
        setLastTimeStatus(System.currentTimeMillis() / 1000);
    }

    @Override
    public void setLastTimeStatus(long lastTimeStatus) {
        PluginDebugger.debug("Action: Update Last Time, Player: " + getName() + ", Last Time: " + lastTimeStatus);
        this.lastTimeStatus = lastTimeStatus;
        PlayersDatabaseBridge.saveLastTimeStatus(this);
    }

    @Override
    public long getLastTimeStatus() {
        return lastTimeStatus;
    }

    @Override
    public void updateName() {
        Player player = asPlayer();
        if (player != null)
            this.setName(player.getName());
    }

    @Override
    public void setName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        if (!this.name.equals(name)) {
            try {
                plugin.getPlayers().getPlayersContainer().removePlayer(this);
                this.name = name;
                PlayersDatabaseBridge.savePlayerName(this);
            } finally {
                plugin.getPlayers().getPlayersContainer().addPlayer(this);
            }
        }
    }

    @Override
    public Player asPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public boolean isOnline() {
        OfflinePlayer offlinePlayer = asOfflinePlayer();
        return offlinePlayer != null && offlinePlayer.isOnline();
    }

    @Override
    public void runIfOnline(Consumer<Player> toRun) {
        Player player = asPlayer();
        if (player != null)
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
        return player != null && plugin.getProviders().getVanishProvider().isVanished(player);
    }

    @Override
    public boolean isShownAsOnline() {
        Player player = asPlayer();
        return player != null && player.getGameMode() != GameMode.SPECTATOR && !isVanished();
    }

    @Override
    public boolean hasPermission(String permission) {
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");
        Player player = asPlayer();
        return permission.isEmpty() || (player != null && player.hasPermission(permission));
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");
        Player player = asPlayer();
        return player != null && plugin.getProviders().getPermissionsProvider().hasPermission(player, permission);
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission) {
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");
        Island island = getIsland();
        return island != null && island.hasPermission(this, permission);
    }

    /*
     *   Location Methods
     */

    @Override
    public HitActionResult canHit(SuperiorPlayer otherPlayer) {
        Preconditions.checkNotNull(otherPlayer, "otherPlayer parameter cannot be null.");

        // Players can hit themselves
        if (equals(otherPlayer))
            return HitActionResult.SUCCESS;

        World world = getWorld();

        // Checks for island teammates pvp
        if (getIslandLeader().equals(otherPlayer.getIslandLeader()) &&
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

    @Override
    public World getWorld() {
        Location location = getLocation();
        return location == null ? null : location.getWorld();
    }

    @Override
    public Location getLocation() {
        Player player = asPlayer();
        return player == null ? null : player.getLocation();
    }

    @Override
    public void teleport(Location location) {
        teleport(location, null);
    }

    @Override
    public void teleport(Location location, @Nullable Consumer<Boolean> teleportResult) {
        Player player = asPlayer();
        if (player != null) {
            playerTeleportAlgorithm.teleport(player, location).whenComplete((result, error) -> {
                if (teleportResult != null)
                    teleportResult.accept(error == null && result);
            });
        } else if (teleportResult != null) {
            teleportResult.accept(false);
        }
    }

    @Override
    public void teleport(Island island) {
        this.teleport(island, (Consumer<Boolean>) null);
    }

    @Override
    public void teleport(Island island, World.Environment environment) {
        this.teleport(island, environment, null);
    }

    @Override
    public void teleport(Island island, @Nullable Consumer<Boolean> teleportResult) {
        this.teleport(island, plugin.getSettings().getWorlds().getDefaultWorld(), teleportResult);
    }

    @Override
    public void teleport(Island island, World.Environment environment, @Nullable Consumer<Boolean> teleportResult) {
        Player player = asPlayer();
        if (player != null) {
            playerTeleportAlgorithm.teleport(player, island, environment).whenComplete((result, error) -> {
                if (teleportResult != null)
                    teleportResult.accept(error == null && result);
            });
        } else if (teleportResult != null) {
            teleportResult.accept(false);
        }
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
        Island island = getIsland();
        return island == null ? this : island.getOwner();
    }

    @Override
    public void setIslandLeader(SuperiorPlayer islandLeader) {
        setIsland(islandLeader.getIsland());
    }

    @Override
    public Island getIsland() {
        return playerIsland;
    }

    @Override
    public void setIsland(Island island) {
        PluginDebugger.debug("Action: Change Island, Player: " + getName() + ", New Island: " +
                (island == null ? "None" : island.getUniqueId().toString()));
        this.playerIsland = island;
    }

    @Override
    public boolean hasIsland() {
        return getIsland() != null;
    }

    @Override
    public PlayerRole getPlayerRole() {
        if (playerRole == null)
            setPlayerRole(SPlayerRole.guestRole());

        return playerRole;
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        PluginDebugger.debug("Action: Change Role, Player: " + getName() + ", Role: " + playerRole);
        this.playerRole = playerRole;
        Island island = getIsland();
        if (island != null && island.getOwner() != this)
            IslandsDatabaseBridge.saveMemberRole(island, this);
    }

    @Override
    public int getDisbands() {
        return disbands;
    }

    @Override
    public void setDisbands(int disbands) {
        PluginDebugger.debug("Action: Set Disbands, Player: " + getName() + ", Amount: " + disbands);
        this.disbands = Math.max(disbands, 0);
        PlayersDatabaseBridge.saveDisbands(this);
    }

    @Override
    public boolean hasDisbands() {
        return disbands > 0;
    }

    /*
     *   Preferences Methods
     */

    @Override
    public java.util.Locale getUserLocale() {
        if (userLocale == null)
            userLocale = PlayerLocales.getDefaultLocale();
        return userLocale;
    }

    @Override
    public void setUserLocale(java.util.Locale userLocale) {
        Preconditions.checkNotNull(userLocale, "userLocale parameter cannot be null.");
        Preconditions.checkArgument(PlayerLocales.isValidLocale(userLocale), "Locale " + userLocale + " is not a valid locale.");

        PluginDebugger.debug("Action: Set User Locale, Player: " + getName() + ", Locale: " + userLocale.getLanguage() + "-" + userLocale.getCountry());

        this.userLocale = userLocale;

        PlayersDatabaseBridge.saveUserLocale(this);
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return worldBorderEnabled;
    }

    @Override
    public void toggleWorldBorder() {
        setWorldBorderEnabled(!worldBorderEnabled);
    }

    @Override
    public void setWorldBorderEnabled(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Border, Player: " + getName() + ", Border: " + enabled);
        this.worldBorderEnabled = enabled;
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
        setBlocksStacker(!blocksStackerEnabled);
    }

    @Override
    public void setBlocksStacker(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Stacker, Player: " + getName() + ", Stacker: " + enabled);
        blocksStackerEnabled = enabled;
    }

    @Override
    public boolean hasSchematicModeEnabled() {
        return schematicModeEnabled;
    }

    @Override
    public void toggleSchematicMode() {
        setSchematicMode(!schematicModeEnabled);
    }

    @Override
    public void setSchematicMode(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Schematic, Player: " + getName() + ", Schematic: " + enabled);
        schematicModeEnabled = enabled;
    }

    @Override
    public boolean hasTeamChatEnabled() {
        return teamChatEnabled;
    }

    @Override
    public void toggleTeamChat() {
        setTeamChat(!teamChatEnabled);
    }

    @Override
    public void setTeamChat(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Chat, Player: " + getName() + ", Chat: " + enabled);
        teamChatEnabled = enabled;
    }

    @Override
    public boolean hasBypassModeEnabled() {
        Player player = asPlayer();

        if (bypassModeEnabled && player != null && !player.hasPermission("superior.admin.bypass"))
            bypassModeEnabled = false;

        return bypassModeEnabled;
    }

    @Override
    public void toggleBypassMode() {
        setBypassMode(!bypassModeEnabled);
    }

    @Override
    public void setBypassMode(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Bypass, Player: " + getName() + ", Bypass: " + enabled);
        bypassModeEnabled = enabled;
    }

    @Override
    public boolean hasToggledPanel() {
        return toggledPanel;
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        this.toggledPanel = toggledPanel;
        PluginDebugger.debug("Action: Toggle Panel, Player: " + getName() + ", Panel: " + toggledPanel);
        PlayersDatabaseBridge.saveToggledPanel(this);
    }

    @Override
    public boolean hasIslandFlyEnabled() {
        Player player = asPlayer();

        if (islandFly && player != null && !player.hasPermission("superior.island.fly")) {
            islandFly = false;
            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }

        return islandFly;
    }

    @Override
    public void toggleIslandFly() {
        setIslandFly(!islandFly);
    }

    @Override
    public void setIslandFly(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Fly, Player: " + getName() + ", Fly: " + enabled);
        islandFly = enabled;
        PlayersDatabaseBridge.saveIslandFly(this);
    }

    @Override
    public boolean hasAdminSpyEnabled() {
        return adminSpyEnabled;
    }

    @Override
    public void toggleAdminSpy() {
        setAdminSpy(!adminSpyEnabled);
    }

    @Override
    public void setAdminSpy(boolean enabled) {
        PluginDebugger.debug("Action: Toggle Spy, Player: " + getName() + ", Spy: " + enabled);
        adminSpyEnabled = enabled;
    }

    @Override
    public BorderColor getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {
        Preconditions.checkNotNull(borderColor, "borderColor parameter cannot be null.");
        PluginDebugger.debug("Action: Set Border Color, Player: " + getName() + ", Border Color: " + borderColor);
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
        this.schematicPos1 = block == null ? null : new SBlockPosition(block.getLocation());
        PluginDebugger.debug("Action: Schematic Position #1, Player: " + getName() + ", Pos: " +
                (block == null ? "None" : Formatters.LOCATION_FORMATTER.format(block.getLocation())));
    }

    @Override
    public SBlockPosition getSchematicPos2() {
        return schematicPos2;
    }

    @Override
    public void setSchematicPos2(Block block) {
        this.schematicPos2 = block == null ? null : new SBlockPosition(block.getLocation());
        PluginDebugger.debug("Action: Schematic Position #2, Player: " + getName() + ", Pos: " +
                (block == null ? "None" : Formatters.LOCATION_FORMATTER.format(block.getLocation())));
    }

    /*
     *   Missions Methods
     */

    @Override
    public boolean isImmunedToPvP() {
        return immuneToPvP;
    }

    @Override
    public void setImmunedToPvP(boolean immunedToPvP) {
        PluginDebugger.debug("Action: Set PvP Immune, Player: " + getName() + ", Immune: " + immunedToPvP);
        this.immuneToPvP = immunedToPvP;
    }

    @Override
    public boolean isLeavingFlag() {
        return leavingFlag;
    }

    @Override
    public void setLeavingFlag(boolean leavingFlag) {
        PluginDebugger.debug("Action: Set Leaving Flag, Player: " + getName() + ", Flag: " + leavingFlag);
        this.leavingFlag = leavingFlag;
    }

    @Override
    public BukkitTask getTeleportTask() {
        return teleportTask;
    }

    @Override
    public void setTeleportTask(BukkitTask teleportTask) {
        if (this.teleportTask != null)
            this.teleportTask.cancel();
        this.teleportTask = teleportTask;
    }

    @Override
    public boolean isImmunedToPortals() {
        return immuneToPortals;
    }

    /*
     *   Data Methods
     */

    @Override
    public void setImmunedToPortals(boolean immuneToTeleport) {
        PluginDebugger.debug("Action: Set Portals Immune, Player: " + getName() + ", Immune: " + immuneToTeleport);
        this.immuneToPortals = immuneToTeleport;
    }

    @Override
    public void merge(SuperiorPlayer otherPlayer) {
        Preconditions.checkNotNull(otherPlayer, "otherPlayer parameter cannot be null.");

        this.name = otherPlayer.getName();
        this.playerIsland = otherPlayer.getIsland();
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

        // Convert data for missions
        plugin.getMissions().convertPlayerData(otherPlayer, this);

        PlayersDatabaseBridge.updatePlayer(this);
        PlayersDatabaseBridge.deletePlayer(otherPlayer);
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        if (persistentDataContainer == null)
            persistentDataContainer = plugin.getFactory().createPersistentDataContainer(this);
        return persistentDataContainer;
    }

    @Override
    public void completeMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.setAmountMissionCompleted(mission, completedMissions.getOrDefault(mission, 0) + 1);
    }

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.setAmountMissionCompleted(mission, completedMissions.getOrDefault(mission, 0) - 1);
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
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        PluginDebugger.debug("Action: Set Amount Mission Completed, Player: " + getName() +
                ", Mission: " + mission.getName() + ", Amount: " + finishCount);

        if (finishCount > 0) {
            completedMissions.put(mission, finishCount);
            PlayersDatabaseBridge.saveMission(this, mission, finishCount);
        } else {
            completedMissions.remove(mission);
            PlayersDatabaseBridge.removeMission(this, mission);
        }

        mission.clearData(this);
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new SequentialListBuilder<Mission<?>>().build(completedMissions.keySet());
    }

    /*
     *   Other Methods
     */

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return Collections.unmodifiableMap(completedMissions);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperiorPlayer && uuid.equals(((SuperiorPlayer) obj).getUniqueId());
    }

    @Override
    public String toString() {
        return "SSuperiorPlayer{" +
                "uuid=[" + uuid + "]," +
                "name=[" + name + "]" +
                "}";
    }

    private void loadFromCachedInfo(CachedPlayerInfo cachedPlayerInfo) {
        this.toggledPanel = cachedPlayerInfo.toggledPanel;
        this.islandFly = cachedPlayerInfo.islandFly;
        this.borderColor = cachedPlayerInfo.borderColor;
        this.userLocale = cachedPlayerInfo.userLocale;
        this.worldBorderEnabled = cachedPlayerInfo.worldBorderEnabled;
        this.completedMissions.putAll(cachedPlayerInfo.completedMissions);
        if (cachedPlayerInfo.persistentData.length > 0)
            getPersistentDataContainer().load(cachedPlayerInfo.persistentData);
    }

}
