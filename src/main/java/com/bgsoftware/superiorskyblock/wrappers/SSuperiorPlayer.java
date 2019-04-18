package com.bgsoftware.superiorskyblock.wrappers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

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

public final class SSuperiorPlayer implements SuperiorPlayer {

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

    public SSuperiorPlayer(ResultSet resultSet) throws SQLException {
        player = UUID.fromString(resultSet.getString("player"));
        teamLeader = UUID.fromString(resultSet.getString("teamLeader"));
        name = resultSet.getString("name");
        textureValue = resultSet.getString("textureValue");
        islandRole = IslandRole.valueOf(resultSet.getString("islandRole"));
        disbands = resultSet.getInt("disbands");
    }

    public SSuperiorPlayer(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();

        player = UUID.fromString(((StringTag) compoundValues.get("player")).getValue());
        teamLeader = UUID.fromString(((StringTag) compoundValues.get("teamLeader")).getValue());
        name = ((StringTag) compoundValues.get("name")).getValue();
        islandRole = IslandRole.valueOf(((StringTag) compoundValues.get("islandRole")).getValue());
        textureValue = ((StringTag) compoundValues.get("textureValue")).getValue();
        disbands = compoundValues.containsKey("disbands") ?
                (int) compoundValues.get("disbands").getValue() :
                SuperiorSkyblockPlugin.getPlugin().getSettings().disbandCount;

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
    }

    public void updateName(){
        this.name = Bukkit.getPlayer(player).getName();
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
    }

    public Island getIsland(){
        return plugin.getGrid().getIsland(this);
    }

    public IslandRole getIslandRole() {
        return islandRole;
    }

    public void setIslandRole(IslandRole islandRole) {
        this.islandRole = islandRole;
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
        this.disbands = disbands < 0 ? 0 : disbands;
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

    public String getSaveStatement(){
        return String.format("UPDATE players SET teamLeader='%s',name='%s',islandRole='%s',textureValue='%s',disbands='%d' WHERE player='%s'",
                teamLeader, name, islandRole.name(), textureValue, disbands, player);
    }

//    public CompoundTag getAsTag(){
//        Map<String, Tag> compoundValues = new HashMap<>();
//
//        compoundValues.put("player", new StringTag(player.toString()));
//        compoundValues.put("teamLeader", new StringTag(teamLeader.toString()));
//        compoundValues.put("name", new StringTag(this.name));
//        compoundValues.put("islandRole", new StringTag(islandRole.name()));
//        compoundValues.put("textureValue", new StringTag(textureValue));
//
//        return new CompoundTag(compoundValues);
//    }

    @Override
    public String toString() {
        return "SSuperiorPlayer{" +
                "uuid=[" + player + "]," +
                "name=[" + name + "]" +
                "}";
    }

    public static SuperiorPlayer of(CommandSender sender){
        return sender == null ? null : of((Player) sender);
    }

    public static SuperiorPlayer of(Player player){
        return player == null ? null : of(player.getUniqueId());
    }

    public static SuperiorPlayer of(UUID uuid){
        return uuid == null || plugin.getPlayers() == null ? null : plugin.getPlayers().getSuperiorPlayer(uuid);
    }

    public static SuperiorPlayer of(String name){
        return name == null ? null : plugin.getPlayers().getSuperiorPlayer(name);
    }

}
