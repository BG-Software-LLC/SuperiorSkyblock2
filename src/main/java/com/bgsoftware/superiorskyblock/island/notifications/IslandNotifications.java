package com.bgsoftware.superiorskyblock.island.notifications;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

import java.util.Collections;

public class IslandNotifications {

    private IslandNotifications() {

    }

    public static void notifyPlayerQuit(SuperiorPlayer superiorPlayer) {
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if (island == null)
            return;

        IslandUtils.sendMessage(island, Message.PLAYER_QUIT_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());

        boolean anyOnline = island.getIslandMembers(true).stream().anyMatch(islandMember ->
                islandMember != superiorPlayer && islandMember.isOnline());

        if (!anyOnline) {
            island.setLastTimeUpdate(System.currentTimeMillis() / 1000);
            island.setCurrentlyActive(false);
        }
    }

    public static void notifyPlayerJoin(SuperiorPlayer superiorPlayer) {
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();
        if (island == null)
            return;

        IslandUtils.sendMessage(island, Message.PLAYER_JOIN_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
        island.updateLastTime();
        island.setCurrentlyActive(true);
    }

}
