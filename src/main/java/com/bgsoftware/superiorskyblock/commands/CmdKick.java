package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.Locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdKick implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("kick", "remove");
    }

    @Override
    public String getPermission() {
        return "superior.island.kick";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "kick <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_KICK.getMessage(locale);
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
        return IslandPrivileges.KICK_MEMBER;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_KICK_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if(targetPlayer == null)
            return;

        if(!island.isMember(targetPlayer)){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        if(!targetPlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole())){
            Locale.KICK_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return;
        }

        EventsCaller.callIslandKickEvent(superiorPlayer, targetPlayer, island);

        island.kickMember(targetPlayer);

        IslandUtils.sendMessage(island, Locale.KICK_ANNOUNCEMENT, new ArrayList<>(), targetPlayer.getName(), superiorPlayer.getName());

        Locale.GOT_KICKED.send(targetPlayer, superiorPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getIslandMembersWithLowerRole(island, args[1], superiorPlayer.getPlayerRole()) : new ArrayList<>();
    }

}
