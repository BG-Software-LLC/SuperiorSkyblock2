package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayersHandler {

    private static Map<UUID, WrappedPlayer> players = new HashMap<>();

    public WrappedPlayer getWrappedPlayer(String name){
        for(WrappedPlayer wrappedPlayer : players.values()){
            if(wrappedPlayer.getName().equalsIgnoreCase(name))
                return wrappedPlayer;
        }

        return null;
    }

    public WrappedPlayer getWrappedPlayer(UUID uuid){
        if(!players.containsKey(uuid))
            players.put(uuid, new WrappedPlayer(uuid));
        return players.get(uuid);
    }

    public void loadPlayer(CompoundTag tag){
        UUID player = UUID.fromString(((StringTag) tag.getValue().get("player")).getValue());
        players.put(player, new WrappedPlayer(tag));
    }

    public List<WrappedPlayer> getAllPlayers(){
        return new ArrayList<>(players.values());
    }

}
