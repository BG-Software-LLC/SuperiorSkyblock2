package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public interface IAdminIslandCommand extends ISuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if(!supportMultipleIslands()){
            Pair<Island, SuperiorPlayer> arguments = CommandArguments.getIsland(plugin, sender, args[2]);
            if(arguments.getKey() != null)
                execute(plugin, sender, arguments.getValue(), arguments.getKey(), args);
        }
        else{
            Pair<List<Island>, SuperiorPlayer> arguments = CommandArguments.getMultipleIslands(plugin, sender, args[2]);
            if(!arguments.getKey().isEmpty())
                execute(plugin, sender, arguments.getValue(), arguments.getKey(), args);
        }
    }

    @Override
    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> tabVariables = new ArrayList<>();

        if(args.length == 3) {
            if(supportMultipleIslands() && "*".contains(args[2]))
                tabVariables.add("*");
            tabVariables.addAll(CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[2], false));
        }
        else if(args.length > 3){
            if(supportMultipleIslands()) {
                tabVariables = adminTabComplete(plugin, sender, null, args);
            }
            else{
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
                Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();
                if(island != null) {
                    tabVariables = adminTabComplete(plugin, sender, island, args);
                    if(tabVariables.isEmpty())
                        tabVariables = adminTabComplete(plugin, sender, island, args);
                }
            }
        }

        return tabVariables;
    }

    boolean supportMultipleIslands();

    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args){

    }

    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args){

    }

    default List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args){
        return new ArrayList<>();
    }

}
