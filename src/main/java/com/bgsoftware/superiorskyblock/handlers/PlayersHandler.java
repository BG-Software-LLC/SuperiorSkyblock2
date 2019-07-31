package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class PlayersHandler implements PlayersManager {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static Map<UUID, SuperiorPlayer> players = new HashMap<>();

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

    public void loadPlayer(ResultSet resultSet) throws SQLException {
        UUID player = UUID.fromString(resultSet.getString("player"));
        players.put(player, new SSuperiorPlayer(resultSet));
    }


    public void loadPlayer(CompoundTag tag){
        UUID player = UUID.fromString(((StringTag) tag.getValue().get("player")).getValue());
        players.put(player, new SSuperiorPlayer(tag));
    }

    public List<SuperiorPlayer> getAllPlayers(){
        return new ArrayList<>(players.values());
    }

}
