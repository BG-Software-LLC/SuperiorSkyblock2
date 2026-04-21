package com.bgsoftware.superiorskyblock.island.notifications;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;

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

        // Give interest as soon as the owner joins the server
        if (BuiltinModules.BANK.getConfiguration().isBankInterestEnabled() && superiorPlayer == island.getOwner()) {
            int bankInterestInterval = BuiltinModules.BANK.getConfiguration().getBankInterestInterval();
            long currentTime = System.currentTimeMillis() / 1000;
            long ticksToNextInterest = (bankInterestInterval - (currentTime - island.getLastInterestTime())) * 20;
            if (ticksToNextInterest <= 0) {
                island.giveInterest(false);
            }
        }
    }

}
