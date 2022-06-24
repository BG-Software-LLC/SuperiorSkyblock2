package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdCoop implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("coop", "trust");
    }

    @Override
    public String getPermission() {
        return "superior.island.coop";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "coop <" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_COOP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.COOP_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_COOP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if (targetPlayer == null)
            return;

        if (!targetPlayer.isOnline()) {
            Message.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if (island.isMember(targetPlayer)) {
            Message.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if (island.isCoop(targetPlayer)) {
            Message.PLAYER_ALREADY_COOP.send(superiorPlayer);
            return;
        }

        if (island.isBanned(targetPlayer)) {
            Message.COOP_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        if (island.getCoopPlayers().size() >= island.getCoopLimit()) {
            Message.COOP_LIMIT_EXCEED.send(superiorPlayer);
            return;
        }

        if (!plugin.getEventsBus().callIslandCoopPlayerEvent(island, superiorPlayer, targetPlayer))
            return;

        island.addCoop(targetPlayer);

        IslandUtils.sendMessage(island, Message.COOP_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName(), targetPlayer.getName());

        if (island.getName().isEmpty())
            Message.JOINED_ISLAND_AS_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Message.JOINED_ISLAND_AS_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayers(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), onlinePlayer ->
                        !island.isMember(onlinePlayer) && !island.isBanned(onlinePlayer) && !island.isCoop(onlinePlayer))
                : Collections.emptyList();
    }

}
