package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface IPermissibleCommand extends ISuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island island = null;
        SuperiorPlayer superiorPlayer = null;

        if(!canBeExecutedByConsole() || sender instanceof Player){
            Pair<Island, SuperiorPlayer> arguments = CommandArguments.getSenderIsland(plugin, sender);

            island = arguments.getKey();

            if(island == null)
                return;

            superiorPlayer = arguments.getValue();

            if(!superiorPlayer.hasPermission(getPrivilege())){
                getPermissionLackMessage().send(superiorPlayer, island.getRequiredPlayerRole(getPrivilege()));
                return;
            }
        }

        execute(plugin, superiorPlayer, island, args);
    }

    @Override
    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island island = null;
        SuperiorPlayer superiorPlayer = null;

        if(!canBeExecutedByConsole() || sender instanceof Player){
            superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
            island = superiorPlayer.getIsland();
        }

        return superiorPlayer == null || (island != null && superiorPlayer.hasPermission(getPrivilege())) ?
                tabComplete(plugin, superiorPlayer, island, args) : new ArrayList<>();
    }

    IslandPrivilege getPrivilege();

    Locale getPermissionLackMessage();

    void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args);

    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args){
        return new ArrayList<>();
    }

}
