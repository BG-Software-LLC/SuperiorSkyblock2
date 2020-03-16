package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.database.CachedResultSet;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class PlayersHandler implements PlayersManager {

    private static final int GUEST_ROLE_INDEX = -2, COOP_ROLE_INDEX = -1;

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static Registry<UUID, SuperiorPlayer> players = Registry.createRegistry();
    private static Registry<Integer, PlayerRole> roles = Registry.createRegistry();
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
            players.add(uuid, new SSuperiorPlayer(uuid));
            Executor.async(() -> plugin.getDataHandler().insertPlayer(players.get(uuid)));
        }
        return players.get(uuid);
    }

    @Override
    public List<SuperiorPlayer> getAllPlayers() {
        return new ArrayList<>(players.values());
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
        return roles.keys().stream().sorted().map(roles::get).collect(Collectors.toList());
    }


    public void loadPlayer(CachedResultSet resultSet){
        UUID player = UUID.fromString(resultSet.getString("player"));
        SuperiorPlayer superiorPlayer = new SSuperiorPlayer(resultSet);
        synchronized (this) {
            players.add(player, superiorPlayer);
        }
        resultSet.delete();
    }

    private void loadRole(ConfigurationSection section, int type){
        int weight = section.getInt("weight", type);
        roles.add(weight, new SPlayerRole(section.getString("name"), weight, section.getStringList("permissions")));
        if(weight > lastRole)
            lastRole = weight;
    }

}
