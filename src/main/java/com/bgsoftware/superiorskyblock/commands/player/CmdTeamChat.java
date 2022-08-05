package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdTeamChat implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("teamchat", "chat", "tc");
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
            if (!plugin.getEventsBus().callPlayerToggleTeamChatEvent(superiorPlayer))
                return;

            if (superiorPlayer.hasTeamChatEnabled()) {
                Message.TOGGLED_TEAM_CHAT_OFF.send(superiorPlayer);
            } else {
                Message.TOGGLED_TEAM_CHAT_ON.send(superiorPlayer);
            }

            superiorPlayer.toggleTeamChat();
        } else {
            String message = CommandArguments.buildLongString(args, 1, superiorPlayer.hasPermissionWithoutOP("superior.chat.color"));
            IslandUtils.sendMessage(island, Message.TEAM_CHAT_FORMAT, Collections.emptyList(), superiorPlayer.getPlayerRole(),
                    superiorPlayer.getName(), message);
            Message.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), superiorPlayer.getPlayerRole(), superiorPlayer.getName(), message);
            for (Player _onlinePlayer : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(_onlinePlayer);
                if (onlinePlayer.hasAdminSpyEnabled())
                    Message.SPY_TEAM_CHAT_FORMAT.send(onlinePlayer, superiorPlayer.getPlayerRole(), superiorPlayer.getName(), message);
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
