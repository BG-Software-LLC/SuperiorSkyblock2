package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CmdExpel implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("expel");
    }

    @Override
    public String getPermission() {
        return "superior.island.expel";
    }

    @Override
    public String getUsage() {
        return "island expel <player-name>";
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
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

        if(targetPlayer == null || !targetPlayer.asOfflinePlayer().isOnline()){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        Player target = targetPlayer.asPlayer();
        Island island = plugin.getGrid().getIslandAt(target.getLocation());

        if(sender instanceof Player){
            WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
            Island playerIsland = plugin.getGrid().getIsland(wrappedPlayer);

            if(playerIsland == null){
                Locale.INVALID_ISLAND.send(sender);
                return;
            }

            if(!wrappedPlayer.hasPermission(IslandPermission.EXPEL_PLAYERS)){
                Locale.NO_EXPEL_PERMISSION.send(sender, island.getRequiredRole(IslandPermission.EXPEL_PLAYERS));
                return;
            }

            if(!island.equals(playerIsland)){
                Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
                return;
            }

            if(island.hasPermission(targetPlayer, IslandPermission.EXPEL_BYPASS)){
                Locale.PLAYER_EXPEL_BYPASS.send(sender);
                return;
            }
        }

        target.teleport(plugin.getGrid().getSpawnIsland().getCenter());
        Locale.EXPELLED_PLAYER.send(sender, targetPlayer.getName());
        Locale.GOT_EXPELLED.send(targetPlayer, sender.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.EXPEL_BYPASS)){
            List<String> list = new ArrayList<>();
            WrappedPlayer targetPlayer;

            for (UUID uuid : wrappedPlayer.getIsland().getVisitors()) {
                targetPlayer = WrappedPlayer.of(uuid);
                if(targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(targetPlayer.getName());
            }

            return list;
        }

        return new ArrayList<>();
    }
}
