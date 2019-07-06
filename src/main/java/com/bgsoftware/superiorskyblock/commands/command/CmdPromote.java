package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CmdPromote implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("promote");
    }

    @Override
    public String getPermission() {
        return "superior.island.promote";
    }

    @Override
    public String getUsage() {
        return "island promote <player-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_PROMOTE.getMessage();
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
            Locale.NO_PROMOTE_PERMISSION.send(superiorPlayer, island.getRequiredRole(IslandPermission.PROMOTE_MEMBERS));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(!targetPlayer.getIslandRole().isLessThan(superiorPlayer.getIslandRole()) ||
                targetPlayer.getIslandRole().getNextRole().isHigherThan(superiorPlayer.getIslandRole())){
            Locale.PROMOTE_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return;
        }

        if(targetPlayer.getIslandRole().getNextRole() == IslandRole.LEADER){
            Locale.LAST_ROLE_PROMOTE.send(superiorPlayer);
            return;
        }

        targetPlayer.setIslandRole(targetPlayer.getIslandRole().getNextRole());

        Locale.PROMOTED_MEMBER.send(superiorPlayer, targetPlayer.getName(), targetPlayer.getIslandRole());
        Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getIslandRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.PROMOTE_MEMBERS)){
            List<String> list = new ArrayList<>();
            SuperiorPlayer targetPlayer;

            for(UUID uuid : island.getAllMembers()){
                targetPlayer = SSuperiorPlayer.of(uuid);
                if(targetPlayer.getIslandRole().isLessThan(superiorPlayer.getIslandRole()) &&
                        !targetPlayer.getIslandRole().getNextRole().isHigherThan(superiorPlayer.getIslandRole()) &&
                        targetPlayer.getIslandRole().getNextRole() != IslandRole.LEADER &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
