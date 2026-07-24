package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.ChatStates;
import org.bukkit.Bukkit;

import java.util.LinkedHashSet;
import java.util.Set;

public class IslandChat {

    private static final Set<SuperiorPlayer> spies = new LinkedHashSet<>();

    private IslandChat() {

    }

    public static void addSpy(SuperiorPlayer spy) {
        spies.add(spy);
    }

    public static void removeSpy(SuperiorPlayer spy) {
        spies.remove(spy);
    }

    public static void handleIslandChat(Island island, SuperiorPlayer superiorPlayer, ChatState chatState, String message) {
        if (chatState != ChatStates.LOCAL_CHAT && chatState != ChatStates.TEAM_CHAT) {
            return;
        }

        PluginEvent<PluginEventArgs.IslandChat> event = PluginEventsFactory.callIslandChatEvent(island, superiorPlayer, chatState,
                superiorPlayer.hasPermissionWithoutOP("superior.chat.color") ? Formatters.COLOR_FORMATTER.format(message) : message);

        if (event.isCancelled()) {
            return;
        }

        if (chatState == ChatStates.LOCAL_CHAT) {
            handleLocalChat(island, superiorPlayer, event.getArgs().message);
        } else {
            handleTeamChat(island, superiorPlayer, event.getArgs().message);
        }
    }

    private static void handleLocalChat(Island island, SuperiorPlayer superiorPlayer, String message) {
        String playerRoleName = IslandUtils.getPlayerRole(island, superiorPlayer).getDisplayName();
        String superiorPlayerName = superiorPlayer.getName();

        for (SuperiorPlayer targetPlayer : island.getAllPlayersInside()) {
            Message.LOCAL_CHAT_FORMAT.send(targetPlayer, playerRoleName, superiorPlayerName, message);
        }

        Message.SPY_LOCAL_CHAT_FORMAT.send(Bukkit.getConsoleSender(), playerRoleName, superiorPlayerName, message);

        for (SuperiorPlayer targetPlayer : spies) {
            Message.SPY_LOCAL_CHAT_FORMAT.send(targetPlayer, playerRoleName, superiorPlayerName, message);
        }
    }

    private static void handleTeamChat(Island island, SuperiorPlayer superiorPlayer, String message) {
        String playerRoleName = IslandUtils.getPlayerRole(island, superiorPlayer).getDisplayName();
        String superiorPlayerName = superiorPlayer.getName();

        for (SuperiorPlayer targetPlayer : island.getIslandMembers(true)) {
            Message.TEAM_CHAT_FORMAT.send(targetPlayer, playerRoleName, superiorPlayerName, message);
        }

        Message.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), playerRoleName, superiorPlayerName, message);

        for (SuperiorPlayer targetPlayer : spies) {
            Message.SPY_TEAM_CHAT_FORMAT.send(targetPlayer, playerRoleName, superiorPlayerName, message);
        }
    }

}
