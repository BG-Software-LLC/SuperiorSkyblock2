package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.mission.MissionData;
import com.bgsoftware.superiorskyblock.mission.MissionReference;
import com.bgsoftware.superiorskyblock.player.builder.SuperiorPlayerBuilderImpl;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class SSuperiorPlayer implements SuperiorPlayer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final DatabaseBridge databaseBridge;
    private final PlayerTeleportAlgorithm playerTeleportAlgorithm;
    @Nullable
    private PersistentDataContainer persistentDataContainer; // Lazy loading

    private final Map<MissionReference, Counter> completedMissions = new ConcurrentHashMap<>();
    private final List<UUID> pendingInvites = new LinkedList<>();

    private final UUID uuid;

    private Island playerIsland = null;
    private String name;
    private String textureValue;
    private PlayerRole playerRole;
    private java.util.Locale userLocale;

    private boolean worldBorderEnabled;
    private boolean blocksStackerEnabled = plugin.getSettings().isDefaultStackedBlocks();
    private boolean schematicModeEnabled = false;
    private boolean bypassModeEnabled = false;
    private boolean teamChatEnabled = false;
    private boolean toggledPanel;
    private boolean islandFly;
    private boolean adminSpyEnabled = false;

    private SBlockPosition schematicPos1 = null;
    private SBlockPosition schematicPos2 = null;
    private int disbands;
    private BorderColor borderColor;
    private long lastTimeStatus;

    private BukkitTask teleportTask = null;
    private EnumSet<PlayerStatus> playerStatuses = EnumSet.noneOf(PlayerStatus.class);

    public SSuperiorPlayer(SuperiorPlayerBuilderImpl builder) {
        this.uuid = builder.uuid;
        this.name = builder.name;
        this.playerRole = builder.playerRole;
        this.disbands = builder.disbands;
        this.userLocale = builder.locale;
        this.textureValue = builder.textureValue;
        this.lastTimeStatus = builder.lastTimeUpdated;
        this.toggledPanel = builder.toggledPanel;
        this.islandFly = builder.islandFly;
        this.borderColor = builder.borderColor;
        this.worldBorderEnabled = builder.worldBorderEnabled;
        this.completedMissions.putAll(builder.completedMissions);
        if (builder.persistentData.length > 0)
            getPersistentDataContainer().load(builder.persistentData);

        this.databaseBridge = plugin.getFactory().createDatabaseBridge(this);
        this.playerTeleportAlgorithm = plugin.getFactory().createPlayerTeleportAlgorithm(this);

        databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
    }

    /*
     *   General Methods
     */

    private static HitActionResult checkPvPAllow(SuperiorPlayer player, boolean target) {
        // Checks for online status
        if (!player.isOnline())
            return target ? HitActionResult.TARGET_NOT_ONLINE : HitActionResult.NOT_ONLINE;

        // Checks for pvp warm-up
        if (player.hasPlayerStatus(PlayerStatus.PVP_IMMUNED))
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
    public void setTextureValue(String textureValue) {
        Preconditions.checkNotNull(textureValue, "textureValue parameter cannot be null.");

        Log.debug(Debug.SET_TEXTURE_VALUE, getName(), textureValue);

        // We first update the texture value, even if they are equal.
        this.textureValue = textureValue;

        // We now compare them but remove the timestamp when comparing.
        if (Objects.equals(removeTextureValueTimeStamp(this.textureValue), removeTextureValueTimeStamp(textureValue)))
            return;

        // We only save the value if it's actually different.
        PlayersDatabaseBridge.saveTextureValue(this);
    }

    @Override
    public void updateLastTimeStatus() {
        setLastTimeStatus(System.currentTimeMillis() / 1000);
    }

    @Override
    public void setLastTimeStatus(long lastTimeStatus) {
        Log.debug(Debug.SET_PLAYER_LAST_TIME_UPDATED, getName(), lastTimeStatus);

        if (this.lastTimeStatus == lastTimeStatus)
            return;

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

        if (Objects.equals(this.name, name))
            return;

        try {
            plugin.getPlayers().getPlayersContainer().removePlayer(this);
            this.name = name;
            PlayersDatabaseBridge.savePlayerName(this);
        } finally {
            plugin.getPlayers().getPlayersContainer().addPlayer(this);
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

    @Nullable
    @Override
    public MenuView<?, ?> getOpenedView() {
        Player player = asPlayer();

        if (player != null) {
            InventoryView openInventory = player.getOpenInventory();
            if (openInventory != null && openInventory.getTopInventory() != null) {
                InventoryHolder inventoryHolder = openInventory.getTopInventory().getHolder();
                if (inventoryHolder instanceof MenuView)
                    return (MenuView<?, ?>) inventoryHolder;
            }
        }

        return null;
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

        if (permission.isEmpty())
            return true;

        Log.debug(Debug.PERMISSION_LOOKUP, getName(), permission);

        Player player = asPlayer();
        if (player == null) {
            Log.debugResult(Debug.PERMISSION_LOOKUP, "Result", "Player is not online");
            return false;
        }

        boolean res = player.hasPermission(permission);

        Log.debugResult(Debug.PERMISSION_LOOKUP, "Result", res);

        return res;
    }

    @Override
    public boolean hasPermissionWithoutOP(String permission) {
        Preconditions.checkNotNull(permission, "permission parameter cannot be null.");

        if (permission.isEmpty())
            return true;

        Log.debug(Debug.PERMISSION_LOOKUP, getName(), permission, "No-Op Check");

        Player player = asPlayer();
        if (player == null) {
            Log.debugResult(Debug.PERMISSION_LOOKUP, "Result", "Player is not online");
            return false;
        }

        boolean res = plugin.getProviders().getPermissionsProvider().hasPermission(player, permission);

        Log.debugResult(Debug.PERMISSION_LOOKUP, "Result", res);

        return res;
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
        return player != null && island != null && island.isInside(player.getLocation());
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
        Log.debug(Debug.SET_PLAYER_ISLAND, getName(), island == null ? "null" : island.getOwner().getName());

        this.playerIsland = island;

        if (this.playerIsland == null) {
            this.playerRole = SPlayerRole.guestRole();
        }

    }

    @Override
    public boolean hasIsland() {
        return getIsland() != null;
    }

    @Override
    public void addInvite(Island island) {
        this.pendingInvites.add(island.getUniqueId());
    }

    @Override
    public void removeInvite(Island island) {
        this.pendingInvites.remove(island.getUniqueId());
    }

    @Override
    public List<Island> getInvites() {
        return new SequentialListBuilder<UUID>()
                .map(this.pendingInvites, uuid -> plugin.getGrid().getIslandByUUID(uuid));
    }

    @Override
    public PlayerRole getPlayerRole() {
        if (playerRole == null || (this.playerIsland == null && this.playerRole != SPlayerRole.guestRole()))
            setPlayerRole(SPlayerRole.guestRole());

        return playerRole;
    }

    @Override
    public void setPlayerRole(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");

        Log.debug(Debug.SET_PLAYER_ROLE, getName(), playerRole.getName());

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
        disbands = Math.max(disbands, 0);

        Log.debug(Debug.SET_DISBANDS, getName(), disbands);

        if (this.disbands == disbands)
            return;

        this.disbands = disbands;

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

        Log.debug(Debug.SET_LANGUAGE, getName(), userLocale.getLanguage() + "-" + userLocale.getCountry());

        if (Objects.equals(this.userLocale, userLocale))
            return;

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
        Log.debug(Debug.SET_TOGGLED_BORDER, getName(), enabled);

        if (this.worldBorderEnabled == enabled)
            return;

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
        Log.debug(Debug.SET_TOGGLED_STACKER, getName(), enabled);
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
        Log.debug(Debug.SET_TOGGLED_SCHEMATIC, getName(), enabled);
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
        Log.debug(Debug.SET_TEAM_CHAT, getName(), enabled);
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
        Log.debug(Debug.SET_ADMIN_BYPASS, getName(), enabled);
        bypassModeEnabled = enabled;
    }

    @Override
    public boolean hasToggledPanel() {
        return toggledPanel;
    }

    @Override
    public void setToggledPanel(boolean toggledPanel) {
        Log.debug(Debug.SET_TOGGLED_PANEL, getName(), toggledPanel);

        if (this.toggledPanel == toggledPanel)
            return;

        this.toggledPanel = toggledPanel;
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
        Log.debug(Debug.SET_ISLAND_FLY, getName(), enabled);

        if (this.islandFly == enabled)
            return;

        this.islandFly = enabled;
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
        Log.debug(Debug.SET_ADMIN_SPY, getName(), enabled);
        adminSpyEnabled = enabled;
    }

    @Override
    public BorderColor getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(BorderColor borderColor) {
        Preconditions.checkNotNull(borderColor, "borderColor parameter cannot be null.");

        Log.debug(Debug.SET_BORDER_COLOR, getName(), borderColor);

        if (this.borderColor == borderColor)
            return;

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
    public void setSchematicPos1(@Nullable Block block) {
        Log.debug(Debug.SET_SCHEMATIC_POSITION, getName(), block == null ? "null" : block.getLocation());
        this.schematicPos1 = block == null ? null : new SBlockPosition(block.getLocation());
    }

    @Override
    public SBlockPosition getSchematicPos2() {
        return schematicPos2;
    }

    @Override
    public void setSchematicPos2(@Nullable Block block) {
        Log.debug(Debug.SET_SCHEMATIC_POSITION, getName(), block == null ? "null" : block.getLocation());
        this.schematicPos2 = block == null ? null : new SBlockPosition(block.getLocation());
    }

    /*
     *   Missions Methods
     */

    /*
     *   Data Methods
     */

    @Override
    @Deprecated
    public boolean isImmunedToPvP() {
        return this.hasPlayerStatus(PlayerStatus.PVP_IMMUNED);
    }

    @Override
    @Deprecated
    public void setImmunedToPvP(boolean immunedToPvP) {
        if (immunedToPvP)
            setPlayerStatus(PlayerStatus.PVP_IMMUNED);
        else
            removePlayerStatus(PlayerStatus.PVP_IMMUNED);
    }

    @Override
    @Deprecated
    public boolean isLeavingFlag() {
        return this.hasPlayerStatus(PlayerStatus.LEAVING_ISLAND);
    }

    @Override
    @Deprecated
    public void setLeavingFlag(boolean leavingFlag) {
        if (leavingFlag)
            setPlayerStatus(PlayerStatus.LEAVING_ISLAND);
        else
            removePlayerStatus(PlayerStatus.LEAVING_ISLAND);
    }

    @Override
    public boolean isImmunedToPortals() {
        return this.hasPlayerStatus(PlayerStatus.PORTALS_IMMUNED);
    }

    @Override
    public void setImmunedToPortals(boolean immuneToTeleport) {
        if (immuneToTeleport)
            setPlayerStatus(PlayerStatus.PORTALS_IMMUNED);
        else
            removePlayerStatus(PlayerStatus.PORTALS_IMMUNED);
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
    public PlayerStatus getPlayerStatus() {
        for (PlayerStatus playerStatus : PlayerStatus.values()) {
            if (this.playerStatuses.contains(playerStatus))
                return playerStatus;
        }

        return PlayerStatus.NONE;
    }

    @Override
    public void setPlayerStatus(PlayerStatus playerStatus) {
        Preconditions.checkNotNull(playerStatus, "playerStatus cannot be null");
        Preconditions.checkArgument(playerStatus != PlayerStatus.NONE, "Cannot set PlayerStatus.NONE");

        if (this.hasPlayerStatus(playerStatus))
            return;

        Log.debug(Debug.SET_PLAYER_STATUS, getName(), playerStatus.name());
        this.playerStatuses.add(playerStatus);
    }

    @Override
    public void removePlayerStatus(PlayerStatus playerStatus) {
        Preconditions.checkNotNull(playerStatus, "playerStatus cannot be null");
        Preconditions.checkArgument(playerStatus != PlayerStatus.NONE, "Cannot remove PlayerStatus.NONE");

        if (!this.hasPlayerStatus(playerStatus))
            return;

        Log.debug(Debug.REMOVE_PLAYER_STATUS, getName(), playerStatus.name());
        this.playerStatuses.remove(playerStatus);
    }

    @Override
    public boolean hasPlayerStatus(PlayerStatus playerStatus) {
        Preconditions.checkNotNull(playerStatus, "playerStatus cannot be null");
        Preconditions.checkArgument(playerStatus != PlayerStatus.NONE, "Cannot check PlayerStatus.NONE");
        return this.playerStatuses.contains(playerStatus);
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
        this.completedMissions.clear();

        otherPlayer.getCompletedMissionsWithAmounts().forEach((mission, finishCount) ->
                this.completedMissions.put(new MissionReference(mission), new Counter(finishCount)));

        if (!otherPlayer.isPersistentDataContainerEmpty()) {
            byte[] data = otherPlayer.getPersistentDataContainer().serialize();
            getPersistentDataContainer().load(data);
        }

        // Convert data for missions
        plugin.getMissions().convertPlayerData(otherPlayer, this);

        // Replace player in DB.
        PlayersDatabaseBridge.replacePlayer(otherPlayer, this);
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
    public boolean isPersistentDataContainerEmpty() {
        return persistentDataContainer == null || persistentDataContainer.isEmpty();
    }

    @Override
    public void savePersistentDataContainer() {
        PlayersDatabaseBridge.executeFutureSaves(this, PlayersDatabaseBridge.FutureSave.PERSISTENT_DATA);
    }

    @Override
    public void completeMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.changeAmountMissionsCompletedInternal(mission, counter -> counter.inc(1));
    }

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.changeAmountMissionsCompletedInternal(mission, counter -> counter.inc(-1));
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Counter finishCount = completedMissions.get(new MissionReference(mission));
        return finishCount != null && finishCount.get() > 0;
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
        Counter finishCount = completedMissions.get(new MissionReference(mission));
        return finishCount == null ? 0 : finishCount.get();
    }

    @Override
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.changeAmountMissionsCompletedInternal(mission, counter -> counter.set(finishCount));
    }

    private void changeAmountMissionsCompletedInternal(Mission<?> mission, Function<Counter, Integer> action) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");

        MissionReference missionReference = new MissionReference(mission);

        Counter finishCount = completedMissions.computeIfAbsent(missionReference, r -> new Counter(0));
        int oldFinishCount = action.apply(finishCount);
        int newFinishCount = finishCount.get();

        Log.debug(Debug.SET_PLAYER_MISSION_COMPLETED, getName(), mission.getName(), newFinishCount);

        // We always want to reset data
        mission.clearData(this);

        if (newFinishCount > 0) {
            if (newFinishCount == oldFinishCount)
                return;

            PlayersDatabaseBridge.saveMission(this, mission, newFinishCount);
        } else {
            completedMissions.remove(missionReference);

            if (oldFinishCount <= 0)
                return;

            PlayersDatabaseBridge.removeMission(this, mission);
        }
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new SequentialListBuilder<MissionReference>()
                .filter(MissionReference::isValid)
                .map(completedMissions.keySet(), MissionReference::getMission);
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        Map<Mission<?>, Integer> completedMissions = new LinkedHashMap<>();

        this.completedMissions.forEach((mission, finishCount) -> {
            if (mission.isValid())
                completedMissions.put(mission.getMission(), finishCount.get());
        });

        return completedMissions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(completedMissions);
    }

    /*
     *   Other Methods
     */

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperiorPlayer && (this == obj || uuid.equals(((SuperiorPlayer) obj).getUniqueId()));
    }

    @Override
    public String toString() {
        return "SSuperiorPlayer{" +
                "uuid=[" + uuid + "]," +
                "name=[" + name + "]" +
                "}";
    }

    private static String removeTextureValueTimeStamp(@Nullable String textureValue) {
        // The texture value string is a json containing a timestamp value.
        // However, when we compare texture values, we want to emit the timestamp value.
        // This value is found at index 35->41 (6 chars in length).
        return textureValue == null || textureValue.length() <= 42 ? null : textureValue.substring(0, 35) + textureValue.substring(42);
    }

}
