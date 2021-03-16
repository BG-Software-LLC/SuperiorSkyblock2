package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdExpel implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("expel");
    }

    @Override
    public String getPermission() {
        return "superior.island.expel";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "expel <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_EXPEL.getMessage(locale);
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.EXPEL_PLAYERS;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_EXPEL_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island playerIsland, String[] args) {
        CommandSender sender = superiorPlayer == null ? Bukkit.getConsoleSender() : superiorPlayer.asPlayer();
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[1]);

        if(targetPlayer == null || sender == null)
            return;

        Player target = targetPlayer.asPlayer();

        if(target == null){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        Island targetIsland = plugin.getGrid().getIslandAt(target.getLocation());

        if(targetIsland == null){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
            return;
        }

        // Checking requirements for players
        if(superiorPlayer != null){
            if(!targetIsland.equals(playerIsland)){
                Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
                return;
            }

            if(targetIsland.hasPermission(targetPlayer, IslandPrivileges.EXPEL_BYPASS)){
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
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length != 2 ? new ArrayList<>() : island != null ? CommandTabCompletes.getIslandVisitors(island, args[1]) :
                CommandTabCompletes.getOnlinePlayers(plugin, args[1], plugin.getSettings().tabCompleteHideVanished,
                        onlinePlayer -> plugin.getGrid().getIslandAt(onlinePlayer.getLocation()) != null);
    }

}
