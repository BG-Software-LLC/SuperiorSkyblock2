package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandChat;
import com.bgsoftware.superiorskyblock.player.chat.ChatStates;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdTeamChat implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("teamchat", "tc");
    }

    @Override
    public String getPermission() {
        return "superior.island.teamchat";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "teamchat [" + Message.COMMAND_ARGUMENT_MESSAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_TEAM_CHAT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getIsland();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getSuperiorPlayer();

        if (args.length == 1) {
            ChatState oldChatState = superiorPlayer.getChatState();
            ChatState newChatState = oldChatState == ChatStates.TEAM_CHAT ?
                    ChatStates.GLOBAL : ChatStates.TEAM_CHAT;

            if (!PluginEventsFactory.callPlayerChangeChatStateEvent(superiorPlayer, newChatState) ||
                    !PluginEventsFactory.callPlayerToggleTeamChatEvent(superiorPlayer)) {
                return;
            }

            if (newChatState == ChatStates.TEAM_CHAT) {
                if (oldChatState == ChatStates.LOCAL_CHAT) {
                    Message.TOGGLED_LOCAL_CHAT_OFF.send(superiorPlayer);
                }

                Message.TOGGLED_TEAM_CHAT_ON.send(superiorPlayer);
            } else {
                Message.TOGGLED_TEAM_CHAT_OFF.send(superiorPlayer);
            }

            superiorPlayer.setChatState(newChatState);
        } else {
            String message = CommandArguments.buildLongString(args, 1, false);
            IslandChat.handleIslandChat(island, superiorPlayer, ChatStates.TEAM_CHAT, message);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
