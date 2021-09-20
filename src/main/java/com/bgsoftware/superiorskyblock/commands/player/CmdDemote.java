package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdDemote implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("demote");
    }

    @Override
    public String getPermission() {
        return "superior.island.demote";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "demote <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_DEMOTE.getMessage(locale);
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
        return IslandPrivileges.DEMOTE_MEMBERS;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_DEMOTE_PERMISSION;
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
            Locale.DEMOTE_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return;
        }

        PlayerRole previousRole = targetPlayer.getPlayerRole();
        int roleLimit;

        do {
            previousRole = previousRole.getPreviousRole();
            roleLimit = previousRole == null ? -1 : island.getRoleLimit(previousRole);
        }while (previousRole != null && !previousRole.isFirstRole() && roleLimit >= 0 && roleLimit >= island.getIslandMembers(previousRole).size());

        if (previousRole == null) {
            Locale.LAST_ROLE_DEMOTE.send(superiorPlayer);
            return;
        }

        targetPlayer.setPlayerRole(previousRole);

        Locale.DEMOTED_MEMBER.send(superiorPlayer, targetPlayer.getName(), targetPlayer.getPlayerRole());
        Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getIslandMembers(island, args[1], islandMember ->
                islandMember.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole()) &&
                        islandMember.getPlayerRole().getPreviousRole() != null) : new ArrayList<>();
    }

}
