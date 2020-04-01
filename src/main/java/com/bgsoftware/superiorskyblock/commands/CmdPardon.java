package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdPardon implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pardon", "unban");
    }

    @Override
    public String getPermission() {
        return "superior.island.pardon";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "pardon <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_PARDON.getMessage(locale);
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

        if(!superiorPlayer.hasPermission(IslandPrivileges.BAN_MEMBER)){
            Locale.NO_BAN_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.BAN_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(!island.isBanned(targetPlayer)){
            Locale.PLAYER_NOT_BANNED.send(superiorPlayer);
            return;
        }

        island.unbanMember(targetPlayer);

        ((SIsland) island).sendMessage(Locale.UNBAN_ANNOUNCEMENT, new ArrayList<>(), targetPlayer.getName(), superiorPlayer.getName());

        Locale.GOT_UNBANNED.send(targetPlayer, island.getOwner().getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPrivileges.BAN_MEMBER)){
            List<String> list = new ArrayList<>();

            for(SuperiorPlayer targetPlayer : island.getBannedPlayers()){
                if(targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
