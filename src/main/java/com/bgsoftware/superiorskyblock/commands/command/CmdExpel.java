package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdExpel implements ICommand {

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
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_EXPEL.getMessage();
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null || !targetPlayer.asOfflinePlayer().isOnline()){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        Player target = targetPlayer.asPlayer();
        Island island = plugin.getGrid().getIslandAt(target.getLocation());

        if(island == null){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
            return;
        }

        if(sender instanceof Player){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
            Island playerIsland = plugin.getGrid().getIsland(superiorPlayer);

            if(playerIsland == null){
                Locale.INVALID_ISLAND.send(sender);
                return;
            }

            if(!superiorPlayer.hasPermission(IslandPermission.EXPEL_PLAYERS)){
                Locale.NO_EXPEL_PERMISSION.send(sender, island.getRequiredPlayerRole(IslandPermission.EXPEL_PLAYERS));
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

        targetPlayer.teleport(plugin.getGrid().getSpawnIsland());
        target.getLocation().setDirection(plugin.getGrid().getSpawnIsland().getCenter(World.Environment.NORMAL).getDirection());
        Locale.EXPELLED_PLAYER.send(sender, targetPlayer.getName());
        Locale.GOT_EXPELLED.send(targetPlayer, sender.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.EXPEL_BYPASS)){
            List<String> list = new ArrayList<>();

            for (SuperiorPlayer targetPlayer : superiorPlayer.getIsland().getIslandVisitors()) {
                if(targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(targetPlayer.getName());
            }

            return list;
        }

        return new ArrayList<>();
    }
}
