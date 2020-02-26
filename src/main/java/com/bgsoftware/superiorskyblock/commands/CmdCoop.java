package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdCoop implements ISuperiorCommand {

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
        return "coop <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_COOP.getMessage(locale);
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

        if(!superiorPlayer.hasPermission(IslandPermission.COOP_MEMBER)){
            Locale.NO_COOP_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.COOP_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(island.isMember(targetPlayer)){
            Locale.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if(island.isCoop(targetPlayer)){
            Locale.PLAYER_ALREADY_COOP.send(superiorPlayer);
            return;
        }

        if(island.isBanned(targetPlayer)){
            Locale.COOP_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        island.addCoop(targetPlayer);

        ((SIsland) island).sendMessage(Locale.COOP_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), targetPlayer.getName());

        if(island.getName().isEmpty())
            Locale.JOINED_ISLAND_AS_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Locale.JOINED_ISLAND_AS_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.COOP_MEMBER)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer targetPlayer = SSuperiorPlayer.of(player);
                if(!island.isMember(targetPlayer) && !island.isBanned(targetPlayer) && !island.isCoop(targetPlayer) &&
                        player.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(player.getName());
                }
            }

            return list;
        }
        return new ArrayList<>();
    }
}
