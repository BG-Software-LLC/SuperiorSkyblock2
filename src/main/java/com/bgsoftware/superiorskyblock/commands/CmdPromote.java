package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdPromote implements ISuperiorCommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.PROMOTE_MEMBERS)){
            Locale.NO_PROMOTE_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.PROMOTE_MEMBERS));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

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
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.PROMOTE_MEMBERS)){
            List<String> list = new ArrayList<>();

            for(SuperiorPlayer targetPlayer : island.getIslandMembers(false)){
                PlayerRole playerRole = targetPlayer.getPlayerRole();
                PlayerRole nextRole = playerRole.getNextRole();
                if(!playerRole.isLastRole() && !nextRole.isLastRole() && playerRole.isLessThan(superiorPlayer.getPlayerRole()) &&
                        !nextRole.isHigherThan(superiorPlayer.getPlayerRole()) &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
