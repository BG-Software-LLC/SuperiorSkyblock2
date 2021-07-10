package com.bgsoftware.superiorskyblock.listeners.logic;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;

import java.util.Collections;

public final class PlayersLogic {

    private PlayersLogic(){
    }

    public static void handleJoin(SuperiorPlayer superiorPlayer){
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if(island != null) {
            IslandUtils.sendMessage(island, Locale.PLAYER_JOIN_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            island.updateLastTime();
            island.setCurrentlyActive();
        }
    }

    public static void handleQuit(SuperiorPlayer superiorPlayer){
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if(island != null) {
            IslandUtils.sendMessage(island, Locale.PLAYER_QUIT_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            boolean anyOnline = island.getIslandMembers(true).stream().anyMatch(_superiorPlayer ->
                    !_superiorPlayer.getUniqueId().equals(superiorPlayer.getUniqueId()) &&  _superiorPlayer.isOnline());
            if(!anyOnline)
                island.setLastTimeUpdate(System.currentTimeMillis() / 1000);
        }
    }

}
