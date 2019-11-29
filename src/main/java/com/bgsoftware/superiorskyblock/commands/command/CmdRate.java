package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.menu.IslandRateMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdRate implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rate");
    }

    @Override
    public String getPermission() {
        return "superior.island.rate";
    }

    @Override
    public String getUsage() {
        return "island rate [" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage() + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage() + "]";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_RATE.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 1;
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
        Island island;

        if(args.length == 2){
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);
            island = targetPlayer == null ? plugin.getGrid().getIsland(args[1]) : targetPlayer.getIsland();
        }
        else {
            island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());
        }

        if(island == null){
            Locale.INVALID_ISLAND_LOCATION.send(superiorPlayer);
            return;
        }

        if(!plugin.getSettings().rateOwnIsland && island.equals(superiorPlayer.getIsland())){
            Locale.RATE_OWN_ISLAND.send(superiorPlayer);
            return;
        }

        IslandRateMenu.openInventory(superiorPlayer, island, null);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();
        List<String> list = new ArrayList<>();

        if(args.length == 2){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island onlineIsland = onlinePlayer.getIsland();
                if (onlineIsland != null && (plugin.getSettings().rateOwnIsland || !onlineIsland.equals(island))) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(player.getName());
                    if(!onlineIsland.getName().isEmpty() && onlineIsland.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(onlineIsland.getName());
                }
            }
        }

        return list;
    }
}
