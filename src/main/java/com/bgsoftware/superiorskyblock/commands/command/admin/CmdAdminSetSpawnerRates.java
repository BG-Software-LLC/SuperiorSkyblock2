package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetSpawnerRates implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setspawnerrates");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setspawnerrates";
    }

    @Override
    public String getUsage() {
        return "island admin setspawnerrates <player-name/island-name> <multiplier>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_SPAWNER_RATES.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if(island == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        double multiplier;

        try{
            multiplier = Double.valueOf(args[3]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_MULTIPLIER.send(sender, args[3]);
            return;
        }

        island.setSpawnerRatesMultiplier(multiplier);

        if(targetPlayer == null)
            Locale.CHANGED_SPAWNER_RATES_NAME.send(sender, island.getName());
        else
            Locale.CHANGED_SPAWNER_RATES.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                if (onlinePlayer.getIsland() != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if (onlinePlayer.getIsland() != null && onlinePlayer.getIsland().getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(onlinePlayer.getIsland().getName());
                }
            }
        }

        return list;
    }
}
