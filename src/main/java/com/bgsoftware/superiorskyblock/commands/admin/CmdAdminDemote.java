package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminDemote implements IAdminPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("demote");
    }

    @Override
    public String getPermission() {
        return "superior.admin.demote";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin demote <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DEMOTE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        PlayerRole currentRole = targetPlayer.getPlayerRole();

        if(currentRole.isLastRole()){
            Locale.DEMOTE_LEADER.send(sender);
            return;
        }

        PlayerRole previousRole = currentRole;
        int roleLimit;

        do {
            previousRole = previousRole.getPreviousRole();
            roleLimit = previousRole == null ? -1 : island.getRoleLimit(previousRole);
        }while (previousRole != null && !previousRole.isFirstRole() && roleLimit >= 0 && roleLimit >= island.getIslandMembers(previousRole).size());

        if(previousRole == null){
            Locale.LAST_ROLE_DEMOTE.send(sender);
            return;
        }

        targetPlayer.setPlayerRole(previousRole);

        Locale.DEMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
        Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
    }

}
