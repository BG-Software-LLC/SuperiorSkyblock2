package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAccept implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("accept", "join");
    }

    @Override
    public String getPermission() {
        return "superior.island.accept";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "accept [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ACCEPT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        SuperiorPlayer targetPlayer;
        Island island;

        if (args.length == 1) {
            List<Island> playerPendingInvites = superiorPlayer.getInvites();
            island = playerPendingInvites.isEmpty() ? null : playerPendingInvites.get(0);
            targetPlayer = null;
        } else {
            targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[1]);
            island = targetPlayer == null ? plugin.getGrid().getIsland(args[1]) : targetPlayer.getIsland();
        }

        if (island == null || !island.isInvited(superiorPlayer)) {
            Message.NO_ISLAND_INVITE.send(superiorPlayer);
            return;
        }

        if (superiorPlayer.getIsland() != null) {
            Message.JOIN_WHILE_IN_ISLAND.send(superiorPlayer);
            return;
        }

        if (island.getTeamLimit() >= 0 && island.getIslandMembers(true).size() >= island.getTeamLimit()) {
            Message.JOIN_FULL_ISLAND.send(superiorPlayer);
            island.revokeInvite(superiorPlayer);
            return;
        }

        if (!plugin.getEventsBus().callIslandJoinEvent(superiorPlayer, island, IslandJoinEvent.Cause.INVITE))
            return;

        IslandUtils.sendMessage(island, Message.JOIN_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName());

        island.revokeInvite(superiorPlayer);
        island.addMember(superiorPlayer, SPlayerRole.defaultRole());

        if (targetPlayer == null)
            Message.JOINED_ISLAND_NAME.send(superiorPlayer, island.getName());
        else
            Message.JOINED_ISLAND.send(superiorPlayer, targetPlayer.getName());

        if (plugin.getSettings().isTeleportOnJoin())
            superiorPlayer.teleport(island);
        if (plugin.getSettings().isClearOnJoin())
            plugin.getNMSPlayers().clearInventory(superiorPlayer.asPlayer());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), (onlinePlayer, onlineIsland) ->
                        onlineIsland != null && onlineIsland.isInvited(superiorPlayer)) : Collections.emptyList();
    }

}
