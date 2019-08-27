package com.bgsoftware.superiorskyblock.wrappers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import com.bgsoftware.superiorskyblock.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.database.Query;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public final class SSuperiorPlayer extends DatabaseObject implements SuperiorPlayer {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final UUID player;

    private UUID teamLeader;
    private String name, textureValue = "";
    private IslandRole islandRole;

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

    public SSuperiorPlayer(ResultSet resultSet) throws SQLException {
        player = UUID.fromString(resultSet.getString("player"));
        teamLeader = UUID.fromString(resultSet.getString("teamLeader"));
        name = resultSet.getString("name");
        textureValue = resultSet.getString("textureValue");
        islandRole = IslandRole.valueOf(resultSet.getString("islandRole"));
        disbands = resultSet.getInt("disbands");
        toggledPanel = resultSet.getBoolean("toggledPanel");
        islandFly = resultSet.getBoolean("islandFly");
        borderColor = BorderColor.valueOf(resultSet.getString("borderColor"));
        lastTimeStatus = resultSet.getLong("lastTimeStatus");
    }

    public SSuperiorPlayer(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();

        player = UUID.fromString(((StringTag) compoundValues.get("player")).getValue());
        teamLeader = UUID.fromString(((StringTag) compoundValues.get("teamLeader")).getValue());
        name = ((StringTag) compoundValues.get("name")).getValue();
        islandRole = IslandRole.valueOf(((StringTag) compoundValues.get("islandRole")).getValue());
        textureValue = ((StringTag) compoundValues.get("textureValue")).getValue();
        disbands = compoundValues.containsKey("disbands") ? (int) compoundValues.get("disbands").getValue() : plugin.getSettings().disbandCount;

        if(plugin.getGrid().getIsland(SSuperiorPlayer.of(teamLeader)) == null)
            teamLeader = player;
    }

    public SSuperiorPlayer(UUID player){
        OfflinePlayer offlinePlayer;
        this.player = player;
        this.name = (offlinePlayer = Bukkit.getOfflinePlayer(player)) == null || offlinePlayer.getName() == null ? "null" : offlinePlayer.getName();
        this.teamLeader = player;
        this.islandRole = IslandRole.GUEST;
        this.disbands = SuperiorSkyblockPlugin.getPlugin().getSettings().disbandCount;
    }

    public UUID getUniqueId(){
        return player;
    }

    public String getName() {
        return name;
    }

    public String getTextureValue() {
        return textureValue;
    }

    public void setTextureValue(String textureValue) {
        this.textureValue = textureValue;
        Query.PLAYER_SET_TEXTURE.getStatementHolder()
                .setString(textureValue)
                .setString(player.toString())
                .execute(true);
    }

    public void updateName(){
        this.name = Bukkit.getPlayer(player).getName();
        Query.PLAYER_SET_NAME.getStatementHolder()
                .setString(name)
                .setString(player.toString())
                .execute(true);
    }

    public World getWorld(){
        return getLocation().getWorld();
    }

    public Location getLocation(){
        return asPlayer().getLocation();
    }

    public UUID getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(UUID teamLeader) {
        this.teamLeader = teamLeader;
        Query.PLAYER_SET_LEADER.getStatementHolder()
                .setString(teamLeader.toString())
                .setString(player.toString())
                .execute(true);
    }

    public Island getIsland(){
        return plugin.getGrid().getIsland(this);
    }

    public IslandRole getIslandRole() {
        return islandRole;
    }

    public void setIslandRole(IslandRole islandRole) {
        this.islandRole = islandRole;
        Query.PLAYER_SET_ROLE.getStatementHolder()
                .setString(islandRole.name())
                .setString(player.toString())
                .execute(true);
    }

    public boolean hasWorldBorderEnabled() {
        return worldBorderEnabled;
    }

    public void toggleWorldBorder() {
        worldBorderEnabled = !worldBorderEnabled;
    }

    public boolean hasBlocksStackerEnabled() {
        return blocksStackerEnabled;
    }

    public void toggleBlocksStacker() {
        blocksStackerEnabled = !blocksStackerEnabled;
    }

    public boolean hasSchematicModeEnabled() {
        return schematicModeEnabled;
    }

    public void toggleSchematicMode() {
        schematicModeEnabled = !schematicModeEnabled;
    }

    public boolean hasTeamChatEnabled() {
        return teamChatEnabled;
    }

    public void toggleBypassMode(){
        bypassModeEnabled = !bypassModeEnabled;
    }

    public boolean hasBypassModeEnabled() {
        return bypassModeEnabled;
    }

    public void toggleTeamChat() {
        teamChatEnabled = !teamChatEnabled;
    }

    public boolean hasDisbands() {
        return disbands > 0;
    }

    public int getDisbands() {
        return disbands;
    }

    public void setDisbands(int disbands) {
        this.disbands = Math.max(disbands, 0);
        Query.PLAYER_SET_DISBANDS.getStatementHolder()
                .setInt(disbands)
                .setString(player.toString())
                .execute(true);
    }

    public void setToggledPanel(boolean toggledPanel) {
        this.toggledPanel = toggledPanel;
        Query.PLAYER_SET_TOGGLED_PANEL.getStatementHolder()
                .setBoolean(toggledPanel)
                .setString(player.toString())
                .execute(true);
    }

    public boolean hasToggledPanel() {
        return toggledPanel;
    }

    public boolean hasIslandFlyEnabled(){
        return islandFly;
    }

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

    public BlockPosition getSchematicPos1() {
        return schematicPos1;
    }

    public void setSchematicPos1(Block block) {
        this.schematicPos1 = block == null ? null : SBlockPosition.of(block.getLocation());
    }

    public SBlockPosition getSchematicPos2() {
        return schematicPos2;
    }

    public void setSchematicPos2(Block block) {
        this.schematicPos2 = block == null ? null : SBlockPosition.of(block.getLocation());
    }

    public Player asPlayer(){
        return Bukkit.getPlayer(player);
    }

    public OfflinePlayer asOfflinePlayer(){
        return Bukkit.getOfflinePlayer(player);
    }

    public boolean isOnline(){
        return asOfflinePlayer().isOnline();
    }

    public boolean hasPermission(String permission){
        return permission.isEmpty() || asPlayer().hasPermission(permission);
    }

    public boolean hasPermission(IslandPermission permission){
        Island island = getIsland();
        return island != null && island.hasPermission(this, permission);
    }

    @Override
    public void executeUpdateStatement(boolean async) {
        Query.PLAYER_UPDATE.getStatementHolder()
                .setString(teamLeader.toString())
                .setString(name)
                .setString(islandRole.name())
                .setString(textureValue)
                .setInt(disbands)
                .setBoolean(toggledPanel)
                .setBoolean(islandFly)
                .setString(borderColor.name())
                .setString(lastTimeStatus + "")
                .setString(player.toString())
                .execute(async);
    }

    @Override
    public void executeInsertStatement(boolean async) {
        Query.PLAYER_INSERT.getStatementHolder()
                .setString(player.toString())
                .setString(teamLeader.toString())
                .setString(name)
                .setString(islandRole.name())
                .setString(textureValue)
                .setInt(plugin.getSettings().disbandCount)
                .setBoolean(toggledPanel)
                .setBoolean(islandFly)
                .setString(borderColor.name())
                .setString(lastTimeStatus + "")
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
