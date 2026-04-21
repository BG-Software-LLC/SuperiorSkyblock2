package com.bgsoftware.superiorskyblock.player.chat;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import org.bukkit.Location;

import java.util.List;

public class ChatStates {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static final ChatState GLOBAL = register(new ChatState("GLOBAL") {

        @Override
        public List<SuperiorPlayer> getTargetPlayers(SuperiorPlayer superiorPlayer) {
            return null;
        }

    });

    public static final ChatState LOCAL_CHAT = register(new ChatState("LOCAL_CHAT") {

        @Override
        public List<SuperiorPlayer> getTargetPlayers(SuperiorPlayer superiorPlayer) {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt((superiorPlayer.asPlayer()).getLocation(wrapper.getHandle()));
            }

            if (island == null || island.isSpawn()) {
                return null;
            } else {
                return island.getAllPlayersInside();
            }
        }

    });

    public static final ChatState TEAM_CHAT = register(new ChatState("TEAM_CHAT") {

        @Override
        public List<SuperiorPlayer> getTargetPlayers(SuperiorPlayer superiorPlayer) {
            Island island = superiorPlayer.getIsland();

            if (island == null) {
                return null;
            } else {
                return island.getAllPlayersInside();
            }
        }

    });

    public static void registerStates() {
        // Do nothing, only trigger all the register calls
    }

    private static ChatState register(ChatState chatState) {
        ChatState.register(chatState);
        return chatState;
    }

}
