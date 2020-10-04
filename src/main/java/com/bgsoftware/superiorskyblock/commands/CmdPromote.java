package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdPromote implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("promote");
    }

    @Override
    public String getPermission() {
        return "superior.island.promote";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "promote <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_PROMOTE.getMessage(locale);
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
        return IslandPrivileges.PROMOTE_MEMBERS;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_PROMOTE_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getTargetPlayer(plugin, superiorPlayer, args[1]);

        if(targetPlayer == null)
            return;

        if(!island.isMember(targetPlayer)){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        PlayerRole playerRole = targetPlayer.getPlayerRole();
        PlayerRole nextRole = playerRole.getNextRole();

        if(playerRole.isLastRole() || nextRole.isLastRole()){
            Locale.LAST_ROLE_PROMOTE.send(superiorPlayer);
            return;
        }

        if(!playerRole.isLessThan(superiorPlayer.getPlayerRole()) || nextRole.isHigherThan(superiorPlayer.getPlayerRole())){
            Locale.PROMOTE_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return;
        }

        targetPlayer.setPlayerRole(nextRole);

        Locale.PROMOTED_MEMBER.send(superiorPlayer, targetPlayer.getName(), targetPlayer.getPlayerRole());
        Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length != 2 ? new ArrayList<>() : CommandTabCompletes.getIslandMembers(island, args[1], islandMember -> {
            PlayerRole playerRole = islandMember.getPlayerRole();
            PlayerRole nextRole = playerRole.getNextRole();
            return !playerRole.isLastRole() && !nextRole.isLastRole() && playerRole.isLessThan(superiorPlayer.getPlayerRole()) &&
                    !nextRole.isHigherThan(superiorPlayer.getPlayerRole());
        });
    }

}
