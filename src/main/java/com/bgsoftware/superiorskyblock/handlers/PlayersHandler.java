package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class PlayersHandler implements PlayersManager {

    private static final int GUEST_ROLE_INDEX = -1, COOP_ROLE_INDEX = -2;

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static Map<UUID, SuperiorPlayer> players = new HashMap<>();
    private static Map<Integer, PlayerRole> roles = new HashMap<>();
    private static int lastRole = Integer.MIN_VALUE;

    public PlayersHandler(){
        ConfigurationSection rolesSection = plugin.getSettings().islandRolesSection;
        loadRole(rolesSection.getConfigurationSection("guest"), GUEST_ROLE_INDEX);
        loadRole(rolesSection.getConfigurationSection("coop"), COOP_ROLE_INDEX);
        for(String roleSection : rolesSection.getConfigurationSection("ladder").getKeys(false))
            loadRole(rolesSection.getConfigurationSection("ladder." + roleSection), 0);
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(String name){
        for(SuperiorPlayer superiorPlayer : players.values()){
            if(superiorPlayer.getName().equalsIgnoreCase(name))
                return superiorPlayer;
        }

        return null;
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(UUID uuid){
        if(!players.containsKey(uuid)) {
            players.put(uuid, new SSuperiorPlayer(uuid));
            Executor.sync(() -> plugin.getDataHandler().insertPlayer(players.get(uuid)));
        }
        return players.get(uuid);
    }

    @Override
    public PlayerRole getPlayerRole(int index) {
        return roles.get(index);
    }

    @Override
    public PlayerRole getPlayerRole(String name) {
        for(PlayerRole playerRole : roles.values()){
            if(playerRole.toString().equalsIgnoreCase(name))
                return playerRole;
        }

        throw new IllegalArgumentException("Invalid role name: " + name);
    }

    @Override
    public PlayerRole getDefaultRole() {
        return getPlayerRole(0);
    }

    @Override
    public PlayerRole getLastRole() {
        return getPlayerRole(lastRole);
    }

    @Override
    public PlayerRole getGuestRole() {
        return getPlayerRole(GUEST_ROLE_INDEX);
    }

    @Override
    public PlayerRole getCoopRole() {
        return getPlayerRole(COOP_ROLE_INDEX);
    }

    @Override
    public List<PlayerRole> getRoles(){
        return new ArrayList<>(roles.values());
    }


    public void loadPlayer(ResultSet resultSet) throws SQLException {
        UUID player = UUID.fromString(resultSet.getString("player"));
        players.put(player, new SSuperiorPlayer(resultSet));
    }


    public void loadPlayer(CompoundTag tag){
        UUID player = UUID.fromString(((StringTag) tag.getValue().get("player")).getValue());
        players.put(player, new SSuperiorPlayer(tag));
    }

    private void loadRole(ConfigurationSection section, int type){
        int weight = section.getInt("weight", type);
        roles.put(weight, new SPlayerRole(section.getString("name"), weight, section.getStringList("permissions")));
        if(weight > lastRole)
            lastRole = weight;
    }

}
