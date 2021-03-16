package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdSetRole implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrole");
    }

    @Override
    public String getPermission() {
        return "superior.island.setrole";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setrole <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ISLAND_ROLE.getMessage(locale) +">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SET_ROLE.getMessage(locale);
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_ROLE;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_SET_ROLE_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island playerIsland, String[] args) {
        CommandSender sender = superiorPlayer == null ? Bukkit.getConsoleSender() : superiorPlayer.asPlayer();
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[1]);

        if(targetPlayer == null || sender == null)
            return;

        if(targetPlayer.getName().equals(sender.getName())){
            Locale.SELF_ROLE_CHANGE.send(sender);
            return;
        }

        PlayerRole playerRole = CommandArguments.getPlayerRole(sender, args[2]);

        if(playerRole == null)
            return;

        if(!playerRole.isRoleLadder()){
            Locale.INVALID_ROLE.send(sender, args[2], SPlayerRole.getValuesString());
            return;
        }

        Island targetIsland = targetPlayer.getIsland();

        // Checking requirements for players
        if(superiorPlayer != null){
            if(!playerIsland.isMember(targetPlayer)){
                Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
                return;
            }

            targetIsland = playerIsland;

            if(targetPlayer.getPlayerRole().isHigherThan(superiorPlayer.getPlayerRole()) ||
                    !playerRole.isLessThan(superiorPlayer.getPlayerRole())){
                Locale.CANNOT_SET_ROLE.send(sender, playerRole);
                return;
            }
        }
        else{
            if (targetIsland == null) {
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            if(playerRole.isLastRole()){
                Locale.CANNOT_SET_ROLE.send(sender, playerRole);
                return;
            }
        }

        if(targetPlayer.getPlayerRole().equals(playerRole)){
            Locale.PLAYER_ALREADY_IN_ROLE.send(sender, targetPlayer.getName(), playerRole);
            return;
        }

        int roleLimit = targetIsland.getRoleLimit(playerRole);

        if(roleLimit >= 0 && targetIsland.getIslandMembers(playerRole).size() >= roleLimit){
            Locale.CANNOT_SET_ROLE.send(sender, playerRole);
            return;
        }

        PlayerRole currentRole = targetPlayer.getPlayerRole();
        targetPlayer.setPlayerRole(playerRole);

        if(currentRole.isLessThan(playerRole)){
            Locale.PROMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
            Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
        }else{
            Locale.DEMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
            Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? island == null ?
                CommandTabCompletes.getOnlinePlayers(plugin, args[1], false, onlinePlayer -> onlinePlayer.getIsland() != null) :
                CommandTabCompletes.getIslandMembers(island, args[1]) :
                args.length == 3 ? CommandTabCompletes.getPlayerRoles(plugin, args[2], PlayerRole::isRoleLadder) : new ArrayList<>();
    }

}
