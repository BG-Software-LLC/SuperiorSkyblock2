package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdUncoop implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("uncoop", "untrust");
    }

    @Override
    public String getPermission() {
        return "superior.island.uncoop";
    }

    @Override
    public String getUsage() {
        return "uncoop <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage() + ">";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_UNCOOP.getMessage();
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

        if(!superiorPlayer.hasPermission(IslandPermission.UNCOOP_MEMBER)){
            Locale.NO_UNCOOP_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.UNCOOP_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(!island.isCoop(targetPlayer)){
            Locale.PLAYER_NOT_COOP.send(superiorPlayer);
            return;
        }

        island.removeCoop(targetPlayer);

        if(!Locale.UNCOOP_ANNOUNCEMENT.isEmpty())
            island.sendMessage(Locale.UNCOOP_ANNOUNCEMENT.getMessage(superiorPlayer.getName(), targetPlayer.getName()));

        if(island.getName().isEmpty())
            Locale.LEFT_ISLAND_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Locale.LEFT_ISLAND_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.COOP_MEMBER)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer targetPlayer = SSuperiorPlayer.of(player);
                if(island.isCoop(targetPlayer) && player.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(player.getName());
                }
            }

            return list;
        }
        return new ArrayList<>();
    }
}
